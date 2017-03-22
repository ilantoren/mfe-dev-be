package com.mfe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.mfe.model.ingredient.EntityMapping;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.recipe.BadParameterException;
import com.mfe.model.recipe.Line;
import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.RecipeSub;
import com.mfe.model.recipe.RecipeSubsCalculation;
import com.mfe.model.recipe.RecipeSubsOption;
import com.mfe.model.recipe.Substitutions;
import com.mfe.model.utils.IngredientPOJOService;
import com.mfe.model.utils.IngredientSubstitution;
import com.mfe.model.utils.RecipeChangeService;

@Service
public class RecipeWithSubstituteServiceImpl implements RecipeWithSubstituteService {

	@Autowired
	IngredientPojoRepository ingredientPojoRepository;

	@Autowired
	SubstitutionsRepository substitutionsRepository;

	@Autowired
	EntityMappingRepository entityMappingRepository;

	@Autowired
	RecipeCalculationRepository recipeSubsCalculationRepository;

	@Autowired
	FileBasedIngredientPOJOService ingredientPOJOService;

	private RecipeChangeService recipeChangeService;

	private Logger log = Logger.getLogger(RecipeWithSubstituteServiceImpl.class);

	public RecipeWithSubstituteServiceImpl() {
		this.recipeChangeService = new RecipeChangeService();
	}

	@Override
	public List<RecipePOJO> getRecipeAndSubstitute(RecipePOJO pojo, String targetId) {
		recipeChangeService.setIngredientPOJOService(ingredientPOJOService);
		List<RecipePOJO> cached;
		String id = pojo == null ? null : pojo.getId();
		log.info( "Retrieving cached recipes substitution already calculated " + id );
		  if ( targetId.equals("NONE")) { 
			  log.info( "no target specified: using first substitution ");
			  cached =getCachedRecipeSubsCalculationByRecipe( pojo );
			  }else { 
				  log.info( "Substitution specified " + targetId );
	            cached = getCachedRecipeSubsCalculation( pojo, targetId );
		    }
		  
		  if(cached != null &&  cached.size() > 1 ) {
			  log.info( "using cached recipe subs calculation " + pojo.getId() );
			  return cached;
		   }
		 
		String recipeId = pojo.getId();
		Substitutions substitutions = substitutionsRepository.findByRecipeId(recipeId);
		if (substitutions == null) {
			log.warn("No substitutions for " + pojo.getId());

			ArrayList<RecipePOJO> tmp = new ArrayList<RecipePOJO>();
			tmp.add(pojo);
			tmp.add(pojo);
			return tmp;
		}
		pojo.setSubs(substitutions.getSubs());
		fillInSubsPerLine(pojo);
		List<RecipePOJO> pojoList = new ArrayList<>();
		Optional<RecipeSub> recipeSub = pojo.getSubs().stream().filter(a -> subHasTargetId(a, targetId)).findFirst();
		recipeSub.ifPresent(x -> {
			String substitutionId = x.getUid();
			Optional<RecipeSubsOption> recipeSubsOpt = x.getOptions().stream()
					.filter(option -> option.getTargetId().equals(targetId)).findFirst();
			recipeSubsOpt.ifPresent(myoption -> {
				List<RecipePOJO> y = getRecipeAndSubstitute(pojo, substitutionId, myoption);
				pojoList.addAll(y);
			});
		});

		if (pojoList.isEmpty()) {
			Optional<RecipeSub> firstSub = pojo.getSubs().stream().findFirst();
			Optional<RecipeSubsOption> firstOption = findFirstOption(pojo);

			Logger.getLogger(getClass().getName()).info("NONE was target, using:  " + firstOption.get().getTarget());
			List<RecipePOJO> y = getRecipeAndSubstitute(pojo, firstSub.get().getUid(), firstOption.get());
			pojoList.addAll(y);
		}

		return pojoList;
	}

	private Optional<RecipeSubsOption> findFirstOption(RecipePOJO pojo) {

		Optional<RecipeSub> sub = pojo.getSubs().stream().findFirst();
		if (sub.isPresent()) {
			return sub.get().getOptions().stream().findAny();
		}
		return Optional.empty();
	}

	private boolean subHasTargetId(RecipeSub recipeSub, String targetId) {
		Optional<RecipeSubsOption> optFound = recipeSub.getOptions().stream()
				.filter(opt -> opt.getTargetId().equals(targetId)).findFirst();
		return optFound.isPresent();
	}

	private List<RecipePOJO> getCachedRecipeSubsCalculationByRecipe(RecipePOJO pojo) {
		log.warn("getCachedRecipeSubsCalculationByRecipe off");
		/*ArrayList<RecipePOJO> results = new ArrayList<>();
		Optional<RecipeSubsCalculation> calculation = recipeSubsCalculationRepository.findByRecipeid(pojo.getId())
				.stream().findAny();
		calculation.ifPresent(recipeSubsCalculation -> {
			results.addAll(useRecipeSubsCalculation(pojo, recipeSubsCalculation));
		});*/
		return null;
	}

	private List<RecipePOJO> getCachedRecipeSubsCalculation(RecipePOJO pojo, String targetId) {
		log.warn( "getCachedRecipeSubsCalculation off");
		
		/*ArrayList<RecipePOJO> results = new ArrayList<>();
		Optional<RecipeSubsCalculation> calculation = recipeSubsCalculationRepository
				.findByRecipeIdAndTargetId(pojo.getId(), targetId).stream().findFirst();
		calculation.ifPresent(recipeSubsCalculation -> {
			results.addAll(useRecipeSubsCalculation(pojo, recipeSubsCalculation));
		});*/
		return null;
	}

	/**
	 * @param pojo
	 * @param recipeSubsCalculation
	 */
	public List<RecipePOJO> useRecipeSubsCalculation(RecipePOJO pojo, RecipeSubsCalculation recipeSubsCalculation) {
		ArrayList<RecipePOJO> results = new ArrayList<>();
		RecipePOJO substitute = pojo.clone();
		substitute.setRecipeChange(recipeSubsCalculation.getRecipeChange());
		substitute.setNutrients(recipeSubsCalculation.getNutrients());
		results.add(0, substitute);
		results.add(1, pojo);
		return results;
	}

	@Override
	public List<RecipePOJO> getRecipeAndSubstitute(RecipePOJO pojo) {
		return getRecipeAndSubstitute(pojo, "NONE");
	}

	@Override
	public List<RecipePOJO> getRecipeAndSubstitute(RecipePOJO pojo, String substitutionId, RecipeSubsOption option) {
		List<RecipePOJO> recipeList = new ArrayList<>();
		try {
			Substitutions recipeSubs = substitutionsRepository.findByRecipeId(pojo.getId());
			if (recipeSubs == null) {
				log.error("No substitutions found for  recipe " + pojo.getId());
			} else {
				pojo.setSubs(recipeSubs.getSubs());
				fillInSubsPerLine(pojo);
			}
			recipeChangeService.calculateRecipeNutrition(pojo);

			RecipePOJO substitutedRecipe = pojo.clone();
			RecipeSubsCalculation subCalculation = recipeChangeService.createRecipeWithSubstitute(pojo, substitutionId,
					option);

			if (subCalculation == null) {
				return new ArrayList<RecipePOJO>();
			}
			recipeChangeService.changeRecipeWithSubstitution(substitutedRecipe, subCalculation);
			substitutedRecipe.setNutrients(subCalculation.getNutrients());
			substitutedRecipe.setRecipeChange(subCalculation.getRecipeChange());
			substitutedRecipe.setSubstitutionRule(subCalculation.getDescription());
			recipeList.add(substitutedRecipe);
			recipeList.add(pojo);
		} catch (BadParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recipeList;
	}

	// @Override
	// public IngredientPOJO getById(String id) {
	// return ingredientPojoRepository.findById( id );
	// }

	private void fillInSubsPerLine(RecipePOJO pojo) {
		Map<String, Set<RecipeSubsCalculation>> subs = recipeChangeService.getAllRecipeSubstitutes(pojo);
		subs.values().forEach(a -> {
			saveSubsCalculation(a);
		});
		for (final Line line : RecipePOJO.getIngredientLines(pojo)) {
			List<IngredientSubstitution> mysubs = new ArrayList<>();
			Set<RecipeSubsCalculation> lineSubs = null;
			if (subs.values() != null || subs.values().size() > 0)
				lineSubs = subs.get(line.getUid() );
			if (lineSubs == null || lineSubs.isEmpty() ) {
				log.warn(pojo.getId() + "   " +  line.getUid()  + "  has no valid substitutions");
				// skip
			} else {
				for (RecipeSubsCalculation subCalculation : lineSubs) {
					if( subCalculation == null || subCalculation.getTarget() == null ) {
						continue;
					}
					IngredientSubstitution sub = new IngredientSubstitution(subCalculation.getTarget(),
							subCalculation.getTargetId(), subCalculation.getProbability().toString());
					sub.setDescription(subCalculation.getDescription());
					if ( subCalculation.getRecipeChange() == null || subCalculation.getRecipeChange().getCarbohydrate() == null ) {
						log.warn( "carbohydrates are not set in " + subCalculation.getDescription() );
					}
					else {
						sub.setChoChange(subCalculation.getRecipeChange().getCarbohydrate().getChange().toString());
					}
					if (  subCalculation.getRecipeChange() == null ||subCalculation.getRecipeChange().getCalories() == null) {
						log.info("calories are not calculated in " + subCalculation.getDescription());
					} else {
						sub.setKcalChange(subCalculation.getRecipeChange().getCalories().getChange().toString());
					}

					mysubs.add(sub);
				}

			}
			line.setSubs(mysubs);
		}
	}

	private void saveSubsCalculation(Set<RecipeSubsCalculation> a) {
		if (a == null || recipeSubsCalculationRepository == null)
			return;
		recipeSubsCalculationRepository.insert(a);

	}

	

	@Override
	public List<RecipeSubsCalculation> getAllRecipeCalculations(RecipePOJO pojo) {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	// public IngredientPOJO getByEntityMapping(String entityId) {
	// this.recipeChangeService.setIngredientPOJOService(this);
	//
	// EntityMapping entity = entityMappingRepository.findById( entityId );
	// if ( entity == null || entity.getNdb_no() == null ) {
	// //
	// return null;
	// }
	// else {
	// String ndb = entity.getNdb_no();
	// return ingredientPojoRepository.findById( ndb );
	// }
	// }

}
