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
public class SqlDimension {

    private final List<SqlHierarchy> hierarchies;
    
   

    public SqlDimension() {       
        this.hierarchies = new ArrayList();
    }

    public List<SqlHierarchy> getHierarchies() {
        return hierarchies;
    }
    
    public SqlHierarchy getHierarchyByIdx(int idx) {
        return hierarchies.get(idx);
    }
    
    public void addHierarchy(SqlHierarchy hier) {
        this.hierarchies.add(hier);
    }

    public SqlHierarchy addHierarchy(int idx) {
        return this.hierarchies.get(idx);
    }

    public void handleDuplicates() {
        int hierarchiesSize = this.hierarchies.size();
        if (hierarchiesSize > 1) {
            String[] lastLevelCodeAliass = new String[hierarchiesSize];
            String[] lastLevelNameAlias = new String[hierarchiesSize];

            for (int i = 0; i < hierarchiesSize; i++) {
                SqlHierarchy hier = this.hierarchies.get(i);
                SqlLevel lastLevel = hier.getLevel(hier.getLastLevelIdx());
                for (int j = 0; j < i; j++) {
                    if (lastLevelCodeAliass[j].equals(lastLevel.getCodeAlias())) {
                        lastLevel.setCodeAlias(lastLevel.getCodeAlias() + "_" + i);
                    }

                    if (lastLevelNameAlias[j].equals(lastLevel.getNameAlias())) {
                        lastLevel.setNameAlias(lastLevel.getNameAlias() + "_" + i);
                    }
                }
                lastLevelCodeAliass[i] = lastLevel.getCodeAlias();
                lastLevelNameAlias[i] = lastLevel.getNameAlias();

            }
        }
    }

}
