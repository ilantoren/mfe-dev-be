/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.recipe.BadParameterException;
import com.mfe.model.recipe.NutrientProfile;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author freda
 */
public class ReflectionTest {
    public static String TEST_FILE = "oneIngredient.json";
    

    public static void main(String[] args) {
        final   Pattern pat = Pattern.compile("([\\d\\.]+)\\s*(\\w+)*");
        FileReader fr = null;
        FileWriter fw = null;
        try {
            String[] useFields = {"energy", "adj_protein", "alcohol", "caffeine", "calcium", "carbohydrate", "cholesterol",
                "fiber", "glucose", "iron", "lactose", "magnesium", "monoFat", "polyFat", "proFact", "protein", "satFat",
                "sodium", "sucrose", "sugars", "theobromine", "transFat", "vitA", "vitC", "vitD", "totalFat", "calories", "starch"};
            HashSet<String> set = new HashSet();
            set.addAll(Arrays.asList(useFields));
            fr = new FileReader( TEST_FILE );
            fw = new FileWriter("addTest.json");
            ObjectMapper mapper = new ObjectMapper();
            IngredientPOJO pojo = mapper.readValue(fr, IngredientPOJO.class );
            Class<?> c = pojo.getClass();
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                if ( set.contains(f.getName() )) {
                    System.out.println(f.getName());
                    final String s = f.get(pojo).toString();
                    final Matcher m = pat.matcher(s);
                    System.out.println( s);
                    if ( m.find()) {
                        System.out.println( m.group(1) + "  " + m.group(2));
                    }
                }
            }
            
            NutrientProfile test = new NutrientProfile();
            test.setAll(pojo, 0.5);
            try {
                test.add(pojo, 0.5);
            } catch (BadParameterException ex) {
                Logger.getLogger(ReflectionTest.class.getName()).log(Level.SEVERE, "test throws excepton ", ex);
            }
            mapper.writeValue(fw, test);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReflectionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReflectionTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(ReflectionTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(ReflectionTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
