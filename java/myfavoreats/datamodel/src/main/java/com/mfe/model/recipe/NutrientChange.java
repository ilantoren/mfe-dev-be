/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Objects;

/**
 *
 * @author richardthorne
 */
public class NutrientChange {
    
    
    public NutrientChange() {}
    
    
    @JacksonXmlProperty
    @JsonProperty
    private String name;
    
    
    @JacksonXmlProperty
    @JsonProperty
    private Double change;
    
    @JacksonXmlProperty
    @JsonProperty
    private Double percentChange;
    
     @JacksonXmlProperty(isAttribute = true)
     @JsonProperty
    private ChangeType changeType;
     
     
    public NutrientChange ( String name, Double change,  Double percentChange, ChangeType changeType ) {
        this.name = name;
        this.change = change;
        this.percentChange = percentChange;
        this.changeType = changeType;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getChange() {
        return change;
    }

    public void setChange(Double change) {
        this.change = change;
    }

    public Double getPercentChange() {
    	if ( percentChange == null ) return 0D;
        return percentChange;
    }

    public void setPercentChange(Double percentChange) {
        this.percentChange = percentChange;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutrientChange other = (NutrientChange) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.change, other.change)) {
            return false;
        }
        if (!Objects.equals(this.percentChange, other.percentChange)) {
            return false;
        }
        if (this.changeType != other.changeType) {
            return false;
        }
        return true;
    }

    
    

    @Override
    public String toString() {
        return "NutrientChange{" + "name=" + name + ", percentChange=" + percentChange + ", changeType=" + changeType + '}';
    }
    
}
