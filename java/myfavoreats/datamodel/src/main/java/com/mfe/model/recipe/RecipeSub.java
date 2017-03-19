package com.mfe.model.recipe;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "recipeSub")
@JsonInclude(Include.NON_EMPTY)
public class RecipeSub {
	private static final String MANUAL = "MANUAL";
	@Id
	String uid;
	String description;
	String source;
	String sourceId;
	String type;
	@JsonProperty( value="instanceId")
	String instanceId;  //internalId
	Double probability;
	@JsonProperty( defaultValue = "MANUAL" )
	String origin;
	@JsonProperty( value = "Version")
	String version;
	String infolink;
	String workerId;
	String moreinfo;
	
	List<RecipeSubsOption> options = new ArrayList<>();
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
	
	
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public Double getProbability() {
		if ( probability == null ) return 1.0;
		return probability;
	}
	public void setProbability(Double probability) {
		this.probability = probability;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getInfolink() {
		return infolink;
	}
	public void setInfolink(String infolink) {
		this.infolink = infolink;
	}
	
	public String getWorkerId() {
		return workerId;
	}
	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
	public List<RecipeSubsOption> getOptions() {
		return options;
	}
	public void setOptions(List<RecipeSubsOption> options) {
		this.options = options;
	}
	public String getMoreinfo() {
		return moreinfo;
	}
	public void setMoreinfo(String moreinfo) {
		this.moreinfo = moreinfo;
	}
	
	
	

}
