package com.mfe.model.commoncrawl;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "recipeLink")
public class RecipeLink {
	
	public static Pattern  sitePattern = Pattern.compile("(?:http://)?([^\\/]+)");
	
	public RecipeLink( String url ){
		this.url = url;
		Matcher m = sitePattern.matcher(url);
		if (m.find()) {
			site = m.group(1);
		}
		errors = false;
	}
	String site;
	String url;
	Date crawled;
	Boolean errors;
	
	// framework constructor
	public RecipeLink() {};
}
	 

