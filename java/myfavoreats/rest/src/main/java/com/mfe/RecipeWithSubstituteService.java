package com.mfe;

import java.util.List;

import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.RecipeSubsCalculation;
import com.mfe.model.recipe.RecipeSubsOption;

public interface RecipeWithSubstituteService {
	public List<RecipePOJO> getRecipeAndSubstitute(RecipePOJO pojo);
	public List<RecipePOJO> getRecipeAndSubstitute( RecipePOJO pojo, String optionId);
	public List<RecipePOJO> getRecipeAndSubstitute( RecipePOJO pojo, String substitutionId, RecipeSubsOption option);
	public List<RecipeSubsCalculation> getAllRecipeCalculations( RecipePOJO pojo );
}
