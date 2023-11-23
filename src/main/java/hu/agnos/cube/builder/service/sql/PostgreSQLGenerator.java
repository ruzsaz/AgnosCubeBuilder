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
import java.util.ArrayList;
import java.util.List;
import lombok.ToString;

/**
 *
 * @author parisek
 */
@ToString
public class PostgreSQLGenerator extends SQLGenerator {

    public String getDropSQL(String prefix, String destinationTableName) {
        StringBuilder result = new StringBuilder("DROP TABLE ");
        result.append(getFullyQualifiedTableNameWithPrefix(prefix, destinationTableName));
        return result.toString();
    }

    public String getImportSQL(String prefix, SqlCube sqlCube, long from, long to) {
        StringBuilder selectStatement = new StringBuilder("select ");
//        StringBuilder orderStatement = new StringBuilder(" order by ");
        int columnCnt = 0;
//        for (SqlDimension dim : sqlCube.getDimensions()) {
            for (SqlDmension dim : sqlCube.getDimensions()) {
                for (SqlLevel level : dim.getLevels()) {
                    columnCnt++;
                }
            }
//        }
        columnCnt *= 2;

        sqlCube.setDimensionIndex(new int[columnCnt][]);

        columnCnt = 0;
//        for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
//            SqlDimension dim = sqlCube.findDimensionByIdx(dimIdx);
            for (int hierIdx = 0; hierIdx < sqlCube.getDimensions().size(); hierIdx++) {
                SqlDmension hier = sqlCube.getDimensions().get(hierIdx);
                for (int levelIdx = 0; levelIdx < hier.getLevels().size(); levelIdx++) {
                    SqlLevel level = hier.getLevels().get(levelIdx);

                    //code
                    String levelCode = level.getCode();
                    if (levelCode.contains("#")) {
                        String tempLevelCode = levelCode;
                        tempLevelCode = tempLevelCode.replaceAll("#", " || '.' || ");
                        String[] levelCodeSegment = levelCode.split("#");
                        String lastLevelCode = levelCodeSegment[levelCodeSegment.length - 1];

                        if (hier.isOfflineCalculated()) {
                            String caseClause = "case\n"
                                    + "    when " + lastLevelCode + " like 'All' then 'All'\n"
                                    + "    else " + tempLevelCode + "\n"
                                    + "end";
                            tempLevelCode = caseClause;
                        }

                        selectStatement.append(tempLevelCode);

                        int[] oneRow = new int[]{hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        selectStatement.append(" as ").append("COM_").append(lastLevelCode);

                        selectStatement.append(", ");
//                        orderStatement.append(tempLevelCode).append(", ");

                    } else {
                        selectStatement.append(level.getCode());

                        int[] oneRow = new int[]{hierIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        selectStatement.append(", ");
//                        orderStatement.append(level.getCode()).append(", ");
                    }

                    //name
                    selectStatement.append(level.getName());

                    int[] oneRow = new int[]{hierIdx, levelIdx};
                    sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                    columnCnt++;

                    selectStatement.append(", ");
                }
//            }
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

    public String getDropIndexSQL(SqlCube sqlCube) {
        return "drop index ORDERID_IDX";
    }

    public String getCreateSQL(String prefix, SqlCube sqlCube) {
        StringBuilder selectStatement = new StringBuilder();
        StringBuilder orderStatement = new StringBuilder(" order by ");
        int columnCnt = 0;
//        for (SqlDimension dim : sqlCube.getDimensions()) {
            for (SqlDmension dim : sqlCube.getDimensions()) {
                for (SqlLevel level : dim.getLevels()) {
                    columnCnt++;
                }
            }
//        }
        columnCnt *= 2;

        sqlCube.setDimensionIndex(new int[columnCnt][]);

        columnCnt = 0;
//        for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
//            SqlDimension dim = sqlCube.findDimensionByIdx(dimIdx);
            for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
                SqlDmension hier = sqlCube.getDimensions().get(dimIdx);
                for (int levelIdx = 0; levelIdx < hier.getLevels().size(); levelIdx++) {
                    SqlLevel level = hier.getLevels().get(levelIdx);

                    //code
                    String levelCode = level.getCode();
                    if (levelCode.contains("#")) {
                        String tempLevelCode = levelCode;
                        tempLevelCode = tempLevelCode.replaceAll("#", " || '.' || ");
                        String[] levelCodeSegment = levelCode.split("#");
                        String lastLevelCode = levelCodeSegment[levelCodeSegment.length - 1];

                        if (hier.isOfflineCalculated()) {
                            String caseClause = "case\n"
                                    + "    when " + lastLevelCode + " like 'All' then 'All'\n"
                                    + "    else " + tempLevelCode + "\n"
                                    + "end";
                            tempLevelCode = caseClause;
                        }

                        selectStatement.append(tempLevelCode.toUpperCase());

                        int[] oneRow = new int[]{dimIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        selectStatement.append(" as ").append("COM_").append(lastLevelCode.toUpperCase());

                        selectStatement.append(", ");
                        orderStatement.append(tempLevelCode.toUpperCase()).append(", ");

                    } else {
                        selectStatement.append(level.getCode().toUpperCase());

                        int[] oneRow = new int[]{dimIdx, levelIdx};
                        sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                        columnCnt++;

                        selectStatement.append(", ");
                        orderStatement.append(level.getCode().toUpperCase()).append(", ");
                    }

                    //name
                    selectStatement.append(level.getName().toUpperCase());

                    int[] oneRow = new int[]{dimIdx, levelIdx};
                    sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                    columnCnt++;

                    selectStatement.append(", ");
                }
            }
        //}

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

        //TODO: mi van ha nem char? nincs to_char!!!
        for (String column : dimensionColumnList) {
            querySQLBuilder.append(" coalesce(").append(column).append(", 'N/A') ").append(column).append(", ");
        }
        //TODO: mi van ha nem char? nincs to_char!!!
        for (String column : xmlCube.getDistinctClassicalMeasureNameList()) {
            querySQLBuilder.append(" coalesce(").append(column).append(",0) ").append(column).append(", ");
        }

        return querySQLBuilder.substring(0, querySQLBuilder.length() - 2);
    }

    public String getCreateIndexSQL(String prefix, SqlCube sqlCube) {
//        CREATE UNIQUE INDEX title_idx ON films (title);

        StringBuilder result = new StringBuilder("create unique index  ORDERID_IDX on ").append(getFullyQualifiedTableNameWithPrefix(prefix, sqlCube.getSourceTableName().toUpperCase())).append("(ORDERID)");
        return result.toString();
    }
    
    

}
