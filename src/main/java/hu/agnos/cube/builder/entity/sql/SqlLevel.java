/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.sql;

/**
 *
 * @author parisek
 */
public class SqlLevel {

    private final String code;
    private final String name;

    private String codeAlias;
    private String nameAlias;

    public SqlLevel(String code, String name) {
        this.code = code;

        if (this.code.contains("#")) {
            for (String codeSegment : this.code.split("#")) {
                this.codeAlias = codeSegment;
            }
            this.codeAlias += "_CODE";
        } else {
            this.codeAlias = this.code;
        }
        
        this.name = name;
        
        if(this.name.equals(this.codeAlias) ){
            this.nameAlias = this.name + "_NAME";
        }
        else{
            this.nameAlias = this.name;
        }    
       
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCodeAlias() {
        return codeAlias;
    }

    public void setCodeAlias(String codeAlias) {
        this.codeAlias = codeAlias;
    }

    public String getNameAlias() {
        return nameAlias;
    }

    public void setNameAlias(String nameAlias) {
        this.nameAlias = nameAlias;
    }

    public boolean hasCodeAlias() {
        return !this.code.equals(this.codeAlias);
    }

    public boolean hasNameAlias() {
        return !this.name.equals(this.nameAlias);
    }
}
