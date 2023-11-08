/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import hu.agnos.cube.builder.entity.raw.RawCells;
import hu.agnos.cube.builder.entity.raw.RawCube;
import hu.agnos.cube.builder.entity.raw.RawDimension;
import hu.agnos.cube.builder.entity.raw.RawNode;
import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.builder.util.DBConector;
import hu.agnos.cube.specification.entity.CubeSpecification;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author parisek
 */
public class Step3 {

    private final Logger logger;

    public Step3() {
        logger = LoggerFactory.getLogger(Step3.class);
    }

    /**
     * Az sqlCube-ban definiált táblából létrehoz és adatokkal feltölt egy
     * RawCube-ot.
     *
     * @param sqlCube az importáláshoz szükséges SqlCube
     * @return az adatokkal feltöltött RawCube (ebben még nincsennek id-k és
     * intervallumok!!!)
     */
    public RawCube getPreCube(SqlCube sqlCube, String cubeUniqueName, CubeSpecification xmlCub) throws ClassNotFoundException {

        String sql = sqlCube.getDropSQL();
//        System.out.println("sql11112: " + sql);
        DBConector.slientExecuteQuery(sql, xmlCub.getSourceDBUser(), xmlCub.getSourceDBPassword(), xmlCub.getSourceDBURL(), xmlCub.getSourceDBDriver());

        String createSQL = sqlCube.getCreateSQL();
//        System.out.println("sql11113: " + createSQL);

        String dropIndexSQL = sqlCube.getDropIndexSQL();
//        System.out.println("sql11116: " + dropIndexSQL);

        String createIndexSQL = sqlCube.getCreateIndexSQL();
//        System.out.println("sql11117: " + createIndexSQL);

        String countSQL = "SELECT COUNT(1) FROM " + sqlCube.getSourceTableName();

        // ebben a tömbből megtudható, hogy a ResultSet egy adott oszlopa melyik dimenzió, melyik hierarchiája
        int[][] dimensionIndex = sqlCube.getDimensionIndex();

        RawCube preCube = sqlCube.getEmptyPreCube();

        int dimensionColumnCnt = sqlCube.getDimensionColumnCountInSQLResultSet();

        int measureCnt = sqlCube.getMeasures().size();
        RawCells preCells = null;

        Connection conn = null;
        Statement statement = null;

        String user = xmlCub.getSourceDBUser();
        String password = xmlCub.getSourceDBPassword();
        String url = xmlCub.getSourceDBURL();
        String driver = xmlCub.getSourceDBDriver();

        try {

            Date elozo = new Date();
//            System.out.println("Indulás");

            DBConector.executeQuery(createSQL, user, password, url, driver);

            DBConector.slientExecuteQuery(dropIndexSQL, xmlCub.getSourceDBUser(), xmlCub.getSourceDBPassword(), xmlCub.getSourceDBURL(), xmlCub.getSourceDBDriver());

            DBConector.executeQuery(createIndexSQL, user, password, url, driver);

            conn = DBConector.createDatabaseConnection(user, password, url, driver);

            statement = conn.createStatement();
            ResultSet rSet = statement.executeQuery(countSQL);
            int rowCnt = 0;

            while (rSet.next()) {
                rowCnt = rSet.getInt(1);
                break;
            }
            logger.info("Number of rows to export: " + rowCnt);

            preCells = new RawCells(rowCnt, measureCnt);

            int rowID = 0;

            String importSQL = null;
            int iterationCnt = rowCnt / 50000;

            for (int j = 0; j <= iterationCnt; j++) {

                importSQL = sqlCube.getImportSQL(j * 50000, j * 50000 + 50000);

//                System.out.println("importSQL: " + importSQL);
                rSet = statement.executeQuery(importSQL);

                elozo = new Date();

                while (rSet.next()) {

                    if (((rowID % 50000) == 0) && (rowID != 0)) {
                        Date most = new Date();
//                        System.out.println("\t\t\t" + (rowID / 50000) + ". ötvenezer sor feldolgozva "+ (most.getTime()-elozo.getTime())+" ms alatt." );
                        elozo = most;
                    }
                    List<RawNode> preNodes = new ArrayList<>();

                    //a ResultSet egy sorának kinyerése                
                    for (int i = 1; i <= dimensionColumnCnt; i++) {
                        String nodeCode = eliminateForbiddenChars(rSet.getString(i));
                        if (nodeCode == null) {
                            nodeCode = "All";
                        }
                        int nodeDepth = dimensionIndex[i][2] + 1;
                        i++; // mert egy level code-ból és name-ből áll
                        String nodeName = eliminateForbiddenChars(rSet.getString(i));
                        if (nodeName == null) {
                            nodeName = "All";
                        }
                        RawNode preNode = new RawNode(nodeDepth, nodeCode, nodeName);
                        preNode.addFactTableRowId(rowID);
                        preNodes.add(preNode);
                    }

                    for (int i = 0; i < measureCnt; i++) {
                        int index = dimensionColumnCnt + 1 + i;
                        float measure = rSet.getFloat(index);
                        preCells.addCell(rowID, i, measure);
                    }

//                m  = System.nanoTime();
//                
//                System.out.println("node measure kinyerése: " + (m-e));
//                e=m;
                    processDimensionRow(preNodes, dimensionIndex, preCube);

//                m  = System.nanoTime();
//                
//                System.out.println("processDimensionRow: " + (m-e));
//                e=m;
                    rowID++;
                }
            }
            preCube.setRawCells(preCells);

        } catch (SQLException ex) {
            logger.error(ex.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    logger.error(ex.getMessage());
                }
            }

        }
//        System.out.println("vege");
        return preCube;
    }

    /**
     * Az egy hierarchiába lévő node-ek(oszlopok) feldolgozása
     *
     * @param preNodes a feldolgozandó node-ok listája
     * @param hierarchy az a hierarchia, amelybe a nodokat bele kell rakni
     */
    private void processHierarchyRow(List<RawNode> preNodes, RawDimension hierarchy) {

        if (hierarchy.isOfflineCalculated()) {
            processOLAPHierarchyRow(preNodes, hierarchy);
        } else {
            processNotOLAPHierarchyRow(preNodes, hierarchy);

        }
    }

    /**
     * Egy nem olaposított hierarchia nodjainak hierarchiába töltése. Fontos
     * külömbsége egy OLAP-osított és egy nem OLAP-osított hierarchia
     * feldolgozása között, hogy nem OLAP-osított hierarchiánál a FactTableRowID
     * minden érintett nodban eltárolódik, ezzel szemben az OLAP-ositott
     * esetében csak a legutolsó nem null / ALL kódú node-ban.
     *
     * @param preNodes a feldolgozandó node-ok listája
     * @param hierarchy az a hierarchia, amelybe a nodokat bele kell rakni
     */
    private void processNotOLAPHierarchyRow(List<RawNode> preNodes, RawDimension hierarchy) {

        long e = System.nanoTime();

        RawNode parentNode = hierarchy.getRoot();

//        long m  = System.nanoTime();
//                
//                System.out.println("hierarchy.getRoot(): " + (m-e));
//                e=m;
//root factTableRowId feltöltése
        for (int rowId : preNodes.get(0).getFactTableRowIds().toArray()) {

            parentNode.addFactTableRowId(rowId);
//             m  = System.nanoTime();
//                
//                System.out.println(" parentNode.addFactTableRowId: " + (m-e));
//                e=m;
        }

        for (int j = 0; j < preNodes.size(); j++) {

//             m  = System.nanoTime();
//                
//                System.out.println(" existingNode: " + (m-e));
//                e=m;
            RawNode actualNode = preNodes.get(j);
            RawNode existingNode = parentNode.getChild(actualNode.getCode());

            if (existingNode == null) {
                parentNode.addNewChild(actualNode);
                parentNode = actualNode;
            } else {
                for (int rowId : actualNode.getFactTableRowIds().toArray()) {
                    existingNode.addFactTableRowId(rowId);
                }
                parentNode = existingNode;
            }
        }

    }

    /**
     * Egy olaposított hierarchia nodjainak hierarchiába töltése. Fontos
     * külömbsége egy OLAP-osított és egy nem OLAP-osított hierarchia
     * feldolgozása között, hogy nem OLAP-osított hierarchiánál a FactTableRowID
     * minden érintett nodban eltárolódik, ezzel szemben az OLAP-ositott
     * esetében csak a legutolsó nem null / ALL kódú node-ban.
     *
     * @param preNodes a feldolgozandó node-ok listája
     * @param hierarchy az a hierarchia, amelybe a nodokat bele kell rakni
     */
    private void processOLAPHierarchyRow(List<RawNode> preNodes, RawDimension hierarchy) {
        int lastNonAggregetedNodeIdx = -1;
        int i = 0;
        int preNodesSize = preNodes.size();
        while (i < preNodes.size()) {
            if (preNodes.get(i).getCode().equals("All")) {
                lastNonAggregetedNodeIdx = i - 1;
                break;
            }
            i++;
        }

        if (i == preNodesSize) {
            lastNonAggregetedNodeIdx = i - 1;
        }

        if (lastNonAggregetedNodeIdx == -1) {

            RawNode actulaNode = preNodes.get(0);
            RawNode root = hierarchy.getRoot();
            for (int rowId : actulaNode.getFactTableRowIds().toArray()) {
                root.addFactTableRowId(rowId);
            }
        } else {
            RawNode parentNode = hierarchy.getRoot();
            RawNode actualNode = null;
            for (int j = 0; j < lastNonAggregetedNodeIdx; j++) {
                actualNode = preNodes.get(j);
                if (!parentNode.hasChild(actualNode.getCode())) {
                    actualNode.getFactTableRowIds().clear();
                    parentNode.addNewChild(actualNode);
                }

                parentNode = parentNode.getChild(actualNode.getCode());
            }
            actualNode = preNodes.get(lastNonAggregetedNodeIdx);
            RawNode existingNode = parentNode.getChild(actualNode.getCode());
            if (existingNode == null) {
                parentNode.addNewChild(actualNode);

            } else {

                for (int rowId : actualNode.getFactTableRowIds().toArray()) {
                    existingNode.addFactTableRowId(rowId);

                }
            }
        }
    }

    /**
     * Egy dimenzió node-jainak feldolgozása
     *
     * @param preNodes a feldolgozandó node-ok listája, ennek annyi eleme van,
     * ahány sora a ResultSet-nek
     * @param dimensionIndex az a tömb amely a ResultSet minden egyes oszlopát
     * dimenzióba, azon belül hierarchiákba sorolja.
     * @param preCube az a cube, amelybe a megkapott node-okat töltjük.
     */
    private void processDimensionRow(List<RawNode> preNodes, int[][] dimensionIndex, RawCube preCube) {
        //ez minden egyes node-hoz megmondja, hogy az melyik dimenzió, melyik hierarchiája
        //szerepe majdnem megegyezik a dimensionIndex-vel, de annak 2x annyi sora van, 
        //mert egy node  két oszlopból áll a ResultSet-ben
        int[][] nodeIndexes = new int[preNodes.size()][];

        int idx = 0;
        for (int i = 0; i < dimensionIndex.length; i = i + 2) {
            int[] dimAndHierIdx = new int[2];
            dimAndHierIdx[0] = dimensionIndex[i][0];
            dimAndHierIdx[1] = dimensionIndex[i][1];
            nodeIndexes[idx] = dimAndHierIdx;
            idx++;
        }

        List<RawNode> hierNodes = new ArrayList();

        for (int i = 0; i < preNodes.size(); i++) {
            int dimIdx = nodeIndexes[i][0];

            int hierIdx = nodeIndexes[i][1];

            while ((i < preNodes.size()) && (dimIdx == nodeIndexes[i][0]) && (hierIdx == nodeIndexes[i][1])) {
                hierNodes.add(preNodes.get(i));
                i++;
            }
            processHierarchyRow(hierNodes, preCube.getDimension(dimIdx).getDimension(hierIdx));

            hierNodes = new ArrayList();
            i--;

        }
    }

    private String eliminateForbiddenChars(String originString) {
        if (originString == null || originString.isEmpty()) {
            return originString;
        } else {
            return originString.replaceAll("'", "").replaceAll("\"", "").replace("\\", "");
        }
    }

}
