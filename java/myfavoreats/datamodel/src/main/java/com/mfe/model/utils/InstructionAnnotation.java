/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mfe.model.commoncrawl.Recipe;
import com.mfe.model.recipe.RecipePOJO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.annotation.Id;

/**
 *
 * @author richardthorne
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "instructionAnnotation")
public class InstructionAnnotation implements Serializable{
    
    @Id
    private String id;
    
    private String recipeId;
    
    private String sourceText;
    private String annotated;
    private String replacedText;
    
    public void setRecipe( Recipe r) {
        recipeId =  r.getId(); 
        String instructions = r.getRecipeInstruction().stream().collect(Collectors.joining( " " ));
        sourceText = instructions;
    }

   public void setRecipePOJO( RecipePOJO r ) {
       List<String> instructions = new ArrayList<>();
       r.getSteps().forEach(step -> {
           instructions.add(  step.getInstruction() );
       });
       recipeId = r.getId();
       sourceText =  instructions.stream().collect( Collectors.joining( " "));
   }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getAnnotated() {
        return annotated;
    }

    public void setAnnotated(String annotated) {
        this.annotated = annotated;
    }

    public String getReplacedText() {
        return replacedText;
    }

    public void setReplacedText(String replacedText) {
        this.replacedText = replacedText;
    }

    public InstructionAnnotation() {}
    
}
