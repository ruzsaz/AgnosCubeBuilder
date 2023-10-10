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
public class RawHierarchy {

    private final RawNode root;

    private RawNode[][] nodes;

    private final String hierarchyUniqeName;

    private final boolean isOLAP;

    public RawHierarchy(String hierarchyUniqeName, boolean isOLAP) {
        this.root = new RawNode(0, "All", "All");
        this.hierarchyUniqeName = hierarchyUniqeName;
        this.isOLAP = isOLAP;
    }

    public RawNode[] getBaseLevelNode() {
        int lastIndex = nodes.length - 1;
//        System.out.println("" + this.hierarchyUniqeName + ", lastIndex: " + lastIndex);
        return nodes[lastIndex];
    }

    public RawNode[][] getHierarchy() {
        int lastIndex = nodes.length - 1;
        RawNode[][] result = new RawNode[lastIndex][];

        for (int i = 0; i < lastIndex; i++) {
            result[i] = this.nodes[i];
        }

        return result;
    }

    public String getHierarchyUniqeName() {
        return hierarchyUniqeName;
    }

    public RawNode getRoot() {
        return root;
    }

    public boolean isOLAP() {
        return this.isOLAP;
    }


    private int[] getNodeIdsAuxArray() {
        int depth = getDepth();

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

    public void reindexingInBaseLevelIdInCaseMultiHierarchies(HashMap<String, RawNode> reference) {
        int maxDepth = this.getDepth()-1;
        root.reindexingInBaseLevelIdInCaseMultiHierarchies(reference, maxDepth);
    }

    public HashMap<String, RawNode> getBaseLevelReference() {
        HashMap<String, RawNode> result = new HashMap<>();
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

//    public void generateIntervallForRoot() {
//        Set<Integer> rootFactTableRowIds = root.getFactTableRowIds();
//        rootFactTableRowIds.clear();
//
//        if (root.getAggregateChildId() != -1) {
//            for (RawNode child : root.getChildren()) {
//                if (child.getCode().equals("All")) {
//                    int[] childA = child.getA();
//                    int[] childB = child.getB();
//
//                    for (int i = 0; i < childA.length; i++) {
//
//                        int startIdx = childA[i];
//                        int endIdx = childB[i];
//
//                        while (startIdx <= endIdx) {
//                            if (!rootFactTableRowIds.contains(startIdx)) {
//                                rootFactTableRowIds.add(startIdx);
//                            }
//                            startIdx++;
//                        }
//                    }
//                    break;
//                }
//
//            }
//        } else {
//            for (RawNode child : root.getChildren()) {
//                int[] childA = child.getA();
//                int[] childB = child.getB();
//
//                for (int i = 0; i < childA.length; i++) {
//
//                    int startIdx = childA[i];
//                    int endIdx = childB[i];
//
//                    while (startIdx <= endIdx) {
//                        if (!rootFactTableRowIds.contains(startIdx)) {
//                                rootFactTableRowIds.add(startIdx);
//                            }
//                        startIdx++;
//                    }
//                }
//            }
//        }
//        root.createIntervals(false);
//    }
//    public void setRootAggregetedChild() {
//        for (RawNode child : root.getChildren()) {
//            if (child.getCode().equals("All")) {
//                root.setAggregateChildId(child.getId());
//            }
//        }
//    }
    public void nodeArrayLoder(RawNode member) {
        if (member.equals(root)) {
            this.nodes[0][0] = root;
        }
        for (RawNode child : member.getChildren()) {
            int childId = child.getId();
            int childDepth = child.getDepth();

            this.nodes[childDepth][childId] = child;
            nodeArrayLoder(child);
        }
    }

    private void createNodesArraySkeleton(int[] nodeIdsAuxArray) {
        int levelsCnt = nodeIdsAuxArray.length;
        this.nodes = new RawNode[levelsCnt][];
        for (int i = 0; i < levelsCnt; i++) {
            RawNode[] level = new RawNode[nodeIdsAuxArray[i]];
            this.nodes[i] = level;
        }
    }

    private int getDepth() {
        int depth = 1;
        RawNode actualNode = root;
        while (!actualNode.isLeaf()) {
            depth++;
            actualNode = actualNode.getFirstChild();
        }
        return depth;
    }

    public void printer(RawNode member) {
        root.print();
    }

    @Override
    public String toString() {
        return "RawHierarchy{" + "root=" + root + ", nodes=" + nodes + ", hierarchyUniqeName=" + hierarchyUniqeName + ", isOLAP=" + isOLAP + '}';
    }

    
}
