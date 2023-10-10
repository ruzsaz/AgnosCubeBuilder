/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.sql;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author parisek
 */
public class SqlHierarchy {

    private final String uniqeName;

    private final List<SqlLevel> levels;

    private final boolean isOLAP;

    public SqlHierarchy(String uniqeName, boolean isOLAP) {
        this.uniqeName = uniqeName;
        this.levels = new ArrayList();
        this.isOLAP = isOLAP;
    }

    public void addLevel(SqlLevel l) {
        this.levels.add(l);
    }

    public List<SqlLevel> getLevels() {
        return levels;
    }

    public SqlLevel getLevel(int idx) {
        return this.levels.get(idx);
    }

    public String getUniqeName() {
        return uniqeName;
    }

    public int getLastLevelIdx() {
        return this.levels.size() - 1;
    }

    public boolean isOLAP() {
        return this.isOLAP;
    }
}
