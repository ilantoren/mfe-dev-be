package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "options")
@JsonInclude(Include.NON_EMPTY)
public class RecipeSubsOption {
	
	String target;
	String targetId;
	
	@JsonProperty( defaultValue = "0")
	String uid;
	
	@JsonProperty( defaultValue = "1.0")
	Double quantityRatio;
	
	@JsonProperty( defaultValue = "1.0")
	Double probability;
	
	public  RecipeSubsOption( String target ) {
		this.target = target;
	}
	
	public RecipeSubsOption() {}
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public Double getQuantityRatio() {
		if ( quantityRatio == null ) return 1.0;
		return quantityRatio;
	}
	public void setQuantityRatio(Double quantityRatio) {
		this.quantityRatio = quantityRatio;
	}

	public Double getProbability() {
		return probability;
	}

	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	} 
	
	
}
