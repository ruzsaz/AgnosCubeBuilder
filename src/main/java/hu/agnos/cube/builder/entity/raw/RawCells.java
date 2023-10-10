/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.raw;

/**
 *
 * @author parisek
 */
public class RawCells {
    
    double [][] cells;

    public RawCells(int rowCont, int columnCnt) {
        this.cells = new double[columnCnt] [rowCont];
    }
        
    public double getCell (int rowCont, int columnCnt) {
        return this.cells[columnCnt] [rowCont];
    }
    
    public double[] getColumn (int columnIdx) {
        return this.cells[columnIdx];
    }

    public void addCell(int rowIdx, int columnIdx, double value) {
        this.cells[columnIdx] [rowIdx] = value;
    }
    
    public void addColumn(int columnIdx, double[] values) {
        this.cells[columnIdx] = values;
    }

    public double[][] getCells() {
        return cells;
    }

    public void setCells(double[][] cells) {
        this.cells = cells;
    } 
    
    
}
