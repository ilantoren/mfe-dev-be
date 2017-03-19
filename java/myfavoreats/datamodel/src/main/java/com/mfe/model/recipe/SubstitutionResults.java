package com.mfe.model.recipe;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "substitutionResult")
public class SubstitutionResults {
	String id;
	String substitutionRule;
	String probability = "";
	String carbohydrate = "nc";
	String calories = "nc";
	
	public SubstitutionResults() {}
	
	public SubstitutionResults(String id, RecipeChange recipeChange) {
		this.id = id;
		this.substitutionRule = recipeChange.getSubstitutionRule();
		this.probability = asString( recipeChange.getProbability());
		if ( recipeChange.getCalories() != null )
			this.calories = asString( recipeChange.getCalories().getPercentChange());
		
		if ( recipeChange.getCarbohydrate() != null )
			this.carbohydrate = asString( recipeChange.getCarbohydrate().getPercentChange());
		
	}
	
	@Override
	public String toString() {
		return String.format( "%s - %s  %s kcal %s cho", substitutionRule, probability, calories, carbohydrate );
	}
	
	private String asString( Double value ) {
     if ( value == null ) return "nc";
	 return	BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(100D)).divide(BigDecimal.ONE, BigDecimal.ROUND_UP, 0).toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubstitutionRule() {
		return substitutionRule;
	}

	public void setSubstitutionRule(String substitutionRule) {
		this.substitutionRule = substitutionRule;
	}

	public String getProbability() {
		return probability;
	}

	public void setProbability(String probability) {
		this.probability = probability;
	}

	public String getCarbohydrate() {
		return carbohydrate;
	}

	public void setCarbohydrate(String carbohydrate) {
		this.carbohydrate = carbohydrate;
	}

	public String getCalories() {
		return calories;
	}

	public void setCalories(String calories) {
		this.calories = calories;
	}
	
	
}
