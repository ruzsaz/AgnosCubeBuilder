/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.ClassicalCube;
import hu.agnos.cube.CountDistinctCube;
import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Level;
import hu.agnos.cube.extraCalculation.PostCalculation;
import hu.agnos.cube.measure.CalculatedMeasure;
import hu.agnos.cube.measure.Measure;
import hu.agnos.cube.specification.entity.CubeSpecification;
import hu.agnos.cube.specification.entity.DimensionSpecification;
import hu.agnos.cube.specification.entity.LevelSpecification;
import hu.agnos.cube.specification.entity.MeasureSpecification;
import hu.agnos.cube.specification.entity.PostCalculationSpecification;
import hu.agnos.cube.measure.AbstractMeasure;
import hu.agnos.cube.measure.VirtualMeasure;
import hu.agnos.cube.specification.entity.MeasureType;

/**
 *
 * @author parisek
 */
public class Step1 {

    public Cube createCubeWithMeta(CubeSpecification xmlCube, String cubeUniqueName, String sourceTableName) {
        Cube cube = null;
        if (xmlCube.getType().equals(MeasureType.COUNT_DISTINCT.getType())) {
            cube = new CountDistinctCube(cubeUniqueName, xmlCube.getType());
        } else {
            cube = new ClassicalCube(cubeUniqueName, xmlCube.getType());
        }
        loadMeasure(cube, xmlCube);
        loadHierarchy(cube, xmlCube);
        loadPostCalculation(cube, xmlCube);

        return cube;
    }

    private static void loadMeasure(Cube cube, CubeSpecification xmlCube) {
        for (MeasureSpecification xmlMeasure : xmlCube.getMeasures()) {
            String uniqueName = xmlMeasure.getUniqueName();

            String calculatedFormula = xmlMeasure.getCalculatedFormula();

            if (xmlMeasure.isCalculatedMeasure()) {
                CalculatedMeasure calculatedMeasure = new CalculatedMeasure(uniqueName, MeasureType.valueOf("CALCULATED").getType(), calculatedFormula);
                cube.addMeasure(calculatedMeasure);
            } else if (xmlMeasure.isClassical()) {

                Measure measure = new Measure(uniqueName, MeasureType.valueOf("CLASSICAL").getType());
                cube.addMeasure(measure);
            } else if (xmlMeasure.getType().equals(MeasureType.valueOf("COUNT_DISTINCT").getType())) {
                VirtualMeasure virtualMeasure = new VirtualMeasure(uniqueName, xmlMeasure.getType());
                cube.addMeasure(virtualMeasure);
            }

        }
    }

    private static void loadHierarchy(Cube cube, CubeSpecification xmlCube) {
        int dimIdx = 0;

        for (DimensionSpecification xmlDimension : xmlCube.getDimensions()) {
            String dimensionName = xmlDimension.getUniqueName();

            boolean isOfflineCalculated = xmlDimension.isOfflineCalculated();
            Dimension dim = new Dimension(dimensionName, isOfflineCalculated);
            for (LevelSpecification xmlLevel : xmlDimension.getLevels()) {

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

    private static void loadPostCalculation(Cube cube, CubeSpecification xmlCube) {
        for (PostCalculationSpecification calculation : xmlCube.getExtraCalculations()) {
            AbstractMeasure measure = cube.getMeasureByName(calculation.getMeasureName());
            Dimension dimension = cube.getDimensionByName(calculation.getDimensionName());
            cube.getPostCalculations().add(new PostCalculation(calculation.getType(), measure, dimension));
        }
    }
}
