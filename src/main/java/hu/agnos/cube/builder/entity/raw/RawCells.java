/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.raw;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author parisek
 */
@Getter
@Setter
public class RawCells {
    
    float [][] cells;

    public RawCells(int rowCont, int columnCnt) {
        this.cells = new float[columnCnt] [rowCont];
    }
        
    public float getCell (int rowCont, int columnCnt) {
        return this.cells[columnCnt] [rowCont];
    }
    
    public float[] getColumn (int columnIdx) {
        return this.cells[columnIdx];
    }

    public void addCell(int rowIdx, int columnIdx, float value) {
        this.cells[columnIdx] [rowIdx] = value;
    }
    
    public void addColumn(int columnIdx, float[] values) {
        this.cells[columnIdx] = values;
    } 
    
}
