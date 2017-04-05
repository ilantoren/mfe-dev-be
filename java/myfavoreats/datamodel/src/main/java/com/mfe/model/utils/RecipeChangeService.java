/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.ingredient.IngredientService;
import com.mfe.model.recipe.BadParameterException;
import com.mfe.model.recipe.ChangeType;
import com.mfe.model.recipe.Line;
import com.mfe.model.recipe.NutrientChange;
import com.mfe.model.recipe.NutrientProfile;
import com.mfe.model.recipe.RecipeChange;
import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.RecipeSub;
import com.mfe.model.recipe.RecipeSubsCalculation;
import com.mfe.model.recipe.RecipeSubsOption;


/**
 *
 * @author richardthorne
 */

public class RecipeChangeService {
	
	
	public RecipeChangeService( IngredientPOJOService ingredientPOJOService ) throws Exception {
		if ( ingredientPOJOService == null ) throw new Exception( "Requires a valid IngredientPOJOService");
		this.ingredientPOJOService = ingredientPOJOService;
	}

	final protected IngredientPOJOService ingredientPOJOService;
	
	public Optional<IngredientPOJOService> getIngredientPojoService() {
		if ( ingredientPOJOService == null) 
			return Optional.empty();
		else
			return Optional.of(ingredientPOJOService);
	}
	

	
	Log log = LogFactory.getLog(getClass() );
	
	
    private static String getNutrientField(NutrientProfile diff, String field) {
        String value = "0";
       switch( field ) {
           case "carbohydrate":
               value = diff.getCarbohydrate();
               break;
           case "cholesterol":
               value = diff.getCholesterol();
               break;
           case "protein":
               value =  diff.getProtein();
               break;
           case "satFat": 
               value =  diff.getSatFat();
               break;
           case  "sodium":
               value =  diff.getSodium();
               break;
           case "totalFat":
               value =  diff.getTotalFat();
               break;
           case "sugars":
               value =  diff.getSugars();
               break;
           case "calories":
               value =  diff.getCalories();
               break;
           case "fiber":
               value = diff.getFiber();
               break;
       }
       if ( value.isEmpty()) {
           return "0";
       }
       return value.replaceAll("\\D+$", "");
    }


    public static final Double LOWER = 0.85, HIGHER = 1.15;

    private static void decreaseGood(NutrientChange nc) {
         if ( nc.getChangeType().equals(ChangeType.DECREASE)) {
             nc.setChangeType(ChangeType.BETTER);          
        } else if ( nc.getChangeType().equals(ChangeType.INCREASE)) {
            nc.setChangeType(ChangeType.WORSE);
        }
    }

    private static void increaseGood(NutrientChange nc) {
        if ( nc.getChangeType().equals(ChangeType.INCREASE)) {
             nc.setChangeType(ChangeType.BETTER);          
        } else if ( nc.getChangeType().equals(ChangeType.DECREASE)) {
            nc.setChangeType(ChangeType.WORSE);
        }
    }
   
   
    
   
    
    public  static RecipeChange  calculateChange( RecipePOJO pojo1,   RecipePOJO pojo2 ) throws BadParameterException, IllegalArgumentException, IllegalAccessException, IOException {
            RecipeChange changes = new RecipeChange();
           // changes.setProbability(Double.parseDouble(pojo2.getSubsitutionDescription() ));  // Desc field used to store the probability
            changes.setSubstitutionRule(pojo2.getSubstitutionRule());
            NutrientProfile  original  = pojo1.getNutrients();
            NutrientProfile modified = pojo2.getNutrients();
            changes.setTotalGrams( original.getTotalGrams() );
            changes.setServingSize(100D);
            if ( original == null || modified == null ) return null;
            Set<String> modifiedIngredients = RecipePOJO.getIngredientLines(  pojo2 ).stream().map( x -> x.getFood() ).collect( Collectors.toSet());
            Set<String> originalIngredients = RecipePOJO.getIngredientLines(  pojo1 ).stream().map( x -> x.getFood() ).collect( Collectors.toSet());
            
            List<String> addedToModified = modifiedIngredients.stream().filter(/* NOT */ x ->  ! originalIngredients.contains(x)).collect( Collectors.toList() );
            List<String> removedFromOriginal =  originalIngredients.stream().filter(/*NOT ON RIGHT*/ x -> ! modifiedIngredients.contains(x) ).collect(Collectors.toList() );
            changes.setReducedIngredients(removedFromOriginal);
            changes.setAddedIngredients(addedToModified);
            // "carbohydrate",  "fructose", "cholesterol",  "protein", "satFat", "sodium",  "totalFat","sugars", "fiber",  "calories"
            NutrientProfile diff = IngredientService.clone(modified);
            diff.add(original, -1D);  // same as subtraction   so that modified - original = change 
           
            
            NutrientProfile divided = IngredientService.clone(original);
            divided.divide(modified);
            
            NutrientChange carbohydrate = getChange( diff, divided,  "carbohydrate" );
            changes.setCarbohydrate(carbohydrate);
             
            NutrientChange cholesterol = getChange( diff, divided, "cholesterol" );
            changes.setCarbohydrate(cholesterol);
            
            NutrientChange protein = getChange( diff, divided,  "protein" );
            changes.setProtein(protein);
            
            NutrientChange satFat = getChange( diff, divided,  "satFat" );
            changes.setSatFat(satFat);
            
            NutrientChange sodium = getChange( diff, divided,  "sodium" );
            changes.setSodium(sodium);
            
            NutrientChange totalFat = getChange( diff, divided,  "totalFat" );
            changes.setTotalFat(totalFat);
            
            NutrientChange sugars = getChange( diff, divided,  "sugars" );
            changes.setSugars(sugars);
            
            NutrientChange fiber = getChange( diff, divided,  "fiber" );
            changes.setFiber(fiber);
            
            NutrientChange calories = getChange( diff, divided, "calories" );
            changes.setCalories(calories);
            
            return changes;
        
    } 
    
     private static NutrientChange getChange(NutrientProfile diff, NutrientProfile divided,  String field) {
        String diffValue = getNutrientField( diff, field );
        String divValue  = getNutrientField( divided, field );
        NutrientChange nc = new NutrientChange();
        nc.setName(field);
        
        
        Double divDoub =  BigDecimal.valueOf( Double.valueOf(divValue) ).divide(BigDecimal.ONE).doubleValue();
        Double diffDoub = BigDecimal.valueOf( Double.valueOf(diffValue)).divide(BigDecimal.ONE).doubleValue();
        if ( divDoub <= LOWER && diffDoub != 0) {
            nc.setChangeType(ChangeType.DECREASE);
        }else if ( divDoub >= HIGHER ){
            nc.setChangeType(ChangeType.INCREASE);
        }
        else  {
            nc.setChangeType( ChangeType.NOCHANGE );
        }
        nc.setChange(diffDoub);
        nc.setPercentChange(divDoub);
        return nc;
    }
     
     public static void prepareForDisplay( RecipeChange rc ) {
    	 if ( rc == null ) return;
         setForDisplay( rc.getCalories() );
         setForDisplay( rc.getCarbohydrate());
         setForDisplay( rc.getCholesterol() );
         setForDisplay( rc.getFiber() );
         setForDisplay( rc.getSatFat() );
         setForDisplay( rc.getSodium() );
         setForDisplay( rc.getSugars() );
         setForDisplay( rc.getTotalFat() );
         setForDisplay( rc.getProtein() );
     }
     
     private static void setForDisplay(final NutrientChange nc ) {
         if ( nc == null ) {
             return;
         }
         String field = nc.getName();
         switch( field ) {
           case "carbohydate":
              decreaseGood( nc );
               break;
           case "cholesterol":
              decreaseGood( nc );
               break;
           case "protein":
               increaseGood( nc );
               break;
           case "satFat": 
               decreaseGood( nc );
               break;
           case  "sodium":
               decreaseGood( nc );
           case "totalFat":
              decreaseGood( nc );
               break;
           case "sugars":
               decreaseGood( nc );
               break;
           case "calories":
               decreaseGood( nc );
               break;
           case "fiber" :
               increaseGood( nc );
       }
         
     }
     
	public RecipeSubsCalculation createRecipeSubCalculation(final RecipePOJO pojo, RecipeSub recipeSubstitution,
			RecipeSubsOption option) throws BadParameterException {
		final String recipeId = pojo.getId();
		if (recipeSubstitution == null) {
			return null;
		}
		// normalize recipe to the total of all the ingredients and set portion
		// to 100 g
		Optional<IngredientPOJOService> ingredientServiceOpt = getIngredientPojoService();
		RecipeSubsCalculation recipeSubCalculation = new RecipeSubsCalculation(pojo,
				recipeSubstitution.getUid(), option);
		ingredientServiceOpt.ifPresent(ingredientService -> {
			try {
				calculateRecipeNutrition(pojo, ingredientService);
				RecipePOJO substituteRecipe = pojo.clone();
				substituteRecipe.setId( recipeId + " sub" );

				
				changeRecipeWithSubstitution( substituteRecipe, recipeSubCalculation );
				recipeSubCalculation.setNutrients(substituteRecipe.getNutrients());
				RecipeChange recipeChange = calculateChange(pojo, substituteRecipe);
				recipeSubCalculation.setRecipeChange(recipeChange);
				
			} catch (BadParameterException | IllegalArgumentException | IllegalAccessException | IOException e1) {
				String message = String.format("recipe: %s  substitution: %s   option: %s", pojo.getId(),
						recipeSubstitution.getUid(), option.getUid());
				log.error(message, e1);
			}
		});

		return recipeSubCalculation ;
	}

	/**
	 * Take a cloned recipe prior to changing the ingredient line then change that
	 * 		 recipe according to the recipeSubCalculation
	 * @param substituteRecipe
	 * @param recipeSubCalculation
	 * @param ingredientLineId
	 */
	public boolean changeRecipeWithSubstitution(final RecipePOJO substituteRecipe,
			final RecipeSubsCalculation recipeSubCalculation) {
		String ingredientLineId = recipeSubCalculation.getInstanceId();
		String recipeId = substituteRecipe.getId();
		log.info( "changeRecipeWithSubstitution   recipe " + recipeId + " line instanceId " + ingredientLineId + " " + recipeSubCalculation.getDescription() );
		if (ingredientLineId == null) {
			log.warn(recipeSubCalculation.getDescription() + " has no valid instanceId ");
		}
		Optional<Line> ingredientLine = RecipePOJO.getIngredientLines(substituteRecipe).stream()
				.filter(a -> a.getUid() != null).filter(a -> a.getUid().equals(ingredientLineId)).findFirst();
		AtomicBoolean booleanObj = new AtomicBoolean(true);
		ingredientLine.ifPresent(x -> {
			
   		 		try {
					recipeSubCalculation.setOriginalLine( x.clone() );
				} catch (CloneNotSupportedException e) {
					log.error( "in changeRecipeWithSubstition " + substituteRecipe.getId(), e);
				}
			
		
			String sourceFood = recipeSubCalculation.getSource();
			String targetFood = recipeSubCalculation.getTarget();
			String targetId = recipeSubCalculation.getTargetId();
			substituteRecipe.setRecipeChange(recipeSubCalculation.getRecipeChange());
			x.setEntityId(targetId);
			IngredientPOJO targetIngredient = ingredientPOJOService.getByEntityMapping(targetId);
			if (targetIngredient == null) {
				log.warn(targetId + " does not point to a valid ingredientPOJO");
				booleanObj.set(false);
			} else {
				String replaceNdb = targetIngredient.getUid();
				if (x.getNdb() == null) {
					log.warn(x.getUid() + " " + x.getOriginal() + "  has null ndb value");
				}
				log.info("replacing ingredient " + x.getNdb() + "  with " + replaceNdb);
				x.setNdb(targetIngredient.getUid());

				x.setFood(targetFood);
				x.setCannonical(targetFood);
				log.info("recipeId" + recipeId+ " " + targetFood + " at " + recipeSubCalculation.getTargetId() );
						
				String replaceLine = x.getOriginal().replace(sourceFood, targetFood);
				x.setOriginal(replaceLine);
				recipeSubCalculation.setSubstitutedLine(x);
				try {
					calculateRecipeNutrition( substituteRecipe );
				} catch (BadParameterException e) {
					log.error( "Values missing or corrupted for nutrition calculation", e);
				}
			}
		});
		
		
		if (!ingredientLine.isPresent()) {
			log.warn(ingredientLineId + " is missing ");
			return false;
		}
		return booleanObj.get();
	}
     
     public void calculateRecipeNutrition( RecipePOJO pojo) throws BadParameterException {
    	 Optional<IngredientPOJOService> ingredientService = getIngredientPojoService();
    	 AtomicBoolean hasError = new AtomicBoolean(true);
    	
    	 ingredientService.ifPresent(service -> {
    		 try {
				calculateRecipeNutrition( pojo, service);
				hasError.set( false );
			} catch (BadParameterException e) {
				log.error(  "error in call", e);
			}
    	 });
    	 if ( hasError.get() ) {
    		 throw new BadParameterException( "ingredientPojoService is not set");
    	 }
     }
     
     
     
     public void calculateRecipeNutrition(RecipePOJO pojo, IngredientPOJOService ingredientPojo) throws BadParameterException {
    	 if ( pojo == null ) throw new BadParameterException( "pojo can not be null");
    	 final String recipeId = pojo.getId();
         Date start = new Date();
    	 NutrientProfile nutrients = new NutrientProfile();
         log.info( "calculating for recipePOJO " + recipeId + ": Start timer");

         List<Line> ingredients = (List<Line>) RecipePOJO.getIngredientLines(pojo);
         for (Line ingredient : ingredients) {
             String ndb = ingredient.getNdb();
             if (ndb == null || ingredient.getGram() == null) {
                 continue;
             }
             Double mult = BigDecimal.valueOf(ingredient.getGram()).divide(BigDecimal.valueOf(100d), 6, BigDecimal.ROUND_UP).doubleValue();
             IngredientPOJO ingred = ingredientPojo.getByEntityMapping(ingredient.getEntityId() );
             if (ingred == null) {
            	 log.warn("food: " + ingredient.getFood() + ":" + ingredient.getCannonical()  + " of " + recipeId + " : " + ingredient.getUid() + " no entity mapping");
                 continue;
             }
             nutrients.add(ingred, mult);
             NutrientProfile perIngredient = new NutrientProfile();
             perIngredient.add(ingred, mult);
             ingredient.setCalories(perIngredient.getCalories());
             ingredient.setCarbohydrates(perIngredient.getCarbohydrate());
             ingredient.setCholesterol(perIngredient.getCholesterol());
             ingredient.setProtein(perIngredient.getProtein());
             ingredient.setTotalFat(perIngredient.getTotalFat());
             ingredient.setSodium(perIngredient.getSodium());
             ingredient.setSatFat(perIngredient.getSatFat());
         }
         Double sumGram = ingredients.stream().mapToDouble(i -> i.getGram()).sum();
         pojo.setTotalGrams(BigDecimal.valueOf(sumGram));
         nutrients.setTotalGrams(sumGram);
         if (sumGram > 0) {
             BigDecimal scale = BigDecimal.valueOf(100d).divide(BigDecimal.valueOf(sumGram), 4, BigDecimal.ROUND_HALF_UP);
             //nutrients.setGramsPerPortion(100D);
             //log.info(  "total grams: " + sumGram + "  scale by " + scale  + " for 100 grams");
             //nutrients.scaleAll( scale.doubleValue());
             pojo.setNutrients(nutrients);
             pojo.setGramsPerPortion(BigDecimal.valueOf(100));
         }
         Long elapsed = new Date().getTime() - start.getTime();
         log.info( "Stop timer: elapsed " +  elapsed  + " ms");
     }
     
	public Map<String, Set<RecipeSubsCalculation>> getAllRecipeSubstitutes(RecipePOJO pojo) {
		Map<String, Set<RecipeSubsCalculation>> results = new HashMap<>();
		pojo.getSubs().forEach(s -> {
			String uid = s.getUid();
			String instanceId = s.getInstanceId(); // points to the line to in
													// the recipe to change
			Function<RecipeSubsOption, RecipeSubsCalculation> p = (x) -> {
				try {
					return createRecipeSubCalculation(pojo, s, x);
				} catch (BadParameterException e) {
					log.warn(e);
				}
				return null;
			};

			Set<RecipeSubsCalculation> set = s.getOptions().stream().map(p).filter( x -> x != null).collect(Collectors.toSet());
			if (set.isEmpty()) {
				log.warn("substitutions " + uid + " for recipeId " + pojo.getId() + "  has no valid options ");
			}
			if (results.containsKey(instanceId)) {
				Set<RecipeSubsCalculation> existing = results.get(instanceId);
				existing.addAll(set);
				results.put(instanceId, existing);
			} else {
				results.put(instanceId, set);
			}
		});
		return results;
	}

    
}
