/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hu.agnos.cube.builder.entity.pre;

/**
 *
 * @author parisek
 */
public interface CountDistinctCellInterface {
    
    public int getCell (int rowCont, int columnCnt);
    
    public void addCell(int rowIdx, int columnIdx, int value);
    
    public int[] getColumn (int columnIdx);
    
    public void addColumn(int columnIdx, int[] values);
    
}
