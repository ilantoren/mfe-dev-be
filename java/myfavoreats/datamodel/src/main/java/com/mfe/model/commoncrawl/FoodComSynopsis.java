package com.mfe.model.commoncrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "foodComSynopsis")
public class FoodComSynopsis {
	@JsonProperty("record_spec")
	String recordSpec;
	
	@JsonProperty("all-category-list")
	String allCategoryList;
	
	@JsonProperty( "main-title")
	String mainTitle;
	
	@JsonProperty("primary-category-name")
	String primaryCategoryName;
	
	@JsonProperty( "record-url")
	String recordUrl;
	
	@JsonProperty("has-photo")
	Boolean hasPhoto;
	
	@JsonProperty("main-description")
	String mainDescription;
	
	@JsonProperty("recipe-photo-url")
	String recipePhotoUrl;
	
	@JsonProperty("main-user-name")
	String mainUserName;
	
	@JsonProperty("recipe-link")
	String recipeLink;
}
