package com.mfe.frontend;

import com.mfe.model.recipe.RecipeSub;
import com.mfe.model.recipe.RecipeSubsOption;

import java.util.Comparator;

/**
 * Created by root on 22/03/17.
 */
public class IngredientSorters {

    public static class GramCompare implements Comparator<String>{

        @Override
        public int compare(String o1, String o2) {
            try {
                if (o1 == null || o2 == null) {
                    return -1;
                }

                // Can't compare, one of the values is not in Grams
                if (!o1.endsWith("g") || !o2.endsWith("g")) {
                    return -1;
                }

                String v1 = o1.substring(0, o1.length() - 1);
                String v2 = o2.substring(0, o2.length() - 1);

                return Float.valueOf(v1).compareTo(Float.valueOf(v2));
            }
            catch (Exception e){
                return -1;
            }
        }
    }

    public static class LowCarbsCompare implements Comparator<RecipeSubsOption> {
        @Override
        public int compare(RecipeSubsOption o1, RecipeSubsOption o2) {
            GramCompare gramCompare = new GramCompare();
            if (o1.getIngredient() == null || o2.getIngredient() == null){
                return -1;
            }
            return gramCompare.compare(o1.getIngredient().getCarbohydrate(),o2.getIngredient().getCarbohydrate());
        }
    }
}
