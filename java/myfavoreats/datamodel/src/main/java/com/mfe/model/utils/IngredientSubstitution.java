/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.io.Serializable;

/**
 *
 * @author richardthorne
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "substitution")
public class IngredientSubstitution implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private  String substituteFor;
    private  String probability;
    private  String id;
    private String targetId;
    private String description;
    private String choChange;
    private String kcalChange;

    public IngredientSubstitution(String subFor, String targetId, String prob) {
        this.substituteFor = subFor;
        this.probability = prob;
        this.targetId = targetId;
    }

    public String getSubstituteFor() {
        return substituteFor;
    }

    public void setSubstituteFor(String substituteFor) {
        this.substituteFor = substituteFor;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    
    
    
    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChoChange() {
		return choChange;
	}

	public void setChoChange(String choChange) {
		this.choChange = choChange;
	}
	
	
	

	public String getKcalChange() {
		return kcalChange;
	}

	public void setKcalChange(String kcalChange) {
		this.kcalChange = kcalChange;
	}

	public IngredientSubstitution() {}
}
