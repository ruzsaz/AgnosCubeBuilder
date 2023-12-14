/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.service;

import hu.agnos.cube.Cube;
import hu.agnos.cube.ClassicalCube;
import hu.agnos.cube.CountDistinctCube;
import hu.agnos.cube.builder.entity.pre.PreCellsFloat;
import hu.agnos.cube.builder.entity.pre.PreCellsInt;
import hu.agnos.cube.dimension.Dimension;
import hu.agnos.cube.dimension.Node;
import hu.agnos.cube.builder.entity.pre.PreCube;
import hu.agnos.cube.builder.entity.pre.PreDimension;
import hu.agnos.cube.builder.entity.pre.PreNode;
import hu.agnos.cube.specification.entity.MeasureType;

/**
 *
 * @author parisek
 */
public class Step5 {

    /**
     * EGy PreCube valós Cube-ra konverálása
     *
     * @param cube az a Cube amelybe az adatokat töltjük
     * @param preCube az imputadatokat tartalmazó PreCube
     */
    public void converPreCube2Cube(Cube cube, PreCube preCube) {
          if (preCube.getType().equals(MeasureType.COUNT_DISTINCT.getType())) {
              int [][] cells = ((PreCellsInt)preCube.getRawCells()).getCells();
              ((CountDistinctCube) cube).setCells(cells);
          }
          else {
                  float [][] cells = ((PreCellsFloat)preCube.getRawCells()).getCells();
              ((ClassicalCube) cube).setCells(cells);
        }
  
          
        //a dimenziók és a hierarchiák átmásolása
//        for (int dimId : preCube.rawDimensions.keySet()) {
//            PreDimensions rawDim = preCube.rawDimensions.get(dimId);
        for (int hierId : preCube.getRawDimensions().keySet()) {
            PreDimension preHier = preCube.getRawDimensions().get(hierId);

            Dimension dimension = cube.getDimensions().get(hierId);

            PreNode[][] preHierarchy = preHier.getNodes();
            Node[][] nodeHierarchy = new Node[preHierarchy.length][];
            for (int j = 0; j < preHierarchy.length; j++) {
                PreNode[] oneRowPreNodes = preHierarchy[j];

                Node[] oneRowNodes = new Node[oneRowPreNodes.length];

                for (int i = 0; i < oneRowPreNodes.length; i++) {
                    PreNode preNode = oneRowPreNodes[i];
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
//        }
    }
}
