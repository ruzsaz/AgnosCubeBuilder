/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package hu.agnos.cube.builder.entity.pre;

/**
 *
 * @author parisek
 */
public interface ClassicalCellInterface1 {
    
    public float getCell (int rowCont, int columnCnt);
    
    public void addCell(int rowIdx, int columnIdx, float value);
    
    public float[] getColumn (int columnIdx);
    
    public void addColumn(int columnIdx, float[] values);
    
}
