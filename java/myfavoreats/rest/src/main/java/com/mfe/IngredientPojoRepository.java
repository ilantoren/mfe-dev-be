package com.mfe;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mfe.model.ingredient.IngredientPOJO;

public interface IngredientPojoRepository extends MongoRepository<IngredientPOJO, String> {
	@Query( value = "{id: ?0}")
	public IngredientPOJO  findById( String ndb );
	
}
