/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Level;
import hu.agnos.cube.measure.CalculatedMeasure;
import hu.agnos.cube.measure.Measure;
import hu.agnos.cube.measure.Measures;
import hu.agnos.cube.specification.entity.CubeSpecification;
import hu.agnos.cube.specification.entity.HierarchySpecification;
import hu.agnos.cube.specification.entity.LevelSpecification;
import hu.agnos.cube.specification.entity.MeasureSpecification;

/**
 *
 * @author parisek
 */
public class Step1 {

    public Cube createCubeWithMeta(CubeSpecification xmlCube, String cubeUniqueName, String sourceTableName) {
        Cube cube = new Cube(cubeUniqueName);
//        cube.setSourceTableName(sourceTableName);

        loadMeasure(cube, xmlCube);
        loadHierarchy(cube, xmlCube);

        return cube;
    }

    private static void loadMeasure(Cube cube, CubeSpecification xmlCube) {
        Measures measures = new Measures();
        cube.setMeasures(measures);
        for (MeasureSpecification xmlMeasure : xmlCube.getMeasures()) {
            String uniqueName = xmlMeasure.getUniqueName();
            String calculatedFormula = xmlMeasure.getCalculatedFormula();
            if (calculatedFormula == null) {
                Measure measure = new Measure(uniqueName);
                cube.getMeasures().addMember(measure);
            } else {
                CalculatedMeasure calculatedMeasure = new CalculatedMeasure(uniqueName, calculatedFormula);
                cube.getMeasures().addMember(calculatedMeasure);
            }
        }
    }

    private static void loadHierarchy(Cube cube, CubeSpecification xmlCube) {
        int dimIdx = 0;

        for (HierarchySpecification xmlHierarchy : xmlCube.getHierarchies()) {
            String hierarchyName = xmlHierarchy.getUniqueName();

            boolean isOfflineCalculated = xmlHierarchy.isOfflineCalculated();
            Dimension dim = new Dimension(hierarchyName, isOfflineCalculated);
            for (LevelSpecification xmlLevel : xmlHierarchy.getLevels()) {

                String levelName = xmlLevel.getUniqueName();
                String codeColumnName = xmlLevel.getCodeColumnName();
                String nameColumnName = xmlLevel.getNameColumnName();
                int depth = xmlLevel.getDepth();
                Level level = new Level(depth, levelName, null, codeColumnName, nameColumnName);
                dim.addLevel(level);
            }
            cube.addDimension(dimIdx, dim);
            dimIdx++;
        }
    }

}
