/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.builder.entity.pre.PreCube;

/**
 *
 * @author parisek
 */
public class Step4 {

    public void postProcessPreCube(PreCube preCube) {
        preCube.postProcess();
    }

}
