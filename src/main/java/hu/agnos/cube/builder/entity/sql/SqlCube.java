/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.sql;

import java.util.ArrayList;
import java.util.List;
import hu.agnos.cube.builder.entity.raw.RawCube;
import hu.agnos.cube.builder.entity.raw.RawDimension;
import hu.agnos.cube.builder.entity.raw.RawHierarchy;
import hu.agnos.cube.builder.util.SQLGenerator;

/**
 *
 * @author parisek
 */
public class SqlCube {

    private final List<SqlDimension> dimensions;

    private final List<SqlMeasure> measures;

    private final String sourceTableName;

    private final String sourceDBDriver;

    private int[][] dimensionIndex;

    public SqlCube(String sourceTableName, String sourceDBDriver) {
        this.dimensions = new ArrayList();
        this.measures = new ArrayList();
        this.sourceDBDriver = sourceDBDriver;

        this.sourceTableName = sourceTableName;
    }

    public SqlDimension getDimension(int idx) {
        return this.dimensions.get(idx);
    }

    public List<SqlDimension> getDimensions() {
        return dimensions;
    }

    public void addDimension(SqlDimension dim) {
        this.dimensions.add(dim);
    }

    public SqlMeasure getMeasure(int idx) {
        return this.measures.get(idx);
    }

    public List<SqlMeasure> getMeasures() {
        return measures;
    }

    public void addMeasure(SqlMeasure measure) {
        this.measures.add(measure);
    }

    public String getImportSQL( long from, long to) {
         return SQLGenerator.getImportSQL("TMP_",this, from, to);
    }

    public String getCreateSQL() {       
        return SQLGenerator.getCreateSQL("TMP_",this);
    }

    public String getCreateIndexSQL() {       
        return SQLGenerator.getCreateIndexSQL("TMP_",this);
    }
    
    public String  getDropIndexSQL()  {     
        return SQLGenerator.getDropIndexSQL(this);
    }
    
    public String getDropSQL() {
        return SQLGenerator.getDropSQL("TMP_", sourceTableName, sourceDBDriver);
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public int[][] getDimensionIndex() {
        return dimensionIndex;
    }

    public void setDimensionIndex(int[][] dimensionIndex) {
        this.dimensionIndex = dimensionIndex;
    }

    public String getSourceDBDriver() {
        return sourceDBDriver;
    }
    
    

    public RawCube getEmptyPreCube() {
        RawCube preCube = new RawCube();
        int dimIdx = 0;
        for (SqlDimension sqlDimension : this.dimensions) {
            RawDimension preDim = new RawDimension();
            int hierIdx = 0;
            for (SqlHierarchy sqlHier : sqlDimension.getHierarchies()) {
                RawHierarchy preHierarchy = new RawHierarchy(sqlHier.getUniqeName(), sqlHier.isOLAP());
                preDim.addHierarchy(preHierarchy, hierIdx);
                hierIdx++;
            }
            preCube.addDimension(dimIdx, preDim);
            dimIdx++;
        }
        return preCube;
    }

    public int getDimensionColumnCountInSQLResultSet() {
        int dimensionColumnCnt = 0;
        for (SqlDimension dim : this.dimensions) {
            for (SqlHierarchy hier : dim.getHierarchies()) {
                dimensionColumnCnt += hier.getLevels().size();
            }
        }
        // *2 mert code és name is van
        return dimensionColumnCnt * 2;
    }
}
