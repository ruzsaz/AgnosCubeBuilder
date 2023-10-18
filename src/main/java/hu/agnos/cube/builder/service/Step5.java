/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.molap.Cube;
import hu.agnos.molap.dimension.Dimension;
import hu.agnos.molap.dimension.Hierarchy;
import hu.agnos.molap.dimension.Node;
import hu.agnos.molap.measure.Cells;
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
     public void convertPreCube2Cube(Cube cube, RawCube preCube) {

         // a Cells átmásolása
        cube.setCells(new Cells(preCube.getPreCells().getCells()));
        
        //a dimenziók és a hierarchiák átmásolása
        for (int dimId : preCube.dimensions.keySet()) {
            RawDimension preDim = preCube.dimensions.get(dimId);

            Dimension dimension = cube.getDimensionByIdx(dimId);
            
            
            boolean isFirst = true;
            for (int hierId : preDim.getHierarchies().keySet()) {
                RawHierarchy preHier = preDim.getHierarchies().get(hierId);

                if (isFirst) {
                    RawNode[] preBaseLevelNodes = preHier.getBaseLevelNode();

                    Node[] baseLevelNodes = new Node[preBaseLevelNodes.length];
                    for (int i = 0; i < preBaseLevelNodes.length; i++) {
                        RawNode preNode = preBaseLevelNodes[i];
                        if (preNode == null) {
//                            System.out.println("preNode == null, i:" +i +", hierId: " +hierId +", dimId: " +dimId);
                        }

                        Node node = new Node(
                                preNode.getId(),
                                preNode.getCode(),
                                preNode.getName(),
                                preNode.getA(),
                                preNode.getB(),
                                preNode.getParentId(),
                                preNode.getChildrenId()
//                                preNode.getAggregateChildId()
                        );
                        baseLevelNodes[i] = node;

                    }
                    dimension.setBaseLevelNodes(baseLevelNodes);
                    isFirst = false;

                }

                RawNode[][] preHierarchy = preHier.getHierarchy();
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
                                preNode.getA(),
                                preNode.getB(),
                                preNode.getParentId(),
                                preNode.getChildrenId()
//                                preNode.getAggregateChildId()
                        );
                        oneRowNodes[i] = node;
                    }
                    nodeHierarchy[j] = oneRowNodes;

                }
                Hierarchy hierarchy = dimension.getHierarchyByUniqueName(preHier.getHierarchyUniqeName());
                hierarchy.setNodes(nodeHierarchy);

            }

        }
     }
}
