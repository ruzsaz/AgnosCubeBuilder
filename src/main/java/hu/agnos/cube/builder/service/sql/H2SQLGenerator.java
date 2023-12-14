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

/**
 *
 * @author parisek
 */
public class H2SQLGenerator extends SQLGenerator {

    public String getDropSQL(String prefix, String destinationTableName) {
        StringBuilder result = new StringBuilder("DROP TABLE ");
        result.append(getFullyQualifiedTableNameWithPrefix(prefix, destinationTableName));
        return result.toString();
    }

    public static String getH2DropIndexSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    public String getCreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder result = new StringBuilder("CREATE TABLE ");

        return result.substring(0, result.length() - 2) + ")";

    }

   
    public String getCreateIndexSQL(String prefix, SqlCube sqlCube) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getLoadSQLSubSelectColumnList(CubeSpecification xmlCube) {
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
                    dimensionColumnList.add(1, level.getNameColumnName());

                }
            }
        }
//        }

        for (String column : dimensionColumnList) {
            querySQLBuilder.append(" nvl(to_char(").append(column).append("), 'N/A') ").append(column).append(", ");
        }
        for (String column : xmlCube.getDistinctClassicalMeasureNameList()) {
            querySQLBuilder.append(" nvl(").append(column).append(",0) ").append(column).append(", ");
        }
        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public String getDropIndexSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    @Override
    public String getImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
