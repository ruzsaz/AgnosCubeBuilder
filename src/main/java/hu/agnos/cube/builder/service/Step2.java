/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Level;
import java.util.List;
import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.builder.entity.sql.SqlDmension;
import hu.agnos.cube.builder.entity.sql.SqlLevel;
import hu.agnos.cube.builder.entity.sql.SqlMeasure;
import hu.agnos.cube.measure.AbstractMeasure;

/**
 *
 * @author parisek
 */
public class Step2 {

    public SqlCube getSQLCube(Cube cube, String sourceDBDriver, String sourceTableName) {
        SqlCube sqlCube = new SqlCube(sourceTableName, sourceDBDriver, cube.getType());

        for (Dimension dim : cube.getDimensions()) {
//            SqlDimension sqlDim = new SqlDimension();

            boolean isOfflineCalculated = dim.isOfflineCalculated();

            SqlDmension sqlHier = new SqlDmension(dim.getName(), isOfflineCalculated);

            List<Level> levels = dim.getLevels();
            for (int levelIdx = 1; levelIdx < levels.size(); levelIdx++) {
                Level level = levels.get(levelIdx);
                SqlLevel sqlLevel = new SqlLevel(level.getCodeColumnName(), level.getNameColumnName());
                sqlHier.addLevel(sqlLevel);
            }
//            sqlDim.findDimensionByIdx(sqlHier);
//
////            sqlDim.handleDuplicates();
//            sqlCube.findDimensionByIdx(sqlDim);
            sqlCube.addDimesion(sqlHier);
        }

        for (AbstractMeasure measure : cube.getMeasures()) {
            if (!measure.isCalculated() ) {
                sqlCube.addMeasure(new SqlMeasure(measure.getName(), measure.getType()));
            }
        }
        initSqlCube(sqlCube);
        return sqlCube;
    }

    private void initSqlCube(SqlCube sqlCube) {
        initDimensionIndex(sqlCube);
    }

    private void initDimensionIndex(SqlCube sqlCube) {
        sqlCube.setDimensionIndex(new int[sqlCube.getDimensionColumnCount()][]);
        int columnCnt = 0;
        for (int dimIdx = 0; dimIdx < sqlCube.getDimensions().size(); dimIdx++) {
            SqlDmension dim = sqlCube.getDimensions().get(dimIdx);
            for (int levelIdx = 0; levelIdx < dim.getLevels().size(); levelIdx++) {

                int[] oneRow = new int[]{dimIdx, levelIdx};
                sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                columnCnt++;

                sqlCube.getDimensionIndex()[columnCnt] = oneRow;
                columnCnt++;
            }
        }
    }
}
