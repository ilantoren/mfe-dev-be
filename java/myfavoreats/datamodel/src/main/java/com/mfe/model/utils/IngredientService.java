/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.recipe.BadParameterException;
import com.mfe.model.recipe.NutrientProfile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author richardthorne
 */
public class IngredientService {
    
    private IngredientPOJO pojo;
    
    
    
  
    final Pattern pat = Pattern.compile("([\\d|\\.]+)(\\p{Alpha}+)?");

    public IngredientService(IngredientPOJO  p) {
        pojo = p;
        mc = new MathContext(1, RoundingMode.CEILING);
    }

    @JsonIgnore
    private MathContext mc;

    public Set<String> getSet() {
        String[] useFields = {"energy", "adj_protein", "alcohol", "caffeine", "calcium", "carbohydrate", "cholesterol",
            "fiber", "glucose", "iron", "lactose", "magnesium", "monoFat", "polyFat", "protein", "satFat",
            "sodium", "sucrose", "sugars", "theobromine", "transFat", "vitA", "vitC", "vitD", "totalFat", "calories", "starch", "water"};
        Set<String> set = new HashSet();
        set.addAll(Arrays.asList(useFields));
        return set;
    }

    public MathContext getMc() {
        return mc;
    }

    public void setMc(MathContext mc) {
        this.mc = mc;
    }

    public void scaleAll() {
        Iterator<String> it = getSet().iterator();

        while (it.hasNext()) {
            Field f = null;
            String s = "";
            String v = "";

            s = it.next();

            if (s != null) {
                try {
                    f = IngredientPOJO.class.getDeclaredField(s);
                    v = f.get(pojo).toString();
                    setNutrientField(v, s, 1D, f, true);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    public void setAll(IngredientPOJO obj, Double mult) {

        Iterator<String> it = getSet().iterator();

        while (it.hasNext()) {
            Field f = null;
            String s = "";
            String v = "";
            try {
                s = it.next();

                if (s != null) {
                    f = IngredientPOJO.class.getDeclaredField(s);
                }
                v = f.get(obj).toString();
            } catch (NoSuchFieldException e1) {
                Logger.getAnonymousLogger().info(s + " is not a declared field " + obj.getUid
     ());
                continue;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, null, ex);
            }
            setNutrientField(v, s, mult, f);

        }
    }

    private void setNutrientField(String v, String s, Double mult, Field f) {
        setNutrientField(v, s, mult, f, false);
    }

    protected void setNutrientField(String v, String s, Double mult, Field f, boolean scale) {
        StringBuilder sb = new StringBuilder();
        final Matcher m = pat.matcher(v);
        if (m.find()) {
            try {
                Logger.getLogger(IngredientPOJO.class.getName()).log(Level.FINE, s + " " + v + "  " + m.group(1));
                Double d1 = Double.parseDouble(m.group(1)) * mult;
                if (scale) {
                    BigDecimal bd = new BigDecimal(d1);

                    d1 = bd.setScale(1, RoundingMode.UP).doubleValue();
                }
                String unit = m.group(2) == null ? "" : m.group(2);
                sb.append(d1).append(" ").append(unit.trim());
                f.set(pojo, sb.toString());
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void subtract(IngredientPOJO obj) throws BadParameterException {
        add(obj, -1d);
    }

    public void add(final IngredientPOJO obj, Double mult) throws BadParameterException {
        if (obj == null) {
            Logger.getAnonymousLogger().severe("Passed in a null argument to add; ignored");
            throw new BadParameterException( "Passed in a null argument to add; ignored");
        }
        Iterator<String> it = getSet().iterator();
        while (it.hasNext()) {
            try {
                String s = it.next();
                Field f = IngredientPOJO.class.getDeclaredField(s);
                String val1 = f.get(pojo).toString();
                String val2;
                try {
                    val2 = f.get(obj).toString();
                } catch (Exception e1) {
                    Logger.getAnonymousLogger().info(s + " is absent in " + obj.getUid());
                    continue;
                }
                Double A = 0D;
                Double B = 0D;
                String unit;
                Matcher m1 = pat.matcher(val1);
                Matcher m2 = pat.matcher(val2);
                if (m1.find() && m2.find()) {
                    A = Double.parseDouble(m1.group(1));
                    B = Double.parseDouble(m2.group(1)) * mult;
                    unit = (m2.group(2) == null) ? "" : m2.group(2);
                    f.set(pojo, Double.sum(A, B) + unit);
                } else {

                    if (m1.find()) {
                        A = Double.parseDouble(m1.group(1));
                        unit = (m1.group(2) == null) ? "" : m2.group(2);
                    }
                    if (m2.find()) {
                        A = Double.parseDouble(m2.group(1));
                        unit = (m2.group(2) == null) ? "" : m2.group(2);
                    }
                    unit = (m2.group(2) == null) ? "" : m2.group(2);
                    f.set(pojo, Double.sum(A, B) + unit);
                    Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, "NO MATCH " + s);
                }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void divide(final IngredientPOJO obj) {
        if (obj == null) {
            Logger.getAnonymousLogger().severe("Passed in a null argument to add; ignored");
            return;
        }
        Iterator<String> it = getSet().iterator();
        while (it.hasNext()) {
            try {
                String s = it.next();
                Field f = IngredientPOJO.class.getDeclaredField(s);
                String val1 = f.get(pojo).toString();
                String val2;
                try {
                    val2 = f.get(obj).toString();
                } catch (Exception e1) {
                    Logger.getAnonymousLogger().info(s + " is absent in " + obj.getUid() );
                    continue;
                }
                Double A = 0D;
                Double B = 0D;
                String unit;
                Matcher m1 = pat.matcher(val1);
                Matcher m2 = pat.matcher(val2);
                if (m1.find() && m2.find()) {
                    A = Double.parseDouble(m1.group(1));
                    B = Double.parseDouble(m2.group(1));
                    unit = (m2.group(2) == null) ? "" : m2.group(2);
                    if (A != 0) {
                        f.set(pojo, BigDecimal.valueOf(B).divide(BigDecimal.valueOf(A), 5, BigDecimal.ROUND_UP).toPlainString());
                    } else {
                        f.set(pojo, "");
                    }
                } else {

                    if (m1.find()) {
                        A = Double.parseDouble(m1.group(1));
                        unit = (m1.group(2) == null) ? "" : m2.group(2);
                    }
                    if (m2.find()) {
                        A = Double.parseDouble(m2.group(1));
                        unit = (m2.group(2) == null) ? "" : m2.group(2);
                    }
                    unit = (m2.group(2) == null) ? "" : m2.group(2);
                    if (A != 0) {
                        f.set(pojo, BigDecimal.valueOf(B).divide(BigDecimal.valueOf(A), 5, BigDecimal.ROUND_UP).toPlainString());
                    } else {
                        f.set(pojo, "");
                    }
                    Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, "NO MATCH " + s);
                }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(IngredientPOJO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public static NutrientProfile clone( NutrientProfile nutrientProfile) throws IOException {
    	ObjectMapper mapper = new ObjectMapper();
    	String orig = mapper.writeValueAsString(nutrientProfile);
    	return mapper.readValue(orig, NutrientProfile.class );
    }
    
    public static  NutrientProfile cloneOld( NutrientProfile orig ) throws IllegalArgumentException, IllegalAccessException {
           NutrientProfile daClone = new NutrientProfile();
           
            HashSet<String> set = new HashSet();
            set.addAll(Arrays.asList(NutrientProfile.flds));
             Class<?> c = IngredientPOJO.class;
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                if ( set.contains(f.getName() )) {
                     Object s = f.get(orig);
                     if ( s == null ) {
                         // skip
                     }
                     else {
                        f.set( daClone, s);
                     }
                }
            }
            return daClone;
    }
    
    
    
}
