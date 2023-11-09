/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Level;
import hu.agnos.cube.measure.AbstractMeasure;
import java.util.List;
import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.builder.entity.sql.SqlDimension;
import hu.agnos.cube.builder.entity.sql.SqlHierarchy;
import hu.agnos.cube.builder.entity.sql.SqlLevel;
import hu.agnos.cube.builder.entity.sql.SqlMeasure;

/**
 *
 * @author parisek
 */
public class Step2 {

    public SqlCube getSQLCube(Cube cube, String sourceDBDriver, String sourceTableName) {
        SqlCube sqlCube = new SqlCube(sourceTableName, sourceDBDriver);
  
        for (Dimension dim : cube.getDimensions()) {
            SqlDimension sqlDim = new SqlDimension();

            boolean isOfflineCalculated = dim.isOfflineCalculated();

            SqlHierarchy sqlHier = new SqlHierarchy(dim.getName(), isOfflineCalculated);

            List<Level> levels = dim.getLevels();
            for (int levelIdx = 1; levelIdx < levels.size(); levelIdx++) {
                Level level = levels.get(levelIdx);
                SqlLevel sqlLevel = new SqlLevel(level.getCodeColumnName(), level.getNameColumnName());
                sqlHier.addLevel(sqlLevel);
            }
            sqlDim.addHierarchy(sqlHier);

            sqlDim.handleDuplicates();
            sqlCube.addDimension(sqlDim);
        }

        for (AbstractMeasure measure : cube.getMeasures()) {
            if (!measure.isCalculatedMember()) {
                sqlCube.addMeasure(new SqlMeasure(measure.getName()));
            }
        }

        return sqlCube;
    }

}
