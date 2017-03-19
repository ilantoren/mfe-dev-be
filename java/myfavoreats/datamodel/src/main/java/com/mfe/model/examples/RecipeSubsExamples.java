package com.mfe.model.examples;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.RecipeSub;
import com.mfe.model.recipe.RecipeSubsOption;

import java.util.List;


public class RecipeSubsExamples {
	static final String subs_json = "recipeSubs.json";
	public static void main ( String[] args) {
		RecipePOJO pojo = new RecipePOJO();
		RecipeSub subs = new RecipeSub();
		subs.setProbability(0.93);
		subs.setSource("BOOK");
		subs.setVersion("0.8");
		subs.setSource("spam");
		String[] targets = { "tofu", "seitan", "peas", "tvp" };
	    List<RecipeSubsOption> opts =  Arrays.stream( targets ).map( x -> new RecipeSubsOption( x) ).collect( Collectors.toList() );
	    subs.setOptions( opts );
	    pojo.getSubs().add( subs );
	    ObjectMapper mapper = new ObjectMapper();
	    try {
			mapper.writeValue(new File("test2.json"), pojo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
