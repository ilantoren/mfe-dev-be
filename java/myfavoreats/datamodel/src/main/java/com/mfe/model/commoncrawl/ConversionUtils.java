/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.commoncrawl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mfe.model.recipe.Line;
import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.Step;
import com.mfe.model.utils.IngredientSubstitution;

/**
 *
 * @author richardthorne
 */
public class ConversionUtils {
    public static RecipePOJO convertToPojo( Recipe recipe ) {
        RecipePOJO pojo = new RecipePOJO();
        pojo.setTitle(  recipe.getOgTitle());
        
        if( recipe.getOgUrl() == null ) 
            pojo.setUrn(  recipe.getUrn() );
        else
            pojo.setUrn( recipe.getOgUrl());
        
        pojo.setPhotos(recipe.getOgImage());
        pojo.setParentId(recipe.getId());
        pojo.setDesc(recipe.getOgDescription());
        pojo.setContributor( recipe.getAuthor());
        pojo.setPrepTime(recipe.getPrepTime());
        pojo.setCategories( recipe.getCategory().toArray(new String[recipe.getCategory().size()]));
        pojo.setWebsite(recipe.getOgSite());
        List<Line> ingredients = recipe.getIngredient().stream().map( i -> convertToIngredientLine( i )).collect( toList() );
        
        
        List<Step>  steps = recipe.recipeInstruction.stream().map( s -> new Step(s) ).collect( toList() );
        Optional<Step> firstStep = steps.stream().findFirst();
        if ( firstStep.isPresent()) {
            firstStep.get().setLines(ingredients);  // for now put all the lines into the first step
            pojo.setSteps(steps);
        }
        else {
            pojo.setParseError(true);
            Step s2 = new Step( "no instructions");
            s2.setLines(ingredients);
            log.warn( recipe.getId() + "  has parse error" );
        }
       
        return pojo;
    }

    private static Line convertToIngredientLine(Ingredient i) {
        Line pojo = new Line();
        pojo.setOriginal(i.getOriginal());
        pojo.setFood(i.getCannonical());
        pojo.setMeasure(i.getPortionText());
        pojo.setQuantity(i.getAmount());
        if ( i.getSubs() == null || i.getSubs().isEmpty() ) {
            // nothing
        }
        else {
            List<IngredientSubstitution> z = i.getSubs().stream().collect( Collectors.toList() );
            pojo.setSubs( z );
        }
        return pojo;
    }
    
    static Log log = LogFactory.getLog(ConversionUtils.class);
    
}
