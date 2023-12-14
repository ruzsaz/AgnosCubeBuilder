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

public class PreDimension {

    private final PreNode root;

    private PreNode[][] nodes;

    private final String dimensionUniqeName;

    private final boolean isOfflineCalculated;

    public PreDimension(String dimensionUniqeName, boolean isOfflineCalculated) {
        this.root = new PreNode(0, "All", "All");
        this.dimensionUniqeName = dimensionUniqeName;
        this.isOfflineCalculated = isOfflineCalculated;
    }

    public PreNode[] getBaseLevelNode() {
        int lastIndex = nodes.length - 1;
//        System.out.println("" + this.dimensionUniqeName + ", lastIndex: " + lastIndex);
        return nodes[lastIndex];
    }

    public PreNode[][] getDimension() {
        int lastIndex = nodes.length - 1;
        PreNode[][] result = new PreNode[lastIndex][];

        for (int i = 0; i < lastIndex; i++) {
            result[i] = this.nodes[i];
        }

        return result;
    }

   
   
   

    private int[] getNodeIdsAuxArray() {
        int depth = getDepth();
//        System.out.println("getNodeIdsAuxArray: " + dimensionUniqeName +", depth: " + depth);

        int[] auxArray = new int[depth];
        for (int i = 0; i < depth; i++) {
            auxArray[i] = 0;
        }

        return auxArray;
    }

    public void indexing() {

        int[] nodeIdsAuxArray = getNodeIdsAuxArray();
        root.indexing(nodeIdsAuxArray);
        createNodesArraySkeleton(nodeIdsAuxArray);

    }

    public void reindexingInBaseLevelIdInCaseMultiHierarchies(HashMap<String, PreNode> reference) {
        int maxDepth = this.getDepth()-1;
        root.reindexingInBaseLevelIdInCaseMultiHierarchies(reference, maxDepth);
    }

    public HashMap<String, PreNode> getBaseLevelReference() {
        HashMap<String, PreNode> result = new HashMap<>();
        int maxDepth = this.getDepth()-1;
        result = root.getBaseLevelReferenceAux(result, maxDepth);
        return result;
    }

    public void postProcess3() {
        root.setParentId(-1);
        root.createChildreaIds();
        root.createIntervals();
        nodeArrayLoder(root);
    }

    public void nodeArrayLoder(PreNode member) {
        if (member.equals(root)) {
            this.nodes[0][0] = root;
        }
        for (PreNode child : member.getChildren()) {
            int childId = child.getId();
            int childDepth = child.getDepth();

            this.nodes[childDepth][childId] = child;
            nodeArrayLoder(child);
        }
    }

    private void createNodesArraySkeleton(int[] nodeIdsAuxArray) {
        int levelsCnt = nodeIdsAuxArray.length;
        this.nodes = new PreNode[levelsCnt][];
        for (int i = 0; i < levelsCnt; i++) {
            PreNode[] level = new PreNode[nodeIdsAuxArray[i]];
            this.nodes[i] = level;
        }
    }

    private int getDepth() {
        int depth = 1;
        PreNode actualNode = root;
//        System.out.println("actualNode: " + depth +", "+ actualNode.toString() );
        while (!actualNode.isLeaf()) {
            depth++;
      
            actualNode = actualNode.getFirstChild();
//        System.out.println("actualNode: " + depth +", "+ actualNode.toString() );
     
        }
//          System.out.println("actualNode: " + depth +", "+ actualNode.toString() );
      
        return depth;
    }

    public void printer() {
        root.print();
    }

    @Override
    public String toString() {
        printer();
        return "PreDimension{" + "dimensionUniqeName=" + dimensionUniqeName + ", isOfflineCalculated=" + isOfflineCalculated + '}';
    }


    
}
