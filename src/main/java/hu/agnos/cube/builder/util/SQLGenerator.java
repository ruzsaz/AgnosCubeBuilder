/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.util;

import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.builder.entity.sql.SqlDimension;
import hu.agnos.cube.builder.entity.sql.SqlHierarchy;
import hu.agnos.cube.builder.entity.sql.SqlLevel;
import hu.agnos.cube.builder.entity.sql.SqlMeasure;
import hu.agnos.cube.specification.entity.CubeSpecification;
import hu.agnos.cube.specification.entity.DimensionSpecification;
import hu.agnos.cube.specification.entity.LevelSpecification;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author parisek
 */
public class SQLGenerator {

    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final String SAP_DRIVER = "com.sap.db.jdbc.Driver";

    public static String getDropSQL(String prefix, String destinationTableName, String driver) {
        StringBuilder result = new StringBuilder("DROP TABLE ");
        result.append(getFullyQualifiedTableNameWithPrefix(prefix, destinationTableName));
        if (driver.equals(ORACLE_DRIVER)) {
            result.append(" CASCADE CONSTRAINTS PURGE");
        }
        return result.toString();
    }

    public static String getImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        String result = null;
        switch (sqlCube.getSourceDBDriver()) {
            case ORACLE_DRIVER:
                result = getOracleImportSQL(prefix, sqlCube, from, to);
                break;
            case H2_DRIVER:
                result = getH2ImporteSQL(prefix, sqlCube, from, to);
                break;
            case SQLSERVER_DRIVER:
                result = getSQLServerImportSQL(prefix, sqlCube, from, to);
                break;
            case POSTGRES_DRIVER:
                result = getPostgreImportSQL(prefix, sqlCube, from, to);
                break;
            case SAP_DRIVER:
                result = getSAPImportSQL(prefix, sqlCube, from, to);
                break;
            default:
        }
        return result;
    }

    private static String getPostgreImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        StringBuilder selectStatement = new StringBuilder("select ");
//        StringBuilder orderStatement = new StringBuilder(" order by ");
        int columnCnt = 0;
        for (SqlDimension dim : sqlCube.getDimensions()) {
            for (SqlHierarchy hier : dim.getHierarchies()) {
                for (SqlLevel level : hier.getLevels()) {
                    columnCnt++;
                }
            }
        }
        columnCnt *= 2;

        sqlCube.setDimensionIndex(new int[columnCnt][]);

        columnCnt = 0;
        for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
            SqlDimension dim = sqlCube.getDimension(dimIdx);
            for (int hierIdx = 0; hierIdx < dim.getHierarchies().size(); hierIdx++) {
                SqlHierarchy hier = dim.getHierarchies().get(hierIdx);
                for (int levelIdx = 0; levelIdx < hier.getLevels().size(); levelIdx++) {
                    SqlLevel level = hier.getLevels().get(levelIdx);

                    //code
                    String levelCode = level.getCode();
                    if (levelCode.contains("#")) {
                        String tempLevelCode = levelCode;
                        tempLevelCode = tempLevelCode.replaceAll("#", " || '.' || ");
                        String[] levelCodeSegment = levelCode.split("#");
                        String lastLevelCode = levelCodeSegment[levelCodeSegment.length - 1];

                        if (hier.isOLAP()) {
                            String caseClause = "case\n"
                                    + "    when " + lastLevelCode + " like 'All' then 'All'\n"
                                    + "    else " + tempLevelCode + "\n"
                                    + "end";
                            tempLevelCode = caseClause;
                        }

                        selectStatement.append(tempLevelCode);

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {
                            selectStatement.append(" as ").append(level.getCodeAlias());
                        } else {
                            selectStatement.append(" as ").append("COM_").append(lastLevelCode);
                        }

                        selectStatement.append(", ");
//                        orderStatement.append(tempLevelCode).append(", ");

                    } else {
                        selectStatement.append(level.getCode());

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {

                            selectStatement.append(" as ").append(level.getCodeAlias());
                        }
                        selectStatement.append(", ");
//                        orderStatement.append(level.getCode()).append(", ");
                    }

                    //name
                    selectStatement.append(level.getName());

                    int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                    sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                    columnCnt++;

                    if (level.hasNameAlias()) {
                        selectStatement.append(" as ").append(level.getNameAlias());
                    }
                    selectStatement.append(", ");
                }
            }
        }

        for (SqlMeasure measure : sqlCube.getMeasures()) {
            selectStatement.append(measure.getName()).append(", ");
//            orderStatement.append(measure.getName()).append(", ");
        }
        StringBuilder result = new StringBuilder();

        result.append(selectStatement.substring(0, selectStatement.length() - 2)).append('\n');

        result.append("from ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append('\n');
        result.append("where ORDERID >= ").append(from).append(" and ORDERID < ").append(to).append('\n');
        result.append("order by ORDERID");
        return result.toString();
    }

    public static String getCreateSQL(String prefix, SqlCube sqlCube) {
        String result = null;
        switch (sqlCube.getSourceDBDriver()) {
            case ORACLE_DRIVER:
                result = getOracleCreateSQL(prefix, sqlCube);
                break;
            case H2_DRIVER:
                result = getH2CreateSQL(prefix, sqlCube);
                break;
            case SQLSERVER_DRIVER:
                result = getSQLServerCreateSQL(prefix, sqlCube);
                break;
            case POSTGRES_DRIVER:
                result = getPostgreCreateSQL(prefix, sqlCube);
                break;
            case SAP_DRIVER:
                result = getSAPCreateSQL(prefix, sqlCube);
                break;
            default:
        }
        return result;
    }

    public static String getDropIndexSQL(SqlCube sqlCube) {
        String result = null;
        switch (sqlCube.getSourceDBDriver()) {
            case ORACLE_DRIVER:
                result = getDropIndexSQL(sqlCube);
                break;
            case H2_DRIVER:
                result = getH2DropIndexSQL(sqlCube);
                break;
            case SQLSERVER_DRIVER:
                result = getSQLServerDropIndexSQL(sqlCube);
                break;
            case POSTGRES_DRIVER:
                result = getPostgreDropIndexSQL(sqlCube);
                break;
            case SAP_DRIVER:
                result = getDropIndexCreateSQL(sqlCube);
                break;
            default:
        }
        return result;
    }

    private static String getH2DropIndexSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    private static String getSQLServerDropIndexSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    private static String getPostgreDropIndexSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    private static String getDropIndexCreateSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    public static String getCreateIndexSQL(String prefix, SqlCube sqlCube) {
        String result = null;
        switch (sqlCube.getSourceDBDriver()) {
            case ORACLE_DRIVER:
                result = getOracleCreateIndexSQL(prefix, sqlCube);
                break;
            case H2_DRIVER:
                result = getH2CreateIndexSQL(prefix, sqlCube);
                break;
            case SQLSERVER_DRIVER:
                result = getSQLServerCreateIndexSQL(prefix, sqlCube);
                break;
            case POSTGRES_DRIVER:
                result = getPostgreCreateIndexSQL(prefix, sqlCube);
                break;
            case SAP_DRIVER:
                result = getSAPCreateIndexSQL(prefix, sqlCube);
                break;
            default:
        }
        return result;
    }

    private static String getSAPCreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder result = new StringBuilder("CREATE TABLE ");

        return result.substring(0, result.length() - 2) + ")";

    }

    private static String getSQLServerCreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder selectStatement = new StringBuilder();
        StringBuilder orderStatement = new StringBuilder(" order by ");
        int columnCnt = 0;
        for (SqlDimension dim : sqlCube.getDimensions()) {
            for (SqlHierarchy hier : dim.getHierarchies()) {
                for (SqlLevel level : hier.getLevels()) {
                    columnCnt++;
                }
            }
        }
        columnCnt *= 2;

        sqlCube.setDimensionIndex(new int[columnCnt][]);

        columnCnt = 0;
        for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
            SqlDimension dim = sqlCube.getDimension(dimIdx);
            for (int hierIdx = 0; hierIdx < dim.getHierarchies().size(); hierIdx++) {
                SqlHierarchy hier = dim.getHierarchies().get(hierIdx);
                for (int levelIdx = 0; levelIdx < hier.getLevels().size(); levelIdx++) {
                    SqlLevel level = hier.getLevels().get(levelIdx);

                    //code
                    String levelCode = level.getCode();
                    if (levelCode.contains("#")) {
                        String tempLevelCode = levelCode;
                        tempLevelCode = tempLevelCode.replaceAll("#", " || '.' || ");
                        String[] levelCodeSegment = levelCode.split("#");
                        String lastLevelCode = levelCodeSegment[levelCodeSegment.length - 1];

                        if (hier.isOLAP()) {
                            String caseClause = "case\n"
                                    + "    when " + lastLevelCode + " like 'All' then 'All'\n"
                                    + "    else " + tempLevelCode + "\n"
                                    + "end";
                            tempLevelCode = caseClause;
                        }

                        selectStatement.append(tempLevelCode.toUpperCase());

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {
                            selectStatement.append(" as ").append(level.getCodeAlias().toUpperCase());
                        } else {
                            selectStatement.append(" as ").append("COM_").append(lastLevelCode.toUpperCase());
                        }

                        selectStatement.append(", ");
                        orderStatement.append(tempLevelCode.toUpperCase()).append(", ");

                    } else {
                        selectStatement.append(level.getCode().toUpperCase());

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {

                            selectStatement.append(" as ").append(level.getCodeAlias().toUpperCase());
                        }
                        selectStatement.append(", ");
                        orderStatement.append(level.getCode().toUpperCase()).append(", ");
                    }

                    //name
                    selectStatement.append(level.getName().toUpperCase());

                    int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                    sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                    columnCnt++;

                    if (level.hasNameAlias()) {
                        selectStatement.append(" as ").append(level.getNameAlias().toUpperCase());
                    }
                    selectStatement.append(", ");
                }
            }
        }

        for (SqlMeasure measure : sqlCube.getMeasures()) {
            selectStatement.append(measure.getName().toUpperCase()).append(", ");
            orderStatement.append(measure.getName().toUpperCase()).append(", ");
        }

        StringBuilder result = new StringBuilder("select row_number() over ( ");
        result.append(orderStatement.substring(0, orderStatement.length() - 2)).append(") as orderid,\n");
        result.append(selectStatement.substring(0, selectStatement.length() - 2)).append('\n');
        result.append("into ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append('\n');
        result.append("from ").append(sqlCube.getSourceTableName()).append('\n');
        result.append(orderStatement.substring(0, orderStatement.length() - 2));
        return result.toString();

    }

    private static String getOracleCreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder result = new StringBuilder("CREATE TABLE ");

        return result.substring(0, result.length() - 2) + ")";

    }

    private static String getPostgreCreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder selectStatement = new StringBuilder();
        StringBuilder orderStatement = new StringBuilder(" order by ");
        int columnCnt = 0;
        for (SqlDimension dim : sqlCube.getDimensions()) {
            for (SqlHierarchy hier : dim.getHierarchies()) {
                for (SqlLevel level : hier.getLevels()) {
                    columnCnt++;
                }
            }
        }
        columnCnt *= 2;

        sqlCube.setDimensionIndex(new int[columnCnt][]);

        columnCnt = 0;
        for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
            SqlDimension dim = sqlCube.getDimension(dimIdx);
            for (int hierIdx = 0; hierIdx < dim.getHierarchies().size(); hierIdx++) {
                SqlHierarchy hier = dim.getHierarchies().get(hierIdx);
                for (int levelIdx = 0; levelIdx < hier.getLevels().size(); levelIdx++) {
                    SqlLevel level = hier.getLevels().get(levelIdx);

                    //code
                    String levelCode = level.getCode();
                    if (levelCode.contains("#")) {
                        String tempLevelCode = levelCode;
                        tempLevelCode = tempLevelCode.replaceAll("#", " || '.' || ");
                        String[] levelCodeSegment = levelCode.split("#");
                        String lastLevelCode = levelCodeSegment[levelCodeSegment.length - 1];

                        if (hier.isOLAP()) {
                            String caseClause = "case\n"
                                    + "    when " + lastLevelCode + " like 'All' then 'All'\n"
                                    + "    else " + tempLevelCode + "\n"
                                    + "end";
                            tempLevelCode = caseClause;
                        }

                        selectStatement.append(tempLevelCode.toUpperCase());

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {
                            selectStatement.append(" as ").append(level.getCodeAlias().toUpperCase());
                        } else {
                            selectStatement.append(" as ").append("COM_").append(lastLevelCode.toUpperCase());
                        }

                        selectStatement.append(", ");
                        orderStatement.append(tempLevelCode.toUpperCase()).append(", ");

                    } else {
                        selectStatement.append(level.getCode().toUpperCase());

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {

                            selectStatement.append(" as ").append(level.getCodeAlias().toUpperCase());
                        }
                        selectStatement.append(", ");
                        orderStatement.append(level.getCode().toUpperCase()).append(", ");
                    }

                    //name
                    selectStatement.append(level.getName().toUpperCase());

                    int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                    sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                    columnCnt++;

                    if (level.hasNameAlias()) {
                        selectStatement.append(" as ").append(level.getNameAlias().toUpperCase());
                    }
                    selectStatement.append(", ");
                }
            }
        }

        for (SqlMeasure measure : sqlCube.getMeasures()) {
            selectStatement.append(measure.getName().toUpperCase()).append(", ");
            orderStatement.append(measure.getName().toUpperCase()).append(", ");
        }

        StringBuilder result = new StringBuilder("create table ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append(" as\n select row_number() over ( ");
        result.append(orderStatement.substring(0, orderStatement.length() - 2)).append(") as orderid,\n");
        result.append(selectStatement.substring(0, selectStatement.length() - 2)).append('\n');
        result.append(" from ").append(sqlCube.getSourceTableName()).append('\n');
        result.append(orderStatement.substring(0, orderStatement.length() - 2));
        return result.toString();
    }

    private static String getH2CreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder result = new StringBuilder("CREATE TABLE ");

        return result.substring(0, result.length() - 2) + ")";

    }

    public static String getLoadSQL(String prefix, CubeSpecification xmlCube, String destinationTableName, String sourceTableName) {
        StringBuilder selectQuerySQLBuilder = new StringBuilder("INSERT INTO ");

        selectQuerySQLBuilder.append(getFullyQualifiedTableNameWithPrefix(prefix, destinationTableName)).append(" (");
        selectQuerySQLBuilder.append(getLoadSQLInsertColumnList(xmlCube));
        selectQuerySQLBuilder.append(")\nSELECT ").append(getLoadSQLSelectColumnList(xmlCube));
        selectQuerySQLBuilder.append("\nFROM ");
        selectQuerySQLBuilder.append("\n\t(\n").append(getLoadSQLSubSelect(prefix, xmlCube, sourceTableName)).append("\n\t) as foo\n");
        selectQuerySQLBuilder.append("\nGROUP BY ").append(getLoadSQLGroupBYColumnList(xmlCube));
        String result = selectQuerySQLBuilder.toString();
//        System.out.println("result:\n" + result);
        return result;
    }

    public static String getLoadSQLSubSelect(String prefix, CubeSpecification xmlCube, String sourceTableName) {
        StringBuilder selectQuerySQLBuilder = new StringBuilder("\t\tSELECT ");
        selectQuerySQLBuilder.append(getLoadSQLSubSelectColumnList(xmlCube));

        selectQuerySQLBuilder.append("\n\t\tFROM ");

        selectQuerySQLBuilder.append(getFullyQualifiedTableNameWithPrefix(prefix, sourceTableName));

        String result = selectQuerySQLBuilder.toString();
//        System.out.println("sub result: " + result);
        return result;
    }

    public static String getLoadSQLInsertColumnList(CubeSpecification xmlCube) {
        List<String> dimensionColumnList = new ArrayList<>();
        StringBuilder querySQLBuilder = new StringBuilder();
//        for (XMLDimension dim : xmlCube.getDimensions()) {
            for (DimensionSpecification dim : xmlCube.getDimensions()) {
                for (LevelSpecification level : dim.getLevels()) {
                    if (!dimensionColumnList.contains(level.getCodeColumnSourceName())) {
                        dimensionColumnList.add(level.getCodeColumnSourceName());
                    }
                    if (!level.getNameColumnName().equals(level.getCodeColumnSourceName()) && !dimensionColumnList.contains(level.getNameColumnName())) {
                        dimensionColumnList.add(level.getNameColumnName());
                    }
                }
            }
//        }

        for (String column : dimensionColumnList) {
            querySQLBuilder.append(column).append(", ");
        }

        for (String column : xmlCube.getDistinctMeasureColumnList()) {
            querySQLBuilder.append(column).append(", ");
        }

        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public static String getLoadSQLSelectColumnList(CubeSpecification xmlCube) {
        List<String> dimensionColumnList = new ArrayList<>();
        StringBuilder querySQLBuilder = new StringBuilder();
//        for (XMLDimension dim : xmlCube.getDimensions()) {
            for (DimensionSpecification dim : xmlCube.getDimensions()) {
                for (LevelSpecification level : dim.getLevels()) {

                    String columnName = level.getCodeColumnSourceName();

                    if (!dimensionColumnList.contains(columnName)) {
                        dimensionColumnList.add(columnName);
                    }
                    if (!level.getNameColumnName().equals(level.getCodeColumnSourceName()) && !dimensionColumnList.contains(level.getNameColumnName())) {
                        dimensionColumnList.add(level.getNameColumnName());
                    }
                }
            }
//        }

        for (String column : dimensionColumnList) {
            querySQLBuilder.append(column).append(", ");
        }

        for (String column : xmlCube.getDistinctMeasureColumnList()) {
            querySQLBuilder.append("SUM(").append(column).append("), ");
        }

        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public static String getLoadSQLSubSelectColumnList(CubeSpecification xmlCube) {
        List<String> dimensionColumnList = new ArrayList<>();
        StringBuilder querySQLBuilder = new StringBuilder();
//        for (XMLDimension dim : xmlCube.getDimensions()) {
            for (DimensionSpecification dim : xmlCube.getDimensions()) {
                for (LevelSpecification level : dim.getLevels()) {

                    String columnName = level.getCodeColumnSourceName();

                    if (!dimensionColumnList.contains(columnName)) {

                        dimensionColumnList.add(columnName);

                    }
                    if (!level.getNameColumnName().equals(level.getCodeColumnSourceName()) && !dimensionColumnList.contains(level.getNameColumnName())) {
                        dimensionColumnList.add(1, level.getNameColumnName());

                    }
                }
            }
//        }

        switch (xmlCube.getSourceDBDriver()) {
            case ORACLE_DRIVER:
                for (String column : dimensionColumnList) {
                    querySQLBuilder.append(" nvl(to_char(").append(column).append("), 'N/A') ").append(column).append(", ");
                }
                break;
            case H2_DRIVER:
                for (String column : dimensionColumnList) {
                    querySQLBuilder.append(" nvl(to_char(").append(column).append("), 'N/A') ").append(column).append(", ");
                }
                break;
            case SQLSERVER_DRIVER:
                for (String column : dimensionColumnList) {
                    querySQLBuilder.append(" nvl(to_char(").append(column).append("), 'N/A') ").append(column).append(", ");
                }
                break;
            case POSTGRES_DRIVER:
                //TODO: mi van ha nem char? nincs to_char!!!
                for (String column : dimensionColumnList) {
                    querySQLBuilder.append(" coalesce(").append(column).append(", 'N/A') ").append(column).append(", ");
                }
                break;
            case SAP_DRIVER:
                for (String column : dimensionColumnList) {
                    querySQLBuilder.append(" nvl(to_char(").append(column).append("), 'N/A') ").append(column).append(", ");
                }
                break;
            default:
        }

        switch (xmlCube.getSourceDBDriver()) {
            case ORACLE_DRIVER:
                for (String column : xmlCube.getDistinctMeasureColumnList()) {
                    querySQLBuilder.append(" nvl(").append(column).append(",0) ").append(column).append(", ");
                }
                break;
            case H2_DRIVER:
                for (String column : xmlCube.getDistinctMeasureColumnList()) {
                    querySQLBuilder.append(" nvl(").append(column).append(",0) ").append(column).append(", ");
                }
                break;
            case SQLSERVER_DRIVER:
                for (String column : xmlCube.getDistinctMeasureColumnList()) {
                    querySQLBuilder.append(" nvl(").append(column).append(",0) ").append(column).append(", ");
                }
                break;
            case POSTGRES_DRIVER:
                //TODO: mi van ha nem char? nincs to_char!!!
                for (String column : xmlCube.getDistinctMeasureColumnList()) {
                    querySQLBuilder.append(" coalesce(").append(column).append(",0) ").append(column).append(", ");
                }
                break;
            case SAP_DRIVER:
                for (String column : xmlCube.getDistinctMeasureColumnList()) {
                    querySQLBuilder.append(" nvl(").append(column).append(",0) ").append(column).append(", ");
                }
                break;
            default:
        }

        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public static String getLoadSQLGroupBYColumnList(CubeSpecification xmlCube) {
        List<String> dimensionColumnList = new ArrayList<>();
        StringBuilder querySQLBuilder = new StringBuilder();
//        for (XMLDimension dim : xmlCube.getDimensions()) {
            for (DimensionSpecification dim : xmlCube.getDimensions()) {
                for (LevelSpecification level : dim.getLevels()) {

                    String columnName = level.getCodeColumnSourceName();
                    if (!dimensionColumnList.contains(columnName)) {
                        dimensionColumnList.add(columnName);
                    }
                    if (!level.getNameColumnName().equals(level.getCodeColumnSourceName()) && !dimensionColumnList.contains(level.getNameColumnName())) {
                        dimensionColumnList.add(level.getNameColumnName());
                    }
                }
            }
//        }

        for (String column : dimensionColumnList) {
            querySQLBuilder.append(column).append(", ");
        }
        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public static String getSelectQueryToReload(String prefix, CubeSpecification xmlCube, String destinationTableName, String sourceTableName) {
        StringBuilder insertQuerySQLBuilder = new StringBuilder("INSERT INTO ");

        insertQuerySQLBuilder.append(getFullyQualifiedTableNameWithPrefix(prefix, destinationTableName)).append(" (");

        StringBuilder selectQuerySQLBuilder = new StringBuilder("SELECT ");

        for (String column : xmlCube.getDistinctDimensionColumnList()) {
            selectQuerySQLBuilder.append(column).append(", ");
            insertQuerySQLBuilder.append(column).append(", ");

        }

        List<String> measureColumnList = xmlCube.getDistinctMeasureColumnList();
        for (String column : measureColumnList) {
            selectQuerySQLBuilder.append(column).append(", ");
            insertQuerySQLBuilder.append(column).append(", ");
        }
        selectQuerySQLBuilder = new StringBuilder(selectQuerySQLBuilder.substring(0, selectQuerySQLBuilder.length() - 2));
        insertQuerySQLBuilder = new StringBuilder(insertQuerySQLBuilder.substring(0, insertQuerySQLBuilder.length() - 2));
        insertQuerySQLBuilder.append(")\n");
        selectQuerySQLBuilder.append(" FROM ");
        if (prefix != null) {
            selectQuerySQLBuilder.append(prefix);
        }
        selectQuerySQLBuilder.append(destinationTableName);
        insertQuerySQLBuilder.append(selectQuerySQLBuilder.toString());
        return insertQuerySQLBuilder.toString();
    }

    private static String getOracleImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String getH2ImporteSQL(String prefix, SqlCube sqlCube, long from, long to) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String getSQLServerImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        StringBuilder selectStatement = new StringBuilder("select ");
//        StringBuilder orderStatement = new StringBuilder(" order by ");
        int columnCnt = 0;
        for (SqlDimension dim : sqlCube.getDimensions()) {
            for (SqlHierarchy hier : dim.getHierarchies()) {
                for (SqlLevel level : hier.getLevels()) {
                    columnCnt++;
                }
            }
        }
        columnCnt *= 2;

        sqlCube.setDimensionIndex(new int[columnCnt][]);

        columnCnt = 0;
        for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
            SqlDimension dim = sqlCube.getDimension(dimIdx);
            for (int hierIdx = 0; hierIdx < dim.getHierarchies().size(); hierIdx++) {
                SqlHierarchy hier = dim.getHierarchies().get(hierIdx);
                for (int levelIdx = 0; levelIdx < hier.getLevels().size(); levelIdx++) {
                    SqlLevel level = hier.getLevels().get(levelIdx);

                    //code
                    String levelCode = level.getCode();
                    if (levelCode.contains("#")) {
                        String tempLevelCode = levelCode;
                        tempLevelCode = tempLevelCode.replaceAll("#", " || '.' || ");
                        String[] levelCodeSegment = levelCode.split("#");
                        String lastLevelCode = levelCodeSegment[levelCodeSegment.length - 1];

                        if (hier.isOLAP()) {
                            String caseClause = "case\n"
                                    + "    when " + lastLevelCode + " like 'All' then 'All'\n"
                                    + "    else " + tempLevelCode + "\n"
                                    + "end";
                            tempLevelCode = caseClause;
                        }

                        selectStatement.append(tempLevelCode);

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {
                            selectStatement.append(" as ").append(level.getCodeAlias());
                        } else {
                            selectStatement.append(" as ").append("COM_").append(lastLevelCode);
                        }

                        selectStatement.append(", ");
//                        orderStatement.append(tempLevelCode).append(", ");

                    } else {
                        selectStatement.append(level.getCode());

                        int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        if (level.hasCodeAlias()) {

                            selectStatement.append(" as ").append(level.getCodeAlias());
                        }
                        selectStatement.append(", ");
//                        orderStatement.append(level.getCode()).append(", ");
                    }

                    //name
                    selectStatement.append(level.getName());

                    int[] oneRow = new int[]{dimIdx, hierIdx, levelIdx};
                    sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                    columnCnt++;

                    if (level.hasNameAlias()) {
                        selectStatement.append(" as ").append(level.getNameAlias());
                    }
                    selectStatement.append(", ");
                }
            }
        }

        for (SqlMeasure measure : sqlCube.getMeasures()) {
            selectStatement.append(measure.getName()).append(", ");
//            orderStatement.append(measure.getName()).append(", ");
        }
        StringBuilder result = new StringBuilder();

        result.append(selectStatement.substring(0, selectStatement.length() - 2)).append('\n');

        result.append("from ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append('\n');
        result.append("where ORDERID >= ").append(from).append(" and ORDERID < ").append(to).append('\n');
        result.append("order by ORDERID");
        return result.toString();
    }

    private static String getSAPImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String getSAPCreateIndexSQL(String prefix, SqlCube sqlCube) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String getPostgreCreateIndexSQL(String prefix, SqlCube sqlCube) {
//        CREATE UNIQUE INDEX title_idx ON films (title);

        StringBuilder result = new StringBuilder("create unique index  ORDERID_IDX on ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append("(ORDERID)");
        return result.toString();
    }

    private static String getSQLServerCreateIndexSQL(String prefix, SqlCube sqlCube) {
        StringBuilder result = new StringBuilder("create unique index  ORDERID_IDX on ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append("(ORDERID)");
        return result.toString();
    }

    private static String getH2CreateIndexSQL(String prefix, SqlCube sqlCube) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String getOracleCreateIndexSQL(String prefix, SqlCube sqlCube) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static String getFullyQualifiedTableNameWithPrefix(String prefix, String destinationTableName) {
        StringBuilder result = new StringBuilder();
        if (prefix != null) {
            if (destinationTableName.contains(".")) {
                String[] tableNameSegments = destinationTableName.split("\\.");
                for (int i = 0; i < tableNameSegments.length; i++) {
                    if (i == tableNameSegments.length - 1) {
                        result.append(getQuotedTableNameWithPrefix(prefix, tableNameSegments[i]));
                    } else {
                        result.append(tableNameSegments[i]);
                        result.append(".");
                    }
                }
            } else {
                result.append(getQuotedTableNameWithPrefix(prefix, destinationTableName));
            }
        } else {
            result.append(destinationTableName);
        }
        return result.toString();
    }

    private static String getQuotedTableNameWithPrefix(String prefix, String tableName) {
        StringBuilder result = new StringBuilder();
        if (prefix != null) {
            if (tableName.startsWith("[")) {
                result.append("[");
                result.append(prefix);
                result.append(tableName.substring(1));
            } else if (tableName.startsWith("\"")) {
                result.append("\"");
                result.append(prefix);
                result.append(tableName.substring(1));
            } else {
                result.append(prefix);
                result.append(tableName);
            }
        } else {
            result.append(tableName);
        }
        return result.toString();
    }
}
