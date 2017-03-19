/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author freda
 */
    @JsonIgnoreProperties
    @JacksonXmlRootElement(localName = "data")  
    public class NutrientsFile {
        
    @JacksonXmlProperty(localName = "note")
    @JacksonXmlElementWrapper(useWrapping = false)
    String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    // File still has heading of ingredient for repeated items
    @JacksonXmlProperty(localName = "ingredient")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty(  "ingredient")
    NutrientProfile[] nutrient;

    public NutrientProfile[] getNutrient() {
        return nutrient;
    }

    public void setNutrient(NutrientProfile[] nutrient) {
        this.nutrient = nutrient;
    }
    
}
