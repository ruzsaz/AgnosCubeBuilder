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
public class RawDimension {

    private final HashMap<Integer, RawHierarchy> hierarchies;
 

    public RawDimension() {        
        this.hierarchies = new HashMap();
    }

    public HashMap<Integer, RawHierarchy> getHierarchies() {
        return hierarchies;
    }

    public RawHierarchy getHierarchy(int idx) {
        if (this.hierarchies.containsKey(idx)) {
            return this.hierarchies.get(idx);
        } else {
            return null;
        }
    }

    public void addHierarchy(RawHierarchy hier, Integer idx) {
        if (!this.hierarchies.containsKey(idx)) {
            this.hierarchies.put(idx, hier);
        }
    }

    public void printer() {
        for (RawHierarchy hier : this.hierarchies.values()) {
            hier.printer(hier.getRoot());
//            System.out.println("\n---------------------------------------------\n");
        }
    }

    public void postProcess() {
       
        for (RawHierarchy hier : this.hierarchies.values()) {
            hier.indexing();
        }
        
        if(this.hierarchies.size() > 1){
            HashMap<String, RawNode> reference = null;
            boolean isFirst = true;
           for (RawHierarchy hier : this.hierarchies.values()) {
                if(isFirst){
                    reference = hier.getBaseLevelReference();
                    isFirst = false;
                }
                else{
                    hier.reindexingInBaseLevelIdInCaseMultiHierarchies(reference);
                }
            }
        }
        
        for (RawHierarchy hier : this.hierarchies.values()) {
            hier.postProcess3();
        }
        
    }

    @Override
    public String toString() {
        return "PreDimension{" + ", hierarchiesSize=" + hierarchies.size() + '}';
    }

}
