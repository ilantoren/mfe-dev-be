package com.mfe.model.utils;

import com.mfe.model.ingredient.IngredientPOJO;

public interface IngredientPOJOService {
	
	public IngredientPOJO getById( String id );
	
	public IngredientPOJO getByEntityMapping( String entityId);

}
