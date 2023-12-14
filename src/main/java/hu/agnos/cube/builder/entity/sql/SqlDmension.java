/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.sql;

import java.util.ArrayList;
import java.util.List;
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
public class SqlDmension {

    private final String uniqeName;

    private final List<SqlLevel> levels;

    private final boolean isOfflineCalculated;

    public SqlDmension(String uniqeName, boolean isOfflineCalculated) {
        this.uniqeName = uniqeName;
        this.levels = new ArrayList();
        this.isOfflineCalculated = isOfflineCalculated;
    }

    public void addLevel(SqlLevel l) {
        this.levels.add(l);
    }

    
    public SqlLevel findLevelByIdx(int idx) {
        return this.levels.get(idx);
    }

    public int getLastLevelIdx() {
        return this.levels.size() - 1;
    }

}
