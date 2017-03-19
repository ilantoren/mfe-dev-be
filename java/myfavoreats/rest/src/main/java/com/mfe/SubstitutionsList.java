package com.mfe;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mfe.model.demo.DropDownTitle;
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "substitutionsList")
@JsonInclude(Include.NON_EMPTY)
public class SubstitutionsList {
	@Id
	String id;
	Date created;
	List<DropDownTitle> list;

	public SubstitutionsList() {
		this.created = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public List<DropDownTitle> getList() {
		return list;
	}

	public void setList(List<DropDownTitle> list) {
		this.list = list;
	}
	
	
	
	
}
