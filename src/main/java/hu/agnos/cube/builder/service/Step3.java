/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.builder.entity.pre.AbstractPreCells;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import hu.agnos.cube.builder.entity.pre.PreCellsFloat;
import hu.agnos.cube.builder.entity.pre.PreCellsInt;
import hu.agnos.cube.builder.entity.pre.PreCube;
import hu.agnos.cube.builder.entity.pre.PreDimension;
import hu.agnos.cube.builder.entity.pre.PreNode;
import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.builder.util.DBConector;
import hu.agnos.cube.specification.entity.CubeSpecification;
import hu.agnos.cube.specification.entity.MeasureType;

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
     * PreCube-ot.
     *
     * @param sqlCube az importáláshoz szükséges SqlCube
     * @return az adatokkal feltöltött PreCube (ebben még nincsennek id-k és
     * intervallumok!!!)
     */
    public PreCube getPreCube(SqlCube sqlCube, String cubeUniqueName, CubeSpecification xmlCub) throws ClassNotFoundException {

        String dropTableSQL = sqlCube.getDropSQL();
//        System.out.println("sql11112: " + dropTableSQL);
        DBConector.slientExecuteQuery(dropTableSQL, xmlCub.getSourceDBUser(), xmlCub.getSourceDBPassword(), xmlCub.getSourceDBURL(), xmlCub.getSourceDBDriver());

        String createSQL = sqlCube.getCreateSQL();
//        System.out.println("sql11113: " + createSQL);

        String dropIndexSQL = sqlCube.getDropIndexSQL();
//        System.out.println("sql11116: " + dropIndexSQL);

        String createIndexSQL = sqlCube.getCreateIndexSQL();
//        System.out.println("sql11117: " + createIndexSQL);

        String countSQL = "SELECT COUNT(1) FROM " + sqlCube.getSourceTableName();

        // ebben a tömbből megtudható, hogy a ResultSet egy adott oszlopa melyik dimenzióhoz tartozik
        int[][] dimensionIndex = sqlCube.getDimensionIndex();

//        
//          int[][] dimIdx = sqlCube.getDimensionIndex();
//        for(int i= 0; i < dimIdx.length; i++){
//            System.out.println("i:" + i);
//            for(int j = 0; j < dimIdx[i].length; j++){
//                System.out.println("\tj:" + j);
//                System.out.println("\t\tvalue:" + dimIdx[i][j]);
//            }   
//        }
        PreCube preCube = sqlCube.getEmptyPreCube();

        int dimensionColumnCnt = sqlCube.getDimensionColumnCount();
//        System.out.println("dimensionColumnCnt: " + dimensionColumnCnt);

        int measureCnt = sqlCube.getMeasures().size();
//        System.out.println("measureCnt: " + measureCnt);

        AbstractPreCells preCells = null;

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

//            System.out.println("createSQL: " + createSQL);
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

            if (preCube.getType().equals(MeasureType.CLASSICAL.getType())) {
                preCells = new PreCellsFloat(rowCnt, measureCnt, preCube.getType());
            } else if (preCube.getType().equals(MeasureType.COUNT_DISTINCT.getType())) {
                preCells = new PreCellsInt(rowCnt, preCube.getType());
            }

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
                    List<PreNode> preNodes = new ArrayList<>();

                    //a ResultSet egy sorának kinyerése                
                    for (int i = 0; i < dimensionColumnCnt; i++) {
                        //+1 mert a rSet oszlopainak számozása 1-től kezdődik
                        String nodeCode = eliminateForbiddenChars(rSet.getString(i + 1));

                        i++; // mert egy level code-ból és name-ből áll
                        String nodeName = eliminateForbiddenChars(rSet.getString(i + 1));

                        //+1 mert a nulladik szinten a Root van
                        int nodeDepth = dimensionIndex[i][1] + 1;

//                        System.out.println("i: " + i
//                                + ", nodeCode: " + nodeCode 
//                                + ", nodeName: " + nodeName
//                                + ", nodeDepth: "+ nodeDepth);
                        PreNode preNode = new PreNode(nodeDepth, nodeCode, nodeName);
                        preNode.addFactTableRowId(rowID);
                        preNodes.add(preNode);
                    }

                    if (preCube.getType().equals(MeasureType.CLASSICAL.getType())) {
                        for (int i = 0; i < measureCnt; i++) {
                            int index = dimensionColumnCnt + 1 + i;
                            float measure = rSet.getFloat(index);
                            ((PreCellsFloat) preCells).addCell(rowID, i, measure);
                        }

                    } 
                    else if (preCube.getType().equals(MeasureType.COUNT_DISTINCT.getType())) {
                            int index = dimensionColumnCnt + 1 ;
                            String measureString = rSet.getString(index);
                            String[] measureArray = measureString.split(",");
                            int[] measure = new int[measureArray.length];
                            for(int i = 0; i < measureArray.length; i++){
                                measure[i] = Integer.parseInt(measureArray[i]);
                            }
                            ((PreCellsInt) preCells).addColumn(rowID, measure);
                        
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
    private void processHierarchyRow(List<PreNode> preNodes, PreDimension hierarchy) {

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
    private void processNotOLAPHierarchyRow(List<PreNode> preNodes, PreDimension hierarchy) {

        long e = System.nanoTime();

        PreNode parentNode = hierarchy.getRoot();

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
            PreNode actualNode = preNodes.get(j);
            PreNode existingNode = parentNode.getChild(actualNode.getCode());

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
    private void processOLAPHierarchyRow(List<PreNode> preNodes, PreDimension hierarchy) {
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

            PreNode actulaNode = preNodes.get(0);
            PreNode root = hierarchy.getRoot();
            for (int rowId : actulaNode.getFactTableRowIds().toArray()) {
                root.addFactTableRowId(rowId);
            }
        } else {
            PreNode parentNode = hierarchy.getRoot();
            PreNode actualNode = null;
            for (int j = 0; j < lastNonAggregetedNodeIdx; j++) {
                actualNode = preNodes.get(j);
                if (!parentNode.hasChild(actualNode.getCode())) {
                    actualNode.getFactTableRowIds().clear();
                    parentNode.addNewChild(actualNode);
                }

                parentNode = parentNode.getChild(actualNode.getCode());
            }
            actualNode = preNodes.get(lastNonAggregetedNodeIdx);
            PreNode existingNode = parentNode.getChild(actualNode.getCode());
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
     * dimenzióba sorolja.
     * @param preCube az a cube, amelybe a megkapott node-okat töltjük.
     */
    private void processDimensionRow(List<PreNode> preNodes, int[][] dimensionIndex, PreCube preCube) {

        //ez minden egyes node-hoz megmondja, hogy az melyik hierarchiához tartozik
        //szerepe majdnem megegyezik a dimensionIndex-vel, de annak 2x annyi sora van, 
        //mert egy node  két oszlopból áll a ResultSet-ben
        int[] nodeIndexes = preCube.getNodeIndexes();

        List<PreNode> hierNodes = new ArrayList();

        for (int i = 0; i < preNodes.size(); i++) {

            int hierIdx = nodeIndexes[i];

            while ((i < preNodes.size()) && (hierIdx == nodeIndexes[i])) {
                hierNodes.add(preNodes.get(i));
                i++;
            }
            processHierarchyRow(hierNodes, preCube.getDimension(hierIdx));

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
