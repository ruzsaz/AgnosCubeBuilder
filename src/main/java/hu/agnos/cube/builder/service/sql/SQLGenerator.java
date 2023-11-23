/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service.sql;

import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.specification.entity.CubeSpecification;
import hu.agnos.cube.specification.entity.DimensionSpecification;
import hu.agnos.cube.specification.entity.LevelSpecification;
import java.util.ArrayList;
import java.util.List;
import lombok.ToString;

/**
 *
 * @author parisek
 */
@ToString
public abstract class SQLGenerator {

   
    public abstract String getDropSQL(String prefix, String destinationTableName);

    public abstract String getImportSQL(String prefix, SqlCube sqlCube, long from, long to);
    
    public abstract String getCreateSQL(String prefix, SqlCube sqlCube);
    
    public abstract String getDropIndexSQL(SqlCube sqlCube);
    
    public abstract String getCreateIndexSQL(String prefix, SqlCube sqlCube) ;
       

    
    public String getLoadSQL(String prefix, CubeSpecification xmlCube, String destinationTableName, String sourceTableName) {
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

    public String getLoadSQLSubSelect(String prefix, CubeSpecification xmlCube, String sourceTableName) {
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
                    if (!dimensionColumnList.contains(level.getCodeColumnName())) {
                        dimensionColumnList.add(level.getCodeColumnName());
                    }
                    if (!level.getNameColumnName().equals(level.getCodeColumnName()) && !dimensionColumnList.contains(level.getNameColumnName())) {
                        dimensionColumnList.add(level.getNameColumnName());
                    }
                }
            }
//        }

        for (String column : dimensionColumnList) {
            querySQLBuilder.append(column).append(", ");
        }

        for (String column : xmlCube.getDistinctClassicalMeasureNameList()) {
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

                    String columnName = level.getCodeColumnName();

                    if (!dimensionColumnList.contains(columnName)) {
                        dimensionColumnList.add(columnName);
                    }
                    if (!level.getNameColumnName().equals(level.getCodeColumnName()) && !dimensionColumnList.contains(level.getNameColumnName())) {
                        dimensionColumnList.add(level.getNameColumnName());
                    }
                }
            }
//        }

        for (String column : dimensionColumnList) {
            querySQLBuilder.append(column).append(", ");
        }

        for (String column : xmlCube.getDistinctClassicalMeasureNameList()) {
            querySQLBuilder.append("SUM(").append(column).append("), ");
        }

        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public abstract String getLoadSQLSubSelectColumnList(CubeSpecification xmlCube);
    
    public String getLoadSQLGroupBYColumnList(CubeSpecification xmlCube) {
        List<String> dimensionColumnList = new ArrayList<>();
        StringBuilder querySQLBuilder = new StringBuilder();
//        for (XMLDimension dim : xmlCube.getDimensions()) {
            for (DimensionSpecification dim : xmlCube.getDimensions()) {
                for (LevelSpecification level : dim.getLevels()) {

                    String columnName = level.getCodeColumnName();
                    if (!dimensionColumnList.contains(columnName)) {
                        dimensionColumnList.add(columnName);
                    }
                    if (!level.getNameColumnName().equals(level.getCodeColumnName()) && !dimensionColumnList.contains(level.getNameColumnName())) {
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

        List<String> measureColumnList = xmlCube.getDistinctClassicalMeasureNameList();
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
