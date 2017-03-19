package com.mfe.model.ingredient;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "factors")
public class Factor {

    @JacksonXmlProperty
    @JacksonXmlElementWrapper(localName = "uid")
    String uid;
    @JacksonXmlProperty
    @JacksonXmlElementWrapper(localName = "value")
    String value;

    public Factor(String value) {
		this.value = value;
	}

	public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public Factor() {}

}
