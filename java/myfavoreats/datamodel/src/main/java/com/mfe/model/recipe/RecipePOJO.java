/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Generated;
import org.springframework.data.annotation.Id;



/**
 *
 * @author freda
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "recipe")
@JsonInclude(Include.NON_EMPTY)
public class RecipePOJO extends BaseRecipe {
    
    // utility constructor
    public RecipePOJO() {
       parentId = "";
    }
    
    
  
    @JacksonXmlProperty
    @JsonProperty
    @Id
    String id;  
    
    
    @JacksonXmlProperty
    @JsonProperty
    String title;
    
    @JacksonXmlProperty
    @JsonProperty
    String warnings;
    
    @JacksonXmlProperty
    @JsonProperty
    String substitutionRule;
    
    @JacksonXmlProperty
    @JsonProperty
    String substitutionDescription;
    
    @JacksonXmlProperty
    @JsonProperty
    BigDecimal totalGrams = BigDecimal.ZERO;
    
    @JacksonXmlProperty
    @JsonProperty
    BigDecimal gramsPerPortion = BigDecimal.ZERO;
      
    @JacksonXmlProperty
    @JsonProperty
    String desc;
    
    @JacksonXmlProperty
    @JsonProperty
    String contributor;
    
    @JacksonXmlProperty
    @JsonProperty
    String prepTime;
    
    @JacksonXmlProperty
    @JsonProperty
    String website;
    
    @JacksonXmlProperty
    @JsonProperty
    Integer servings = 1;
    
    @JacksonXmlProperty
    @JsonProperty
    String photos;
    
    @JacksonXmlProperty
    @JsonProperty
    String trafficLightSVG;
    
     @JacksonXmlElementWrapper(localName = "categories")
     @JacksonXmlProperty
     @JsonProperty
     String[] categories;
    
    @JacksonXmlElementWrapper(localName = "ratings")
    @JacksonXmlProperty
    @JsonProperty
    Rating ratings;
    
    @JacksonXmlElementWrapper(localName = "subRule")
    @JacksonXmlProperty
    @JsonProperty
    List<SubstitutionResults> substitutionResult;
   
    @JacksonXmlProperty
    @JsonProperty
    Integer ingredientCount;
    
    
     @JacksonXmlProperty
    @JsonProperty
     String urn;
     
     
    @JacksonXmlElementWrapper(localName = "steps")
    @JsonProperty
    @JacksonXmlProperty
    Collection<Step> steps = new ArrayList<>();
    
    @JacksonXmlProperty
    @JsonProperty
    /*  Changes made to original recipe  implement the substitution rule */
    RecipeChange   recipeChange;
    
    @JacksonXmlElementWrapper(localName = "nutrientProfile")
    @JacksonXmlProperty
    @JsonProperty
    NutrientProfile nutrients;
    
    @JacksonXmlProperty
    @JsonProperty
    private Boolean parseError;
    
    @JacksonXmlProperty
    @JsonProperty
    String parentId = "";
    
    @JacksonXmlElementWrapper(localName = "subs")
    List<RecipeSub> subs = new ArrayList<>();
    
    String site = "";
    
    public NutrientProfile getNutrients() {
        return nutrients;
    }

    public void setNutrients(NutrientProfile nutrients) {
        this.nutrients = nutrients;
    }

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public String getTrafficLightSVG() {
        return trafficLightSVG;
    }

    public void setTrafficLightSVG(String trafficLightSVG) {
        this.trafficLightSVG = trafficLightSVG;
    }

    public Rating getRatings() {
        return ratings;
    }

    public void setRatings(Rating ratings) {
        this.ratings = ratings;
    }

    @Override
    public Collection<Step> getSteps() {
        return steps;
    }

    
    


	public List<RecipeSub> getSubs() {
		return subs;
	}

	public void setSubs(List<RecipeSub> subs) {
		this.subs = subs;
	}

	public void setSteps(Collection<Step> steps) {
        this.steps = steps;
    }

    public BigDecimal getTotalGrams() {
        return totalGrams;
    }

    public void setTotalGrams(BigDecimal totalGrams) {
        this.totalGrams = totalGrams;
    }

    public BigDecimal getGramsPerPortion() {
        return gramsPerPortion;
    }

    public void setGramsPerPortion(BigDecimal gramsPerPortion) {
        this.gramsPerPortion = gramsPerPortion;
    } 
    
   public static String getHeader() {
       return "id, pid,rule,ruleDesc,title,servings,total, perPortion,ingreds," + new NutrientProfile().getSet().stream().collect(Collectors.joining(","));
   } 

    public Integer getIngredientCount() {
        return ingredientCount;
    }

    public void setIngredientCount(Integer ingredientCount) {
        this.ingredientCount = ingredientCount;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public String getSubstitutionRule() {
        return substitutionRule;
    }

    public void setSubstitutionRule(String substitutionRule) {
        this.substitutionRule = substitutionRule;
    }

    public String getSubsitutionDescription() {
        return substitutionDescription;
    }

    public void setSubstitutionDescription(String subsitutionDescription) {
        this.substitutionDescription = subsitutionDescription;
    }

    public RecipeChange getRecipeChange() {
        return recipeChange;
    }

    public void setRecipeChange(RecipeChange recipeChange) {
        this.recipeChange = recipeChange;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

	

	public List<SubstitutionResults> getSubstitutionResult() {
		return substitutionResult;
	}

	public void setSubstitutionResult(List<SubstitutionResults> substitutionResult) {
		this.substitutionResult = substitutionResult;
	}

	public String getSubstitutionDescription() {
		return substitutionDescription;
	}
	
	

public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

@Override
   public String toString() {
      ingredientCount = getSteps().stream().collect( Collectors.summingInt(this::ingredientsByStep));
      StringBuilder sb = new StringBuilder();
      sb.append( id ).append(",");
      sb.append(parentId).append(",");
      sb.append( substitutionRule ).append(",");
      sb.append(substitutionDescription ).append(",");
      sb.append("\"").append( title).append("\",");
      sb.append( servings ).append(',');
      sb.append( totalGrams ).append(",");
      sb.append( gramsPerPortion.toPlainString()).append(",");
      sb.append( ingredientCount ).append(",");
      sb.append(  this.nutrients.toString() );
      return sb.toString();
   }


    public Boolean hasParseError() {
        return parseError;
    }

    public void setParseError(Boolean parseError) {
        this.parseError = parseError;
    }
}
