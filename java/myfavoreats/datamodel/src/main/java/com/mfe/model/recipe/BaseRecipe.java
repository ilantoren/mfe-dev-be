/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfe.model.ingredient.IngredientPOJO;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author richardthorne
 */
public class BaseRecipe  implements Cloneable {

    public static RecipePOJO normalizedNutrients(Map<String, IngredientPOJO> map, final RecipePOJO recipe) {
        BigDecimal grams = recipe.getTotalGrams();
       if ( grams.intValue() == 0) return null;
        BigDecimal bmult = BigDecimal.valueOf(100d).divide(grams, 3, BigDecimal.ROUND_UP);
        Double mult = bmult.doubleValue();
        recipe.setServings(1);
        recipe.setGramsPerPortion(BigDecimal.valueOf(100D));
        IngredientPOJO ingred;
        Collection<Line> list = getIngredientLines(recipe);
        int cnt = 0;
        Double gram;
        NutrientProfile accumulator = new NutrientProfile();
        for (Line line : list) {
            Double percentOfRecipe = line.getGram()/grams.doubleValue();
            line.setGram( percentOfRecipe*100); // number of grams of ingredient in 100 grm portion
            ingred = map.get(line.getNdb());
            update(line, ingred, percentOfRecipe);
            if (cnt == 0) {
                accumulator.setAll(ingred, percentOfRecipe*mult);
            } else {
                gram = line.gram == null ? 0 : line.gram;
                ingred = map.get(line.ndb);
                try {
                    accumulator.add(ingred, percentOfRecipe*mult);
                } catch (BadParameterException ex) {
                    Logger.getLogger(BaseRecipe.class.getName()).log(Level.SEVERE, line.getNdb() + " problem in "  + recipe.getId(), ex);
                }
            }
            cnt++;
        }
        accumulator.totalGrams = grams.doubleValue();
        accumulator.scaleAll();
        recipe.setTotalGrams(BigDecimal.valueOf(100D));
        recipe.setNutrients(accumulator);
        return recipe;
    }

    public static void update(final Line ingredient, final IngredientPOJO ingred, Double mult) {
        NutrientProfile nutrients = new NutrientProfile();
        nutrients.setGramsPerPortion(mult*100d);
        nutrients.setAll(ingred, mult);
        ingredient.setCalories(nutrients.getCalories());
        ingredient.setCarbohydrates(nutrients.getCarbohydrate());
        ingredient.setCholesterol(nutrients.getCholesterol());
        ingredient.setProtein(nutrients.getProtein());
        ingredient.setTotalFat(nutrients.getTotalFat());
        ingredient.setSodium(nutrients.getSodium());
        ingredient.setWater( nutrients.getWater());
    }
    
   
     
   public static Collection<Line> getIngredientLines( RecipePOJO pojo ) {
       Collection<Line>  result  = new ArrayList();
       if ( pojo == null || pojo.getSteps() == null ) return result;
       pojo.getSteps().stream().forEach((step) -> {
           result.addAll(  step.getLines() );
        });
       return result;
   }

    public BaseRecipe() {
    }    
    
    public RecipePOJO clone() {
        RecipePOJO copy = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            StringWriter sw = new StringWriter();
            mapper.writeValue( sw, this);
            copy = mapper.readValue(sw.toString(), RecipePOJO.class);
            copy.setId(null);
        } catch (IOException ex) {
            Logger.getLogger(BaseRecipe.class.getName()).log(Level.SEVERE, null, ex);
        }
        return copy;
    }
    
    public  Collection<Step> getSteps() { return null; }
    
    public Integer ingredientsByStep(Step step) {
        return step.lines.size();
    }
    
     public Step getRecipeStep( Integer stepNum ) {
       for( Step stp :  getSteps() ) {
           if ( stp.num.equals( stepNum )) {
               return stp;
           }
       }    
       return null;
    }
}
