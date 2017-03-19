package com.mfe;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.mfe.model.recipe.RecipeSubsCalculation;

public interface RecipeCalculationRepository extends MongoRepository<RecipeSubsCalculation, String> {
	
	@Query( value ="{$and: [ {recipeId: ?0 }, {'option.targetId' : ?1 }]}")
	public List<RecipeSubsCalculation> findByRecipeIdAndTargetId( String recipeId,  String targetId );
	
	
	@Query( value ="{ recipeId: ?0  }") 
	public List<RecipeSubsCalculation> findByRecipeid( String recipeId );

	

}
