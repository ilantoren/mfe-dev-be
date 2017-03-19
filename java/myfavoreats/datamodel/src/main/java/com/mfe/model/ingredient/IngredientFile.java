/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.ingredient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author freda
 */
    @JsonIgnoreProperties
    @JacksonXmlRootElement(localName = "data")  
    public class IngredientFile {
        
    @JacksonXmlProperty(localName = "note")
    @JacksonXmlElementWrapper(useWrapping = false)
    String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    @JacksonXmlProperty(localName = "ingredient")
    @JacksonXmlElementWrapper(useWrapping = false)
    IngredientPOJO[] ingredient;

    public IngredientPOJO[] getIngredient() {
        return ingredient;
    }

    public void setIngredient(IngredientPOJO[] ingredient) {
        this.ingredient = ingredient;
    }
    
}
