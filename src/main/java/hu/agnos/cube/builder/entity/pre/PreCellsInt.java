/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.pre;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author parisek
 */
@Getter
@Setter
public class PreCellsInt extends AbstractPreCells {

    String type;

    int[][] cells;

    public PreCellsInt(int columnCnt, String type) {
        this.type = type;
        this.cells = new int[columnCnt][];
    }

    public PreCellsInt(int rowCont, int columnCnt) {
        this.cells = new int[columnCnt][rowCont];
    }

    public int getCell(int rowCont, int columnCnt) {
        return this.cells[columnCnt][rowCont];
    }

    public int[] getColumn(int columnIdx) {
        return this.cells[columnIdx];
    }

    public void addCell(int rowIdx, int columnIdx, int value) {
        this.cells[columnIdx][rowIdx] = value;
    }

    public void addColumn(int columnIdx, int[] values) {
        this.cells[columnIdx] = values;
    }

}
