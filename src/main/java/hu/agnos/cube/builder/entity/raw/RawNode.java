/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.raw;

import gnu.trove.list.array.TIntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author parisek
 */
public class RawNode {

    /**
     * a dimenzió-érték hierarchián belüli mélysége. A root esetén ez 0
     */
    private final int depth;

    /**
     * A dimenzió érték azonosítója.
     */
    private Integer id;

    /**
     * A dimenzió érték kódja.
     */
    private String code;

    /**
     * A dimenzió érték neve.
     */
    private String name;

    /**
     * A dimenzió érték gyerekeinek tára.
     */
    private final List<RawNode> children;

    private TIntArrayList factTableRowIds;

    private int[] a;

    private int[] b;

    private int[] childrenId;

    /**
     * erre az egy dimenzióban szereplő több hierarchia esetén van szükség. Ez
     * esetben baseLevel szinten csak 1 id létezhet egy dimenzióben, nem pedig
     * hierarchiánként 1-1.
     */
    private int swapId;

//    private int aggregateChildId;
    private int parentId;

    public RawNode(int depth, String code, String name) {
        this.depth = depth;
        this.code = code;
        this.name = name;
        this.id = -1;
        this.children = new ArrayList<>();
        this.factTableRowIds = new TIntArrayList();
        this.swapId = -1;
    }

    public int getSwapId() {
        return swapId;
    }

    public void setSwapId(int swapId) {
        this.swapId = swapId;
    }

    public void addNewChild(RawNode newChild) {
        boolean isExist = false;
//        System.out.println("addNewChild: " + toString());
        for (RawNode existedChild : this.children) {
//            System.out.println("addNewChild gyerekkeresés...");
            if (newChild.getCode().equals(existedChild.getCode())) {
                isExist = true;
                break;
            }
        }
        if (!isExist) {
//            System.out.println("nincs gyerek addNewChild: " + newChild.toString());
            this.children.add(newChild);
        }
    }

    public TIntArrayList getFactTableRowIds() {
        return factTableRowIds;
    }

    public void addFactTableRowId(Integer id) {

        this.factTableRowIds.add(id);

    }

    public int getDepth() {
        return depth;
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<RawNode> getChildren() {
        return children;
    }

    public RawNode getChild(String code) {
        RawNode result = null;
//        System.out.println("getChild, gyerekszám: " + this.children.size());
        for (RawNode existedChild : this.children) {

            if (existedChild.getCode().equals(code)) {
                result = existedChild;
                break;
            }
        }
        return result;
    }

    public boolean hasChild(String code) {
        boolean result = false;
        if (!this.children.isEmpty()) {
            for (RawNode existedChild : this.children) {
                if (existedChild.getCode().equals(code)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    protected void reindexingInBaseLevelIdInCaseMultiHierarchies(HashMap<String, RawNode> reference, int maxDepth) {
        if (this.depth == maxDepth) {
            if (reference.containsKey(this.code)) {
                this.swapId = reference.get(code).getId();
            }
        } else {
            for (RawNode child : this.children) {
                child.reindexingInBaseLevelIdInCaseMultiHierarchies(reference, maxDepth);
            }
        }
    }

    protected HashMap<String, RawNode> getBaseLevelReferenceAux(HashMap<String, RawNode> map, int maxDepth) {
        if (this.depth == maxDepth) {
            if (!map.containsKey(this.code)) {
                map.put(code, this);
            }
        } else {
            for (RawNode child : this.children) {
                map = child.getBaseLevelReferenceAux(map, maxDepth);
            }
        }
        return map;
    }

    public int getParentId() {
        return parentId;
    }

    public RawNode getFirstChild() {
        RawNode result = null;
        if (!this.children.isEmpty()) {
            result = this.children.get(0);
        }
        return result;
    }

    public int[] getA() {
        return a;
    }

    public void setA(int[] a) {
        this.a = a;
    }

    public int[] getB() {
        return b;
    }

    public void setB(int[] b) {
        this.b = b;
    }

    public int[] getChildrenId() {
        return childrenId;
    }
//
//    public int getAggregateChildId() {
//        return aggregateChildId;
//    }
//
//    public void setAggregateChildId(int aggregateChildId) {
//        this.aggregateChildId = aggregateChildId;
//    }

    public void printer() {
        System.out.println(toString());
        for (RawNode child : this.children) {
            child.printer();
        }
    }

    public void indexing(int[] nodeIdsAuxArray) {
        this.id = nodeIdsAuxArray[this.depth];
        nodeIdsAuxArray[this.depth]++;
        for (RawNode child : this.children) {
            child.indexing(nodeIdsAuxArray);
        }
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
        for (RawNode child : this.children) {
            child.setParentId(this.id);
        }
    }

    public void createChildreaIds() {
//        int[] childrenId
        this.childrenId = null;
        int[] temp = new int[this.children.size()];
        int i = 0;
        for (RawNode child : this.children) {
            int id = child.getSwapId() != -1 ? child.getSwapId() : child.getId();
            temp[i] = id;
            i++;
        }

        Arrays.sort(temp);
        this.childrenId = temp;

        for (RawNode child : this.children) {
            child.createChildreaIds();
        }

    }

    private List<int[]> createIntervalsFrom(int[] elemek) {
        List<int[]> result = new ArrayList<>();

        if (elemek.length == 0) {
            result.add(new int[]{});
            result.add(new int[]{});
            return result;

        } else {
            List<Integer> alsoIdx = new ArrayList();
            List<Integer> felsoIdx = new ArrayList();

            int startPointer = 0;
            int endPointer = 0;
            int size = elemek.length - 1;

            while (endPointer < size) {

                if ((elemek[endPointer + 1] - elemek[endPointer]) > 1) {
                    alsoIdx.add(elemek[startPointer]);
                    felsoIdx.add(elemek[endPointer]);
                    startPointer = endPointer + 1;
                }

                endPointer++;
            }

            alsoIdx.add(elemek[startPointer]);
            felsoIdx.add(elemek[endPointer]);
            startPointer = endPointer + 1;

            int arraySize = alsoIdx.size();

            int[] tempA = new int[arraySize];
            int[] tempB = new int[arraySize];

            for (int i = 0; i < arraySize; i++) {
                tempA[i] = alsoIdx.get(i);
                tempB[i] = felsoIdx.get(i);
            }

            result.add(tempA);
            result.add(tempB);
        }
        return result;

    }

    public void createIntervals() {
        this.factTableRowIds.sort();
        List<int[]> result = createIntervalsFrom(this.factTableRowIds.toArray());

        this.a = result.get(0);
        this.b = result.get(1);

        for (RawNode child : this.children) {
            child.createIntervals();
        }
        this.factTableRowIds = null;

    }

    public void printIntervall() {
        for (int i = 0; i < a.length; i++) {
            System.out.println("" + a[i] + " - " + b[i]);
        }
    }

    public void print() {
        for (int i = 0; i < this.depth; i++) {
            System.out.print("\t");
        }
        System.out.println(toString());
        for (RawNode child : this.children) {
            child.print();
        }
    }

    @Override
    public String toString() {
        return "RawNode{" + "depth=" + depth + ", id=" + id + ", code=" + code + ", name=" + name + ", children=" + children + ", factTableRowIds=" + factTableRowIds + ", a=" + a + ", b=" + b + ", childrenId=" + childrenId + ", swapId=" + swapId + ", parentId=" + parentId + '}';
    }

    
}
