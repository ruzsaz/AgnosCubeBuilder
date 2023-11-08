/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.raw;

import java.util.HashMap;
import lombok.ToString;

/**
 *
 * @author parisek
 */
@ToString
public class RawDimensions {

    private final HashMap<Integer, RawDimension> dimensions;
 

    public RawDimensions() {        
        this.dimensions = new HashMap();
    }

    public HashMap<Integer, RawDimension> getDimensions() {
        return dimensions;
    }

    public RawDimension getDimension(int idx) {
        if (this.dimensions.containsKey(idx)) {
            return this.dimensions.get(idx);
        } else {
            return null;
        }
    }

    public void addHierarchy(RawDimension hier, Integer idx) {
        if (!this.dimensions.containsKey(idx)) {
            this.dimensions.put(idx, hier);
        }
    }

    public void printer() {
        for (RawDimension hier : this.dimensions.values()) {
            hier.printer(hier.getRoot());
//            System.out.println("\n---------------------------------------------\n");
        }
    }

    public void postProcess() {
       
        for (RawDimension hier : this.dimensions.values()) {
            hier.indexing();
        }
        
        if(this.dimensions.size() > 1){
            HashMap<String, RawNode> reference = null;
            boolean isFirst = true;
           for (RawDimension hier : this.dimensions.values()) {
                if(isFirst){
                    reference = hier.getBaseLevelReference();
                    isFirst = false;
                }
                else{
                    hier.reindexingInBaseLevelIdInCaseMultiHierarchies(reference);
                }
            }
        }
        
        for (RawDimension hier : this.dimensions.values()) {
            hier.postProcess3();
        }
        
    }


}
