/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author freda
 */
@JacksonXmlRootElement(localName = "step")
public class Step  {
    
    public Step() {
      lines = new ArrayList();
    }
    
    public Step( String instruction) {
        this.instruction = instruction;
        lines = new ArrayList();
    }
    
    @JacksonXmlProperty
    @JsonProperty
    Integer num;

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public Collection<Line> getLines() {
        return lines;
    }

    public void setLines(Collection<Line> lines) {
        if ( lines == null) {
            lines = new ArrayList();
        }
        this.lines = lines;
    }
    
    @JacksonXmlProperty
    @JsonProperty
    String instruction;
    
    @JacksonXmlElementWrapper(localName = "lines")
    @JacksonXmlProperty
    @JsonProperty
    Collection<Line> lines = new ArrayList();    
    
    
    
    
}
