/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.commoncrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;

/**
 *
 * @author richardthorne
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "recipe")
public class Recipe implements Serializable {



	private String urn;

    public Recipe() {
        this.ingredient = new ArrayList();
    }
    private String ogTitle, ogSite, ogDescription, ogUrl, ogType, ogImage, contributor,classA, classB,
            ogRating, ogRatingScale, recipeName, ratingValue, reviewCount, author, ratingCount,
            description, recipeYield, cookTime, prepTime, totalTime, nutritionInfo;

    boolean unmatchedFood = false, missingFood = false, nutritionCalcError = false;

    @JsonProperty
    @Id
    private String id;

    @JacksonXmlElementWrapper(localName = "ingredients")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    List<Ingredient> ingredient;

    @JacksonXmlElementWrapper(localName = "properties")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    List<String> property = new ArrayList();

    @JacksonXmlElementWrapper(localName = "categories")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    List<String> category = new ArrayList();
    
    
    
    @JacksonXmlElementWrapper(localName = "instructions")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    List<String> recipeInstruction = new ArrayList();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOgTitle() {
        return ogTitle;
    }

    public void setOgTitle(String ogTitle) {
        this.ogTitle = ogTitle;
    }

    public String getOgSite() {
        return ogSite;
    }

    public void setOgSite(String ogSite) {
        this.ogSite = ogSite;
    }

    public String getOgDescription() {
        return ogDescription;
    }

    public void setOgDescription(String ogDescription) {
        this.ogDescription = ogDescription;
    }

    public String getOgUrl() {
        return ogUrl;
    }

    public void setOgUrl(String ogUrl) {
        this.ogUrl = ogUrl;
    }

    public String getClassA() {
        return classA;
    }

    public void setClassA(String classA) {
        this.classA = classA;
    }

    public String getClassB() {
        return classB;
    }

    public void setClassB(String classB) {
        this.classB = classB;
    }

    
    public String getOgType() {
        return ogType;
    }

    public void setOgType(String ogType) {
        this.ogType = ogType;
    }

    public String getOgImage() {
        return ogImage;
    }

    public void setOgImage(String ogImage) {
        this.ogImage = ogImage;
    }

    public String getOgRating() {
        return ogRating;
    }

    public void setOgRating(String ogRating) {
        this.ogRating = ogRating;
    }

    public String getOgRatingScale() {
        return ogRatingScale;
    }

    public void setOgRatingScale(String ogRatingScale) {
        this.ogRatingScale = ogRatingScale;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(String ratingValue) {
        this.ratingValue = ratingValue;
    }

    public String getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(String reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecipeYield() {
        return recipeYield;
    }

    public void setRecipeYield(String recipeYield) {
        this.recipeYield = recipeYield;
    }

    public String getCookTime() {
        return cookTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public List<String> getRecipeInstruction() {
        return recipeInstruction;
    }

    public void setRecipeInstruction(List<String> recipeInstruction) {
        this.recipeInstruction = recipeInstruction;
    }

    public List<Ingredient> getIngredient() {
        return ingredient;
    }

    public void setIngredient(List<Ingredient> ingredient) {
        this.ingredient = ingredient;
    }

    public List<String> getProperty() {
        return property;
    }

    public void setProperty(List<String> property) {
        this.property = property;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(String ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getNutritionInfo() {
        return nutritionInfo;
    }

    public void setNutritionInfo(String nutritionInfo) {
        this.nutritionInfo = nutritionInfo;
    }

    public boolean isUnmatchedFood() {
        return unmatchedFood;
    }

    public void setUnmatchedFood(boolean unmatchedFood) {
        this.unmatchedFood = unmatchedFood;
    }

    public boolean isMissingFood() {
        return missingFood;
    }

    public void setMissingFood(boolean missingFood) {
        this.missingFood = missingFood;
    }

    public boolean isNutritionCalcError() {
        return nutritionCalcError;
    }

    public void setNutritionCalcError(boolean nutritionCalcError) {
        this.nutritionCalcError = nutritionCalcError;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }
    
    public String getUrn() {
        return this.urn;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    
    
}
