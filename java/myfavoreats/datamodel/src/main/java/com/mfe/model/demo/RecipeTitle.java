/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mfe.model.recipe.RecipePOJO;

import java.io.Serializable;

/**
 *
 * @author richardthorne
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "recipeTitle")
public class RecipeTitle implements Serializable {
    private String id;
    private String title;
    private String urn;
	private String site;
	private String imageUrl;
    
   
    
    public RecipeTitle(){}

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

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }
    

    public RecipeTitle(String id, String title, String urn, String site, String imageUrl) {
        this.id = id;
        this.title = title;
        this.site = site;
        
        if (urn != null )
        	this.urn = urn.replaceAll( "\\s", "");
        
        if ( imageUrl != null )
        	this.imageUrl = imageUrl.replaceAll("\\s", "" );
    }

	public RecipeTitle(RecipePOJO a) {
		id = a.getId();
		title = a.getTitle();
		
		if ( a.getWebsite() == null) {
			site = a.getSite();
		}else {
			site = a.getWebsite();
		}
		
		
		if (a.getUrn() != null)
			this.urn = a.getUrn().replaceAll("\\s", "");

		if (a.getPhotos() != null)
			this.imageUrl = a.getPhotos().replaceAll("\\s", "");
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
    
    
}
