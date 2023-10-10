/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.molap.Cube;
import hu.agnos.molap.dimension.Dimension;
import hu.agnos.molap.dimension.Hierarchy;
import hu.agnos.molap.dimension.Level;
import hu.agnos.molap.measure.AbstractMeasure;
import hu.agnos.molap.measure.Measures;
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
    
     public SqlCube getSQLCube(Cube cube, String sourceDBDriver) {
        SqlCube sqlCube = new SqlCube(cube.getSourceTableName(), sourceDBDriver);

        List<Dimension> dimensions = cube.getDimensions();

        Measures measures = cube.getMeasures();

        for (Dimension dim : dimensions) {
            SqlDimension sqlDim = new SqlDimension();

            Hierarchy[] hierarchies = dim.getHierarchies();

            for (Hierarchy hier : hierarchies) {
                boolean isOLAP = hier.isPartitioned();

                SqlHierarchy sqlHier = new SqlHierarchy(hier.getHierarchyUniqueName(), isOLAP);

                List<Level> levels = hier.getLevels();
                for (int levelIdx = 1; levelIdx < levels.size(); levelIdx++) {
                    Level level = levels.get(levelIdx);
                    SqlLevel sqlLevel = new SqlLevel(level.getCodeColumnName(), level.getNameColumnName());
                    sqlHier.addLevel(sqlLevel);
                }
                sqlDim.addHierarchy(sqlHier);
            }

            sqlDim.handleDuplicates();
            sqlCube.addDimension(sqlDim);
        }

        for (AbstractMeasure measure : measures.getMeasures()) {
            if (!measure.isCalculatedMember()) {
                sqlCube.addMeasure(new SqlMeasure(measure.getName()));
            }
        }

        return sqlCube;
    }

}
