/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.recipe.BadParameterException;
import com.mfe.model.recipe.ChangeType;
import com.mfe.model.recipe.Line;
import com.mfe.model.recipe.NutrientChange;
import com.mfe.model.recipe.NutrientProfile;
import com.mfe.model.recipe.RecipeChange;
import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.RecipeSubsCalculation;
import com.mfe.model.recipe.RecipeSubsOption;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author richardthorne
 */

public class RecipeChangeService {
	

	protected IngredientPOJOService ingredientPOJOService;
	
	public Optional<IngredientPOJOService> getIngredientPojoService() {
		if ( ingredientPOJOService == null) 
			return Optional.empty();
		else
			return Optional.of(ingredientPOJOService);
	}
	
	public void setIngredientPOJOService( IngredientPOJOService service) {
		ingredientPOJOService = service;
	}
	
	Logger log = Logger.getLogger(getClass().getName() );
	
	
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
       return value.replaceAll("\\p{Alpha}$", "");
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
   
    public RecipeChangeService() {}
    
    public RecipeChangeService( IngredientPOJOService ingredientService ) {
    	this.ingredientPOJOService = ingredientService;
    }
    
    public  static RecipeChange  calculateChange( RecipePOJO pojo1,   RecipePOJO pojo2 ) throws BadParameterException, IllegalArgumentException, IllegalAccessException, IOException {
            RecipeChange changes = new RecipeChange();
           // changes.setProbability(Double.parseDouble(pojo2.getSubsitutionDescription() ));  // Desc field used to store the probability
            changes.setSubstitutionRule(pojo2.getSubstitutionRule());
            NutrientProfile  original  = pojo1.getNutrients();
            NutrientProfile modified = pojo2.getNutrients();
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
     
     public  RecipeSubsCalculation createRecipeWithSubstitute( RecipePOJO pojo, String recipeSubstitutionId, RecipeSubsOption option ) {
    	 if ( recipeSubstitutionId == null ) {
    		 return null;
    	 }
    	 RecipePOJO substituteRecipe = pojo.clone();
    	 RecipeSubsCalculation recipeSubCalculation = new RecipeSubsCalculation( pojo, recipeSubstitutionId, option );
    	 // complete the substituted element
    	 
    

    	
    	 changeRecipeWithSubstitution(substituteRecipe, recipeSubCalculation);
    	 
    	 Optional<IngredientPOJOService> ingredientMap = getIngredientPojoService();
    	 ingredientMap.ifPresent(map  -> {
    		 try {
				calculateRecipeNutrition( pojo, map);
	    		 calculateRecipeNutrition( substituteRecipe, map );
	    		 recipeSubCalculation.setNutrients(substituteRecipe.getNutrients());
	    		 RecipeChange recipeChange = calculateChange( pojo, substituteRecipe);
	    		 recipeSubCalculation.setRecipeChange(recipeChange);
			} catch (Exception e) {
				Logger.getLogger( this.getClass().getName()).log( Level.SEVERE, "Failed in calculating recipeChange", e);
			}
    	 });
    	 
    	 return recipeSubCalculation;
     }

	/**
	 * @param substituteRecipe
	 * @param recipeSubCalculation
	 * @param ingredientLineId
	 */
	public boolean changeRecipeWithSubstitution(RecipePOJO substituteRecipe, RecipeSubsCalculation recipeSubCalculation) {
		String ingredientLineId = recipeSubCalculation.getInstanceId();
		if ( ingredientLineId == null ) {
			log.warning( recipeSubCalculation.getDescription() + " has no valid instanceId ");
		}
		Optional<Line> ingredientLine = RecipePOJO.getIngredientLines(substituteRecipe)
    			 	.stream()
    			 	.filter( a -> a.getUid() != null )
    			 	.filter( a -> a.getUid().equals( ingredientLineId)).findFirst();
                AtomicBoolean booleanObj = new AtomicBoolean(true);
    	 ingredientLine.ifPresent(x -> {
        	 String sourceFood = recipeSubCalculation.getSource();
        	 String targetFood  = recipeSubCalculation.getTarget();
        	 String targetId = recipeSubCalculation.getTargetId();
        	 IngredientPOJO targetIngredient = ingredientPOJOService.getByEntityMapping(targetId);
        	 if ( targetIngredient == null ) {
        		 Logger.getLogger(getClass().getName()).warning(targetId + " does not point to a valid ingredientPOJO");
                         booleanObj.set( false );
        	 }
        	 else {
        		 String replaceNdb = targetIngredient.getUid();
        		 if ( x.getNdb() == null ) {
        			log.warning( x.getUid() + " " + x.getOriginal() + "  has null ndb value");
        		 }
        		 Logger.getLogger(getClass().getName()).info( "replacing ingredient " + x.getNdb() +  "  with " + replaceNdb );
        		 x.setNdb( targetIngredient.getUid());
        	 }
        	 x.setFood(targetFood);
        	 x.setCannonical(targetFood);
        	 String replaceLine = x.getOriginal().replace(sourceFood, targetFood );
        	 x.setOriginal(replaceLine);
    	 });
    	 
    	 if (! ingredientLine.isPresent() ) {
    		 Logger.getLogger(getClass().getName()).warning( ingredientLineId + " is missing ");
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
				Logger.getGlobal().log( Level.SEVERE, "error in call", e);
			}
    	 });
    	 if ( hasError.get() ) {
    		 throw new BadParameterException( "ingredientPojoService is not set");
    	 }
     }
     public void calculateRecipeNutrition(RecipePOJO pojo, IngredientPOJOService ingredientPojo) throws BadParameterException {
         NutrientProfile nutrients = new NutrientProfile();
         log.info( "calculating");

         List<Line> ingredients = (List<Line>) RecipePOJO.getIngredientLines(pojo);
         for (Line ingredient : ingredients) {
             String ndb = ingredient.getNdb();
             if (ndb == null || ingredient.getGram() == null) {
                 continue;
             }
             Double mult = BigDecimal.valueOf(ingredient.getGram()).divide(BigDecimal.valueOf(100d), 6, BigDecimal.ROUND_UP).doubleValue();
             IngredientPOJO ingred = ingredientPojo.getByEntityMapping(ingredient.getEntityId() );
             if (ingred == null) {
            	 log.warning(ingredient.getCannonical() + " of " + pojo.getId() + " : " + ingredient.getUid() + " no mapping");
                 continue;
             }
             nutrients.add(ingred, mult);
             NutrientProfile perIngredient = new NutrientProfile();
             perIngredient.setGramsPerPortion(100D);
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
         if (sumGram > 0) {
             BigDecimal scale = BigDecimal.valueOf(100d).divide(BigDecimal.valueOf(sumGram), 4, BigDecimal.ROUND_HALF_UP);
             nutrients.setGramsPerPortion(100d);
             nutrients.scaleAll();
             nutrients.setAll(nutrients, scale.doubleValue());
             pojo.setNutrients(nutrients);
             pojo.setGramsPerPortion(BigDecimal.valueOf(100));
         }
     }
     
     public Map<String,Set<RecipeSubsCalculation>> getAllRecipeSubstitutes( RecipePOJO pojo ) {
    	 Map<String,Set<RecipeSubsCalculation>> results = new HashMap<>();
    	 pojo.getSubs().forEach(s -> { 
    		 String instanceId = s.getInstanceId();
    		Set<RecipeSubsCalculation> set =  s.getOptions().stream()
    					.map( opt ->createRecipeWithSubstitute(pojo,s.getUid() , opt ) )
    					.collect( Collectors.toSet());
    		    if ( set.isEmpty() ) {
    		    	log.severe( "substitutions " + s.getUid() + " for line" + instanceId + "  has no valid options ");
    		    }
    		    if ( results.containsKey(instanceId)) {
    		    	Set<RecipeSubsCalculation> existing = results.get(instanceId );
    		    	existing.addAll(set);
    		    	results.put( instanceId, existing);
    		    }
    		    else {
    		    	results.put( instanceId, set);
    		    }
    	 });
    	 return results;
     }

    
}
