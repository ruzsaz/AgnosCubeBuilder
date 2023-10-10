/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.raw;

import java.util.HashMap;

/**
 *
 * @author parisek
 */
public class RawCube {
    
    public HashMap<Integer, RawDimension> dimensions;
    private RawCells preCells;

    public RawCube() {
        this.dimensions = new HashMap();
    }

    public RawCells getPreCells() {
        return preCells;
    }

    public void setPreCells(RawCells preCells) {
        this.preCells = preCells;
    }

    
    public HashMap<Integer,RawDimension> getDimensions() {
        return dimensions;
    }
    
    public RawDimension getDimension(Integer idx) {
        if(this.dimensions.containsKey(idx)){
            return this.dimensions.get(idx);
        }
        return null;
    }
    
    public void addDimension(Integer idx, RawDimension dim){
        if(! this.dimensions.containsKey(idx)){
            this.dimensions.put(idx, dim);
        }
    }
    
    public void printer(){
        for(RawDimension dim : this.dimensions.values()){
            dim.printer();
            System.out.println("\n*******************************************\n");
        }
    }
     
    public void postProcess(){
         for(RawDimension dim : this.dimensions.values()){
             dim.postProcess();
         }       
    }
}
