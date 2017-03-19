package com.mfe.model.recipe;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "substitutions")
@JsonInclude(Include.NON_EMPTY)
public class Substitutions {
	@Id
	private String id;
	private String recipeId;
	List<RecipeSub> subs;
	
	public Substitutions() {
		this.subs = new ArrayList<>();
	}

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

	public List<RecipeSub> getSubs() {
		return subs;
	}

	public void setSubs(List<RecipeSub> subs) {
		this.subs = subs;
	}

	
}
