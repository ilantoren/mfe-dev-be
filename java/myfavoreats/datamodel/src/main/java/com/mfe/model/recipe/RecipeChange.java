/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author richardthorne
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "recipeChange")
public class RecipeChange {
    
    public RecipeChange() {}
    
     @JsonIgnore
      public static final  String[] flds  =  {"carbohydrate",  "fructose", "cholesterol",
          "protein", "satFat", "sodium",  "totalFat","sugars", "fiber",  "calories"};
     
     @JsonIgnore
     public static final  String[] mg  = { "cholesterol", "sodium" };
     
    @JsonIgnore
      private Pattern removeTrailingLetters = Pattern.compile( "\\D+$");
    
    @XmlElement
    private List<String> ingredients;
    @XmlElement
    private Double probability;
    @XmlElement
    private String substitutionRule;
    @XmlElement
    private boolean lowerFat = false;
    @XmlElement
    private boolean lowerCarb = false;
    @XmlElement
    private boolean lowerSodium = false;
    @XmlElement
    private boolean higherFiber = false;
    @XmlElement
    private boolean reducedCalories = false;
    @XmlElement
    private boolean higherProtein = false;

    @JacksonXmlElementWrapper(localName = "added")
    private List<String> addedIngredients = new ArrayList();
    
    @JacksonXmlElementWrapper(localName = "reduced")
    private List<String> reducedIngredients = new ArrayList();
    
    @JacksonXmlElementWrapper(localName = "carbohydrate")
    private NutrientChange carbohydrate;
    
    @JacksonXmlElementWrapper(localName = "totalFat")
    private NutrientChange totalFat;
    
    @JacksonXmlElementWrapper(localName = "protein")
    private NutrientChange protein;
    
    @JacksonXmlElementWrapper(localName = "satFat")
    private NutrientChange satFat;
    
    @JacksonXmlElementWrapper(localName = "sodium")
    private NutrientChange sodium;
    
    @JacksonXmlElementWrapper(localName = "sugars")
    private NutrientChange sugars;
    
    @JacksonXmlElementWrapper(localName = "calories")
    private NutrientChange calories;
    
    @JacksonXmlElementWrapper( localName = "fiber")
    private NutrientChange fiber;
    
    @JacksonXmlElementWrapper( localName = "cholesterol")
    private NutrientChange cholesterol;

    public List<String> getAddedIngredients() {
        return addedIngredients;
    }

    public void setAddedIngredients(List<String> addedIngredients) {
        this.addedIngredients = addedIngredients;
    }

    public List<String> getReducedIngredients() {
        return reducedIngredients;
    }

    public void setReducedIngredients(List<String> reducedIngredients) {
        this.reducedIngredients = reducedIngredients;
    }

    public NutrientChange getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(NutrientChange carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public NutrientChange getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(NutrientChange totalFat) {
        this.totalFat = totalFat;
    }

    public NutrientChange getProtein() {
        return protein;
    }

    public void setProtein(NutrientChange protein) {
        this.protein = protein;
    }

    public NutrientChange getSatFat() {
        return satFat;
    }

    public void setSatFat(NutrientChange satFat) {
        this.satFat = satFat;
    }

    public NutrientChange getSodium() {
        return sodium;
    }

    public void setSodium(NutrientChange sodium) {
        this.sodium = sodium;
    }

    public NutrientChange getSugars() {
        return sugars;
    }

    public void setSugars(NutrientChange sugars) {
        this.sugars = sugars;
    }

    public NutrientChange getCalories() {
        return calories;
    }

    public void setCalories(NutrientChange calories) {
        this.calories = calories;
    }

    public NutrientChange getFiber() {
        return fiber;
    }

    public void setFiber(NutrientChange fiber) {
        this.fiber = fiber;
    }

    public NutrientChange getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(NutrientChange cholesterol) {
        this.cholesterol = cholesterol;
    }

    public Pattern getRemoveTrailingLetters() {
        return removeTrailingLetters;
    }

    public void setRemoveTrailingLetters(Pattern removeTrailingLetters) {
        this.removeTrailingLetters = removeTrailingLetters;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public String getSubstitutionRule() {
        return substitutionRule;
    }

    public void setSubstitutionRule(String substitutionRule) {
        this.substitutionRule = substitutionRule;
    }

    
    public boolean isLowerFat() {
        return lowerFat;
    }

    public void setLowerFat(boolean lowerFat) {
        this.lowerFat = lowerFat;
    }

    public boolean isLowerCarb() {
        return lowerCarb;
    }

    public void setLowerCarb(boolean lowerCarb) {
        this.lowerCarb = lowerCarb;
    }

    public boolean isLowerSodium() {
        return lowerSodium;
    }

    public void setLowerSodium(boolean lowerSodium) {
        this.lowerSodium = lowerSodium;
    }

    public boolean isHigherFiber() {
        return higherFiber;
    }

    public void setHigherFiber(boolean higherFiber) {
        this.higherFiber = higherFiber;
    }

    public boolean isReducedCalories() {
        return reducedCalories;
    }

    public void setReducedCalories(boolean reducedCalories) {
        this.reducedCalories = reducedCalories;
    }

    public boolean isHigherProtein() {
        return higherProtein;
    }

    public void setHigherProtein(boolean higherProtein) {
        this.higherProtein = higherProtein;
    }
    
    
}
