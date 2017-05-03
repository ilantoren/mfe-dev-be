package com.mfe.model.commoncrawl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "foodCom")
public class FoodCom extends Recipe implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Log log = LogFactory.getLog( getClass() );

	
	public FoodCom( Recipe recipe ) {
	
			  Field[] fields = Recipe.class.getDeclaredFields();
			 Arrays.stream( fields ).forEach(f -> {
		     String fieldName = f.getName();
		     Field target;
			try {
				target = this.getClass().getField(fieldName);
				 target.set(recipe, f);
			} catch (NoSuchFieldException | SecurityException e) {
				log.error(e);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				log.error(e);
			} 
		    
			 });
	}
	}
