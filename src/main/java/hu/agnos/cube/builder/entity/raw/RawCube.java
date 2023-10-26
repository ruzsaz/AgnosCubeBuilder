/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.raw;

import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author parisek
 */
@Getter
@Setter
public class RawCube {
    
    public HashMap<Integer, RawDimension> rawDimensions;
    private RawCells rawCells;

    public RawCube() {
        this.rawDimensions = new HashMap();
    }

    public RawDimension getDimension(Integer idx) {
        if(this.rawDimensions.containsKey(idx)){
            return this.rawDimensions.get(idx);
        }
        return null;
    }
    
    public void addDimension(Integer idx, RawDimension dim){
        if(! this.rawDimensions.containsKey(idx)){
            this.rawDimensions.put(idx, dim);
        }
    }
    
    public void printer(){
        for(RawDimension dim : this.rawDimensions.values()){
            dim.printer();
            System.out.println("\n*******************************************\n");
        }
    }
     
    public void postProcess(){
         for(RawDimension dim : this.rawDimensions.values()){
             dim.postProcess();
         }       
    }
}
