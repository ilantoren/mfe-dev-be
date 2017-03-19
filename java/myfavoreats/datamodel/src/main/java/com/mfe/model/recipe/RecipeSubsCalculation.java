package com.mfe.model.recipe;

import java.util.Date;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "recipeSubsCalculation")
@JsonInclude(Include.NON_EMPTY)
public class RecipeSubsCalculation {
	
	public RecipeSub getRecipeSub() {
		return recipeSub;
	}
	public void setRecipeSub(RecipeSub recipeSub) {
		this.recipeSub = recipeSub;
	}
	public RecipeSubsOption getOption() {
		return option;
	}
	public void setOption(RecipeSubsOption option) {
		this.option = option;
	}
	public void setSubstitutionId(String substitutionId) {
		this.substitutionId = substitutionId;
	}


	@Id
	String id;
	String recipeId;
	String substitutionId;
	Date created;
	String description;
	NutrientProfile nutrients;
	RecipeChange recipeChange;
	RecipeSub  recipeSub;
	RecipeSubsOption option;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRecipeId() {
		return recipeId;
	}
	public void setRecipeId(String recipeId) {
		this.recipeId = recipeId;
	}
	
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public NutrientProfile getNutrients() {
		return nutrients;
	}
	public void setNutrients(NutrientProfile nutrients) {
		this.nutrients = nutrients;
	}
	public RecipeChange getRecipeChange() {
		return recipeChange;
	}
	public void setRecipeChange(RecipeChange recipeChange) {
		this.recipeChange = recipeChange;
	}
	
	
	public String getSource() {
		return recipeSub.getSource();
	}
	public String getSourceId() {
		return recipeSub.getSourceId();
	}
	public String getInstanceId() {
		return recipeSub.getInstanceId();
	}
	public Double getProbability() {
		return recipeSub.getProbability();
	}
	public String getTarget() {
		return option.getTarget();
	}
	public String getTargetId() {
		return option.getTargetId();
	}
	public Double getQuantityRatio() {
		return option.getQuantityRatio();
	}
	public String getSubstitutionId() {
		if ( substitutionId == null ) return "";
		return substitutionId;
	}
	
	
	public RecipeSubsCalculation(RecipePOJO recipePojo, String substitutionId, RecipeSubsOption option) {
		this.created = new Date();
		this.option = option;
		this.recipeId = recipePojo.getId();
		this.substitutionId = substitutionId;
		this.recipeSub = null;
		if (substitutionId == null && recipePojo.getSubs().size() > 0) {
			Logger.getLogger( getClass().getName() ).fine( "substitutionId is null: recipeId " + recipePojo.getId()   );
			this.substitutionId = recipePojo.getSubs().get(0).getUid();
		}
		if (this.substitutionId != null) {
			Optional<RecipeSub> sub = recipePojo.getSubs().stream().filter(s -> s.getUid().equals(getSubstitutionId()))
					.findFirst();
			sub.ifPresent(s -> {
				this.recipeSub = s;
				this.description = String.format("%s for %s", s.getSource(), option.getTarget());
			});
		}

	}
	
	
	// default constructor
	public RecipeSubsCalculation() {};
}
