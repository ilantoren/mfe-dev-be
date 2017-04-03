/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mfe.model.utils.IngredientSubstitution;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author freda
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "lines")
public class Line {

    public Line() {}
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(uid).append(",");
        sb.append(recipe).append(",");
        sb.append("\"").append(food).append("\",");
        sb.append(ndb).append(",");
        sb.append(quantity).append(",");
        BigDecimal niceGram = new BigDecimal( gram ).divide(BigDecimal.ONE,3, RoundingMode.UP);
        sb.append(niceGram).append("\n");
        return sb.toString();
    }

    @JacksonXmlProperty
    @JsonProperty
    Integer recipe;

    @JacksonXmlProperty
    @JsonProperty
    String uid;

    @JacksonXmlProperty
    @JsonProperty
    String desc;

    @JacksonXmlProperty
    @JsonProperty
    String measure;


    @JacksonXmlProperty
    @JsonProperty
    String quantity;

    @JacksonXmlProperty
    @JsonProperty
    String food;

    @JacksonXmlProperty
    @JsonProperty
    Double gram;

    @JacksonXmlProperty
    @JsonProperty
    String ndb;

    @JacksonXmlProperty
    @JsonProperty
    private BigDecimal numericQuantity;

    @JacksonXmlProperty
    @JsonProperty
    String protein;

    @JacksonXmlProperty
    @JsonProperty
    String calories;

    @JacksonXmlProperty
    @JsonProperty
    String sodium;

    @JacksonXmlProperty
    @JsonProperty
    String cholesterol;

    @JacksonXmlProperty
    @JsonProperty
    String carbohydrates;

    @JacksonXmlProperty
    @JsonProperty
    String totalFat;

    @JacksonXmlProperty
    @JsonProperty
    String satFat;

    @JacksonXmlProperty
    @JsonProperty
    String mod;

    @JacksonXmlProperty
    @JsonProperty
    Integer step;

    @JacksonXmlProperty
    @JsonProperty
    String full;

    @JacksonXmlProperty
    @JsonProperty
    Boolean updated;
    
    @JacksonXmlProperty
    @JsonProperty
    String original;
    
    String cannonical;
    
    String entityId;
    
    
    @JacksonXmlElementWrapper(localName = "substitution")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    private List<IngredientSubstitution> subs = new ArrayList<>();

	private String water;

    public List<IngredientSubstitution> getSubs() {
        return subs;
    }

    public void setSubs(List<IngredientSubstitution> subs) {
        this.subs = subs;
    }

    

    public Integer getRecipe() {
        return recipe;
    }

    public void setRecipe(Integer recipe) {
        this.recipe = recipe;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public Double getGram() {
        return gram==null ? 0D : gram;
    }

    public void setGram(Double gram) {
        this.gram = gram;
    }

    public String getNdb() {
        return ndb;
    }

    public void setNdb(String ndb) {
        this.ndb = ndb;
    }

    public String getProtein() {
        return protein;
    }

    public void setProtein(String protein) {
        this.protein = protein;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getSodium() {
        return sodium;
    }

    public void setSodium(String sodium) {
        this.sodium = sodium;
    }

    public String getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(String cholesterol) {
        this.cholesterol = cholesterol;
    }

    public String getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(String carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public String getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(String totalFat) {
        this.totalFat = totalFat;
    }

    public String getMod() {
        return mod;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }

    public String getFull() {
        return full;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public Boolean getUpdated() {
        return updated;
    }

    public void setUpdated(Boolean updated) {
        this.updated = updated;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }
    
    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public BigDecimal getNumericQuantity() {
        return numericQuantity;
    }

    public void setNumericQuantity(BigDecimal numericQuantity) {
        this.numericQuantity = numericQuantity;
    }

    public String getSatFat() {
        return satFat;
    }

    public void setSatFat(String satFat) {
        this.satFat = satFat;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getCannonical() {
        return cannonical;
    }

    public void setCannonical(String cannonical) {
        this.cannonical = cannonical;
    }

	public void setWater(String water) {
		this.water = water;		
	}
	
	public String getWater() {
		return this.water;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	@Override
	public Line clone() throws CloneNotSupportedException {
		ObjectMapper m = new ObjectMapper();
		StringWriter w = new StringWriter();
		Line result = null;
		try {
			m.writeValue(w, this);
			result = m.readValue( w.toString(), Line.class );
		} catch (IOException e) {
			
			throw new CloneNotSupportedException(  e.getMessage() );
		}
		return result;
	}
    
    
     
}
