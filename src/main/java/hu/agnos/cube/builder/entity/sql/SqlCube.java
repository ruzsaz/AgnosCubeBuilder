/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.sql;

import java.util.ArrayList;
import java.util.List;
import hu.agnos.cube.builder.entity.pre.PreCube;
import hu.agnos.cube.builder.entity.pre.PreDimension;
import hu.agnos.cube.builder.service.sql.H2SQLGenerator;
import hu.agnos.cube.builder.service.sql.OracleSQLGenerator;
import hu.agnos.cube.builder.service.sql.PostgreSQLGenerator;
import hu.agnos.cube.builder.service.sql.SAPSQLGenerator;
import hu.agnos.cube.builder.service.sql.SQLGenerator;
import hu.agnos.cube.builder.service.sql.SQLServerSQLGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author parisek
 */
@Getter
@Setter
@ToString
public class SqlCube {

    private final String type;
    
    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";
    private static final String SAP_DRIVER = "com.sap.db.jdbc.Driver";

    private final List<SqlDmension> dimensions;

    private final List<SqlMeasure> measures;

    private final String sourceTableName;

    private int[][] dimensionIndex;

    private final SQLGenerator sqlGenerator;

    public SqlCube(String sourceTableName, String sourceDBDriver, String type) {
        this.type = type;
        this.measures = new ArrayList();
        this.dimensions = new ArrayList();

        this.sourceTableName = sourceTableName;

        switch (sourceDBDriver) {
            case ORACLE_DRIVER:
                this.sqlGenerator = new OracleSQLGenerator();
                break;
            case H2_DRIVER:
                this.sqlGenerator = new H2SQLGenerator();
                break;
            case SQLSERVER_DRIVER:
                this.sqlGenerator = new SQLServerSQLGenerator();
                break;
            case POSTGRES_DRIVER:
                this.sqlGenerator = new PostgreSQLGenerator();
                break;
            case SAP_DRIVER:
                this.sqlGenerator = new SAPSQLGenerator();
                break;
            default:
                this.sqlGenerator = null;
        }
    }

    public void addDimesion(SqlDmension dim) {
        this.dimensions.add(dim);
    }

    public SqlDmension findDimensionByIdx(int idx) {
        return this.dimensions.get(idx);
    }

    public SqlMeasure findMeasureByIdx(int idx) {
        return this.measures.get(idx);
    }

    public void addMeasure(SqlMeasure measure) {
        this.measures.add(measure);
    }

    public String getImportSQL(long from, long to) {
        return sqlGenerator.getImportSQL("TMP_", this, from, to);
    }

    public String getCreateSQL() {
        return sqlGenerator.getCreateSQL("TMP_", this);
    }

    public String getCreateIndexSQL() {
        return sqlGenerator.getCreateIndexSQL("TMP_", this);
    }

    public String getDropIndexSQL() {
        return sqlGenerator.getDropIndexSQL(this);
    }

    public String getDropSQL() {
        return sqlGenerator.getDropSQL("TMP_", sourceTableName);
    }

    public PreCube getEmptyPreCube() {
        
        PreCube preCube = new PreCube(dimensionIndex, this.type);
        int dimIdx = 0;
        for (SqlDmension sqlDim : this.dimensions) {
            PreDimension preDim = new PreDimension(
                    sqlDim.getUniqeName(),
                    sqlDim.isOfflineCalculated());
            preCube.addDimension(dimIdx, preDim);
            dimIdx++;
        }
        return preCube;
    }

    public int getDimensionColumnCount() {
        
        
        int dimensionColumnCnt = 0;
        for (SqlDmension hier : getDimensions()) {
            dimensionColumnCnt += hier.getLevels().size();
        }
        // *2 mert code és name is van
        return dimensionColumnCnt * 2;
    }


}
