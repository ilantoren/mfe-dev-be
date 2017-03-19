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

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author freda
 */

@JacksonXmlRootElement(localName = "data")  
@JsonIgnoreProperties(ignoreUnknown = false)
public class RecipeFile {
    public RecipeFile() {}
    @JacksonXmlElementWrapper(localName="recipe")
    @JacksonXmlProperty
    @JsonProperty
    Collection<RecipePOJO> recipe = new ArrayList();
    public Collection<RecipePOJO> getRecipes() { return recipe; }
    public void setRecipes(  Collection<RecipePOJO> pojos) { recipe = pojos; } 
}
