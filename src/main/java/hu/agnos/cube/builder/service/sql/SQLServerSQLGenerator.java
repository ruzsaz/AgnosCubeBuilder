/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service.sql;

import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.builder.entity.sql.SqlDmension;
import hu.agnos.cube.builder.entity.sql.SqlLevel;
import hu.agnos.cube.builder.entity.sql.SqlMeasure;
import hu.agnos.cube.specification.entity.CubeSpecification;
import hu.agnos.cube.specification.entity.DimensionSpecification;
import hu.agnos.cube.specification.entity.LevelSpecification;
import hu.agnos.cube.specification.entity.MeasureSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.ToString;

/**
 *
 * @author parisek
 */
@ToString
public class SQLServerSQLGenerator extends SQLGenerator {

    public String getDropSQL(String prefix, String destinationTableName) {
        StringBuilder result = new StringBuilder("DROP TABLE ");
        result.append(getFullyQualifiedTableNameWithPrefix(prefix, destinationTableName));
        return result.toString();
    }

    public String getLoadSQLSubSelectColumnList(CubeSpecification xmlCube) {
        List<String> dimensionColumnList = new ArrayList<>();
        StringBuilder querySQLBuilder = new StringBuilder();
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

        for (String column : dimensionColumnList) {
            querySQLBuilder.append(" nvl(to_char(").append(column).append("), 'N/A') ").append(column).append(", ");
        }
        Optional<MeasureSpecification> m = xmlCube.getCountDistinctMeasure();
        if (m.isPresent()) {
            querySQLBuilder
                    .append(m.get().getUniqueName())
                    .append(", ");
        }
        else{
            for (String column : xmlCube.getDistinctClassicalMeasureNameList()) {
                querySQLBuilder
                        .append(" nvl(")
                        .append(column)
                        .append(",0) ")
                        .append(column)
                        .append(", ");
            }
        }

        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public String getDropIndexSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    public String getCreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder selectStatement = new StringBuilder();
        StringBuilder orderStatement = new StringBuilder(" order by ");

        for (SqlDmension dim : sqlCube.getDimensions()) {
            for (SqlLevel level : dim.getLevels()) {
                //code
                selectStatement
                        .append(level.getCode().toUpperCase())
                        .append(", ");;

                orderStatement
                        .append(level.getCode()
                                .toUpperCase()).append(", ");

                //name
                selectStatement.append(level.getName().toUpperCase());

                if (level.getName().toUpperCase().equals(level.getCode().toUpperCase())) {
                    selectStatement.append(" as ").append(level.getName().toUpperCase()).append("_NAME");
                }

                selectStatement.append(", ");
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

    public String getImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        StringBuilder selectStatement = new StringBuilder("select ");
        for (SqlDmension dim : sqlCube.getDimensions()) {
            for (SqlLevel level : dim.getLevels()) {
                //code
                selectStatement
                        .append(level.getCode())
                        .append(", ");
                //name
                selectStatement
                        .append(level.getName())
                        .append(", ");
            }
        }

        for (SqlMeasure measure : sqlCube.getMeasures()) {
            selectStatement.append(measure.getName()).append(", ");
        }
        StringBuilder result = new StringBuilder();

        result.append(selectStatement.substring(0, selectStatement.length() - 2)).append('\n');

        result.append("from ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append('\n');
        result.append("where ORDERID >= ").append(from).append(" and ORDERID < ").append(to).append('\n');
        result.append("order by ORDERID");
        return result.toString();
    }

    public String getCreateIndexSQL(String prefix, SqlCube sqlCube) {
        StringBuilder result = new StringBuilder("create unique index  ORDERID_IDX on ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append("(ORDERID)");
        return result.toString();
    }

}
