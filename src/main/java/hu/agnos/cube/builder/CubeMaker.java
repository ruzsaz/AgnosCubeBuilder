/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder;

import hu.agnos.cube.builder.service.Step1;
import hu.agnos.cube.builder.service.Step2;
import hu.agnos.cube.builder.service.Step3;
import hu.agnos.cube.builder.service.Step4;
import hu.agnos.cube.builder.service.Step5;
import hu.agnos.cube.Cube;
import hu.agnos.cube.builder.entity.pre.PreCube;
import hu.agnos.cube.builder.entity.sql.SqlCube;
import hu.agnos.cube.measure.AbstractMeasure;
import hu.agnos.cube.specification.entity.CubeSpecification;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author parisek
 */
public class CubeMaker {

    private static final Logger logger = LoggerFactory.getLogger(CubeMaker.class);

    public static Cube createCube(Statement statement, String cubeUniqueName, String sourceTableName, CubeSpecification xmlCube) throws SQLException, ClassNotFoundException {

        logger.debug("cube building started");
        Cube cube = (new Step1()).createCubeWithMeta(xmlCube, cubeUniqueName, sourceTableName);
        SqlCube sqlCube = (new Step2()).getSQLCube(cube, xmlCube.getSourceDBDriver(), sourceTableName);
        
        logger.debug("create precube");
        PreCube preCube = (new Step3()).getPreCube(sqlCube, cubeUniqueName, xmlCube);
        
//        System.out.println("preCube: " + preCube);
        logger.debug("load cube");

        (new Step4()).postProcessPreCube(preCube);
//        preCube.printer();

        logger.debug("convert preCube to cube");
        (new Step5()).converPreCube2Cube(cube, preCube);
//
        
        logger.debug("finished");
        return cube;

    }
}
