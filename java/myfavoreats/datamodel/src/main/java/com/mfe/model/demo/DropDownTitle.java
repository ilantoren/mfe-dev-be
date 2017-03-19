package com.mfe.model.demo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "data")
public class DropDownTitle implements Comparable<DropDownTitle> {
	public DropDownTitle(String sourceId, String targetId, String description) {
		this.description = description;
		this.targetId = targetId;
		this.sourceId = sourceId;
	}
	
	//  utility constructor
	public DropDownTitle(){}
	
	
	public String sourceId;
	public String targetId;
	public String description;
	@Override
	public int compareTo(DropDownTitle o) {
		return description.compareTo( o.description );
	}
	
	@Override
	public int hashCode() {
		return this.description.hashCode();
	}
	
}