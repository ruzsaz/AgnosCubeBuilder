/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.pre;

import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author parisek
 */
@Getter
@Setter
@ToString
public class PreCube {

    final String type;
    
    //ez minden egyes node-hoz megmondja, hogy az melyik hierarchiához tartozik
    //szerepe majdnem megegyezik a dimensionIndex-vel, de annak 2x annyi sora van, 
    //mert egy node  két oszlopból áll a ResultSet-ben
    final int[] nodeIndexes;

    private final HashMap<Integer, PreDimension> rawDimensions;

//    public HashMap<Integer, RawDimensions> rawDimensions;
    private AbstractPreCells rawCells;

    public PreCube(int[][] dimensionIndex, String type) {
        this.type = type;
        this.rawDimensions = new HashMap();
        nodeIndexes = new int[dimensionIndex.length / 2];
        int idx = 0;
        for (int i = 0; i < dimensionIndex.length; i = i + 2) {
            nodeIndexes[idx] = dimensionIndex[i][0];
            idx++;
        }

    }

    public PreDimension getDimension(Integer idx) {
        if (this.rawDimensions.containsKey(idx)) {
            return this.rawDimensions.get(idx);
        }
        return null;
    }

    public void addDimension(Integer idx, PreDimension dim) {
        if (!this.rawDimensions.containsKey(idx)) {
            this.rawDimensions.put(idx, dim);
        }
    }

//    public void printer(){
//        for(RawDimensions dim : this.rawDimensions.values()){
//            dim.printer();
//            System.out.println("\n*******************************************\n");
//        }
//    }
//     
//    public void postProcess(){
//         for(RawDimensions dim : this.rawDimensions.values()){
//             dim.postProcess();
//         }       
//    }
    public void printer() {
        for (PreDimension hier : this.rawDimensions.values()) {
            hier.printer();
//            System.out.println("\n---------------------------------------------\n");
        }
    }

    public void postProcess() {

        for (PreDimension hier : this.rawDimensions.values()) {
            hier.indexing();
        }

        for (PreDimension hier : this.rawDimensions.values()) {
            hier.postProcess3();
        }

    }

}
