/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hu.agnos.cube.builder.entity.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 *
 * @author parisek
 */
@Getter
@AllArgsConstructor
@ToString
public class SqlMeasure {
    
    private final String name;
    
    private final String type;
    
}
