/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.measure.Cells;
import hu.agnos.cube.builder.entity.raw.RawCube;
import hu.agnos.cube.builder.entity.raw.RawDimension;
import hu.agnos.cube.builder.entity.raw.RawHierarchy;
import hu.agnos.cube.builder.entity.raw.RawNode;

/**
 *
 * @author parisek
 */
public class Step5 {
    
    /**
     * EGy RawCube valós Cube-ra konverálása
     * @param cube az a Cube amelybe az adatokat töltjük
     * @param preCube az imputadatokat tartalmazó RawCube
     */
     public void converPreCube2Cube(Cube cube, RawCube preCube) {

         // a Cells átmásolása
        cube.setCells(new Cells(preCube.getRawCells().getCells()));
        
        //a dimenziók és a hierarchiák átmásolása
        for (int dimId : preCube.rawDimensions.keySet()) {
            RawDimension rawDim = preCube.rawDimensions.get(dimId);

            Dimension dimension = cube.getDimensions().get(dimId);
            
            
            for (int hierId : rawDim.getHierarchies().keySet()) {
                RawHierarchy preHier = rawDim.getHierarchies().get(hierId);

               
                RawNode[][] preHierarchy = preHier.getNodes();
                Node[][] nodeHierarchy = new Node[preHierarchy.length][];
                for (int j = 0; j < preHierarchy.length; j++) {
                    RawNode[] oneRowPreNodes = preHierarchy[j];

                    Node[] oneRowNodes = new Node[oneRowPreNodes.length];

                    for (int i = 0; i < oneRowPreNodes.length; i++) {
                        RawNode preNode = oneRowPreNodes[i];
                        Node node = new Node(
                                preNode.getId(),
                                preNode.getCode(),
                                preNode.getName(),
                                preNode.getDepth(),
                                preNode.getA(),
                                preNode.getB(),
                                preNode.getParentId(),
                                preNode.getChildrenId()
                        );
                        oneRowNodes[i] = node;
                    }
                    nodeHierarchy[j] = oneRowNodes;

                }
                //Hierarchy hierarchy = dimension.getHierarchyByUniqueName(preHier.getHierarchyUniqeName());
                dimension.setNodes(nodeHierarchy);

            }

        }
     }
}
