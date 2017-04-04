package com.mfe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	IngredientPOJOService ingredientPOJOService;

	private RecipeChangeService recipeChangeService;

	private Logger log = Logger.getLogger(RecipeWithSubstituteServiceImpl.class);

	

	@Override
	/*  Create two recipes,  substituted recipe first then original recipe
	 * (non-Javadoc)
	 * @see com.mfe.RecipeWithSubstituteService#getRecipeAndSubstitute(com.mfe.model.recipe.RecipePOJO, java.lang.String)
	 */
	public List<RecipePOJO> getRecipeAndSubstitute(RecipePOJO pojo, String optionId) {
		
		
		List<RecipePOJO> cached;
		String recipeId = pojo == null ? null : pojo.getId();
		
		log.info( "Attempting to retrieve cached recipes substitution already calculated for recipePOJO " + recipeId );
		  if ( optionId.equals("NONE")) { 
			  log.info( "recipeId: " + recipeId + " no optionUid specified: looking for first cached substitution ");
			  cached =getCachedRecipeSubsCalculationByRecipe( pojo );
			  }else { 
				  log.info( "Substitution specified " + optionId );
	            cached = getCachedRecipeSubsCalculation( pojo, optionId );
		    }
		  
		  if(cached != null &&  cached.size() > 1 ) {
			  log.info( "using cached recipe subs calculation " + pojo.getId() );
			  return cached;
		   }
		   
		  
		  /* processing in case where there is no cached recipe */
		 
		
		Substitutions substitutions = substitutionsRepository.findByRecipeId(recipeId);
		if (substitutions == null) {
			log.warn("No substitutions for recipeId of " + recipeId );

			ArrayList<RecipePOJO> tmp = new ArrayList<RecipePOJO>();
			tmp.add(pojo);
			tmp.add(pojo);
			return tmp;
		}
		pojo.setSubs(substitutions.getSubs());
		List<RecipePOJO> pojoList = new ArrayList<>();
		
		// Look for the first Substitutions (RecipeSub) that contains an optionId matching the request optionUid
		// if not provided then recipeSub will be Optional.empty
		Optional<RecipeSub> recipeSub = pojo.getSubs().stream().filter(a -> subHasOptionId(a, optionId)).findFirst();
		recipeSub.ifPresent(x -> {
			String subId = x.getUid();
			String lineId = x.getInstanceId();
			String message = String.format( "Found recipeSub %s by optionId %s  affecting line uid %s", subId, optionId, lineId );
			log.info( message );
			Optional<RecipeSubsOption> recipeSubsOpt = x.getOptions().stream()
					.filter(option -> option.getUid().equals(optionId)).findFirst();
			recipeSubsOpt.ifPresent(myoption -> {
				List<RecipePOJO> y = getRecipeAndSubstitute(pojo, x, myoption);
				pojoList.addAll(y);
			});
		});

		if (pojoList.isEmpty()) {
			Optional<RecipeSub> firstSub = pojo.getSubs().stream().findFirst();
			Optional<RecipeSubsOption> firstOption = findFirstOption(pojo);

			Logger.getLogger(getClass().getName()).info("No optionId was provided, using optionId :  " + firstOption.get().getUid());
			List<RecipePOJO> y = getRecipeAndSubstitute(pojo, firstSub.get(), firstOption.get());
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

	private boolean subHasOptionId(RecipeSub recipeSub, String optionId) {
		Optional<RecipeSubsOption> optFound = recipeSub.getOptions().stream()
				.filter(opt -> opt.getUid().equals(optionId)).findFirst();
		return optFound.isPresent();
	}
	private List<RecipePOJO> getCachedRecipeSubsCalculationByRecipe(RecipePOJO pojo) {
		String recipeId = pojo.getId();
		log.info( "recipeId: " + recipeId + "getCachedRecipeSubsCalculationByRecipe(RecipePOJO pojo)");
		Date start = new Date();
		ArrayList<RecipePOJO> results = new ArrayList<>();
		Optional<RecipeSubsCalculation> calculation = recipeSubsCalculationRepository.findByRecipeid(recipeId)
				.stream().findAny();
		calculation.ifPresent(recipeSubsCalculation -> {
			try {
				List<RecipePOJO> list = useRecipeSubsCalculation(pojo, recipeSubsCalculation );
				results.add(0, list.get(0));
				results.add(1, list.get(1));
			} catch (BadParameterException e) {
				log.error("", e);  //TODO
			}
		});
		Long elapsed = new Date().getTime() - start.getTime();
		log.info("recipeId " + recipeId +  " getCachedRecipeSubsCalculationByRecipe(RecipePOJO) elapsed:" + elapsed + "ms" );
		return results;
	}

	private List<RecipePOJO> getCachedRecipeSubsCalculation(RecipePOJO pojo, String optionId) {
		
		ArrayList<RecipePOJO> results = new ArrayList<>();
		RecipeSubsCalculation calculation = recipeSubsCalculationRepository
				.findByOptionUid( optionId);
		if (  calculation != null ){
			List<RecipePOJO> list;
			try {
				list = useRecipeSubsCalculation(pojo, calculation );
				results.add(0, list.get(0));
				results.add(1, list.get(1));
			} catch (BadParameterException e) {
				log.error("", e);  //TODO
			}	
		}
		
		return results;
	}

	/**
	 * @param pojo
	 * @param recipeSubsCalculation
	 * @throws BadParameterException 
	 */
	public List<RecipePOJO> useRecipeSubsCalculation(RecipePOJO pojo, RecipeSubsCalculation recipeSubsCalculation) throws BadParameterException {
		String recipeId = pojo.getId();
		log.info("Using recipeSubsCalculation for recipeId: " + recipeId + "  "  + recipeSubsCalculation.getDescription()  );
		
		ArrayList<RecipePOJO> results = new ArrayList<>();
		recipeChangeService.calculateRecipeNutrition(pojo);
		Substitutions substitutions = substitutionsRepository.findByRecipeId(recipeId);
		if ( substitutions == null ) {
			log.warn( "recipeId " + recipeId + " missing substitutions");
		}
		else {
			pojo.setSubs(substitutions.getSubs());
			fillInSubsPerLine( pojo );
		}
		
		RecipePOJO substituteRecipe = pojo.clone();
		substituteRecipe.setId( pojo.getId() );
		recipeChangeService.changeRecipeWithSubstitution(substituteRecipe, recipeSubsCalculation);
		results.add(0, substituteRecipe );
		results.add(1, pojo);
		return results;
	}

	@Override
	public List<RecipePOJO> getRecipeAndSubstitute(RecipePOJO pojo) {
		return getRecipeAndSubstitute(pojo, "NONE");
	}
	
	public List<RecipePOJO> getRecipeAndSubstitute( RecipePOJO pojo, RecipeSubsCalculation recipeSubsCalculation ) {
		RecipeSub recipeSub = recipeSubsCalculation.getRecipeSub();
		RecipeSubsOption recipeSubsOption = recipeSubsCalculation.getOption();
		return getRecipeAndSubstitute( pojo, recipeSub, recipeSubsOption );
	}

	public List<RecipePOJO> getRecipeAndSubstitute(RecipePOJO pojo, RecipeSub substitution, RecipeSubsOption option) {
		List<RecipePOJO> recipeList = new ArrayList<>();
		final String recipeId = pojo.getId();
		RecipeSubsCalculation subCalculation = null;
	
			 try {
				subCalculation = recipeChangeService.createRecipeSubCalculation(pojo, substitution,
					option);
			} catch (Exception e1) {
				log.warn( "recipeId: " + recipeId , e1);
			}
		
		String message = String.format("recipeId %s -  substitution uid %s  and option uid  "
				, recipeId
				, substitution.getUid()
				, option.getUid() );
		if (  subCalculation == null  ) {
			log.warn( "There is no substitutionsCalculation available " + message);
			return  recipeList;
		}
		
		try {
			recipeChangeService.calculateRecipeNutrition(pojo);
			
			log.info( "fillInSubPerlLine recipeId " + recipeId);
			fillInSubsPerLine(pojo);
			log.info( "fillInSubPerlLine - completed recipeId " + recipeId);
			RecipePOJO substitutedRecipe = pojo.clone();
			substitutedRecipe.setId( recipeId + " sub");
			log.info( message );
			recipeChangeService.changeRecipeWithSubstitution(substitutedRecipe, subCalculation);
			
			substitutedRecipe.setRecipeChange(subCalculation.getRecipeChange());
			substitutedRecipe.setSubstitutionRule(subCalculation.getDescription());
			substitutedRecipe.setSubs(null);   // presenting the same recipe twice one with changes and original. 
			substitutedRecipe.setSubs( null );
			recipeList.add(0, substitutedRecipe);
			recipeList.add(1, pojo);
		} catch (Exception e) {
			log.error("FAILED: " + message, e);
		}
		return recipeList;
	}



	private void fillInSubsPerLine(RecipePOJO pojo) {
		Map<String, Set<RecipeSubsCalculation>> subs = recipeChangeService.getAllRecipeSubstitutes(pojo);

		subs.values().forEach(a -> {
			saveSubsCalculation(a);
		});
		for (final Line line : RecipePOJO.getIngredientLines(pojo)) {
			List<IngredientSubstitution> mysubs = new ArrayList<>();
			Set<RecipeSubsCalculation> lineSubs = null;
			if (subs.values() != null || subs.values().size() > 0)
				lineSubs = subs.get(line.getUid());
			if (lineSubs == null || lineSubs.isEmpty()) {
				// can happen often
				log.debug(pojo.getId() + "   " + line.getUid() + "  has no valid substitutions");
				// skip
			} else {
				// set of targets for this particular ingredient - make it TODO
				// unique for the targetId in order to reduce clutter
				// @todo   if more than one only show the highest probability
				HashSet<String> targetIdSet = new HashSet<>();
				for (RecipeSubsCalculation subCalculation : lineSubs) {
					if (subCalculation == null || subCalculation.getTarget() == null) {
						continue;
					}
					if (targetIdSet.add(subCalculation.getTarget())) {
						IngredientSubstitution sub = createSubCalculation(subCalculation);

						mysubs.add(sub);
					}
				}

			}
			line.setSubs(mysubs);
		}
	}

	/**
	 * @param subCalculation
	 * @return
	 */
	protected IngredientSubstitution createSubCalculation(RecipeSubsCalculation subCalculation) {
		IngredientSubstitution sub = new IngredientSubstitution(subCalculation.getTarget(),
				subCalculation.getTargetId(), subCalculation.getOption().getUid(),
				subCalculation.getProbability().toString());
		sub.setDescription(subCalculation.getDescription());
		if (subCalculation.getRecipeChange() == null
				|| subCalculation.getRecipeChange().getCarbohydrate() == null) {
			log.warn("carbohydrates are not set in " + subCalculation.getDescription());
		} else {
			sub.setChoChange(subCalculation.getRecipeChange().getCarbohydrate().getChange().toString());
		}
		if (subCalculation.getRecipeChange() == null
				|| subCalculation.getRecipeChange().getCalories() == null) {
			log.info("calories are not calculated in " + subCalculation.getDescription());
		} else {
			sub.setKcalChange(subCalculation.getRecipeChange().getCalories().getChange().toString());
		}
		return sub;
	}

	private void saveSubsCalculation(Set<RecipeSubsCalculation> a) {
		if (a == null || recipeSubsCalculationRepository == null)
			return;
		recipeSubsCalculationRepository.insert(a);

	}

	

	@Override
	/*
	 * (non-Javadoc)
	 * @see com.mfe.RecipeWithSubstituteService#getAllRecipeCalculations(com.mfe.model.recipe.RecipePOJO)
	 */
	public List<RecipeSubsCalculation> getAllRecipeCalculations(RecipePOJO pojo) {
		log.error("NOT IMPLEMENTED");
		// TODO Auto-generated method stub
		return null;
	}
    Callable<Boolean> task = () ->{
    	log.info( "assigning autowire ingredientPOJOService to recipeChangeService");
    	do {
    		Thread.sleep( 100 );
    	}while( ingredientPOJOService == null );
    	this.recipeChangeService = new RecipeChangeService(ingredientPOJOService);
		
		return true;
    };
	
	@PostConstruct
	protected void postConstruct() {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(task);
	}

}
