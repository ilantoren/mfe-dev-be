/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mfe.model.recipe.NutrientProfile;
import com.mfe.model.recipe.RecipeChange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;

/**
 *
 * @author richardthorne
 * Object for searching for changes in RecipePOJO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "demoRecipe")
public class DemoRecipeSearch implements Serializable {
    public DemoRecipeSearch() {}
    
    
    public DemoRecipeSearch( String recipeId, String title ) {
    	this.recipeId = recipeId;
    	this.title = title;
    }
    
    @JacksonXmlProperty
    @JsonProperty
    @Id
    String id;  
    // Data is derived from a recipePOJO and it's id is put here
    String recipeId;
    String title;
    List<String> categories;
    String url;
    String imageUrl;
    String site;   // the short name for the site for display
    boolean isParent = true;
    // not a parent should have the id of the DemoRecipeSearch that is the parent
    String parentId;
    List<String> SubstitutionRule;
    Double probability;
    List<String> ingredients;
    // points to the Id of the parent recipePOJO 
    String recipeParentId;
// needs to be less than 2MB so use one resized to   250px x 200px 
    byte[] imagePNG;
    NutrientProfile nutrient;
    Map<String, String>  changes = new HashMap<>();
    RecipeChange recipeChange;
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isIsParent() {
        return isParent;
    }

    public void setIsParent(boolean isParent) {
        this.isParent = isParent;
    }

    public List<String> getSubstitutionRule() {
        return SubstitutionRule = new ArrayList<>();
    }

    public void setSubstitutionRule(List<String> SubstitutionRule) {
        this.SubstitutionRule = SubstitutionRule;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getRecipeParentId() {
        return recipeParentId;
    }

    public void setRecipeParentId(String recipeParentId) {
        this.recipeParentId = recipeParentId;
    }

    public byte[] getImagePNG() {
        return imagePNG;
    }

    public void setImagePNG(byte[] imagePNG) {
        this.imagePNG = imagePNG;
    }

    public NutrientProfile getNutrient() {
        return nutrient;
    }

    public void setNutrient(NutrientProfile nutrient) {
        this.nutrient = nutrient;
    }

    public Map<String, String> getChanges() {
        return changes;
    }

    public void setChanges(Map<String, String> changes) {
        this.changes = changes;
    }

    public RecipeChange getRecipeChange() {
        return recipeChange;
    }

    public void setRecipeChange(RecipeChange recipeChange) {
        this.recipeChange = recipeChange;
    }


	public String getSite() {
		return site;
	}


	public void setSite(String site) {
		this.site = site;
	}

    
}
