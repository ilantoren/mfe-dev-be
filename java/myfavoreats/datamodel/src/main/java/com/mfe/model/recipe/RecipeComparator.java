/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import java.util.Comparator;

/**
 *
 * @author richardthorne
 * @param <RecipePOJO>
 */
public class RecipeComparator implements Comparator<RecipePOJO> {

    @Override
    public int compare(RecipePOJO o1, RecipePOJO o2) {

		if ( o1.getRecipeChange() == null || o2.getRecipeChange() == null )
		return 0;
		
		return o1.getRecipeChange().getProbability().compareTo( o2.getRecipeChange().getProbability() );
	}
    

   
    
}
