/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.mfe.model.ingredient.IngredientPOJO;
import com.mfe.model.ingredient.IngredientService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.data.annotation.Transient;

/**
 *
 * @author freda
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NutrientProfile extends IngredientPOJO {
   
    
    
    @JsonIgnore
      public static final  String[] flds  =  new String[] { "calcium", "carbohydrate", "water" ,"lactose", "fructose", "cholesterol",
          "protein", "satFat", "sodium", "sucrose",  "totalFat","sugars", "fiber",  "calories" , "water", "ref"};
    @JsonIgnore
      private Pattern removeTrailingLetters = Pattern.compile( "([\\d|\\.]+)(\\D+)$");
    

      private Double ref;
      
      public NutrientProfile() {}
    
    
      public  Set<String>  getSet(){
        return Arrays.stream( flds).collect( Collectors.toSet() );
    }

    public Double getTotalGrams() {
        return totalGrams;
    }

    public void setTotalGrams(Double totalGrams) {
    	this.ref = totalGrams;
        this.totalGrams = totalGrams;
    }

    public Double getGramsPerPortion() {
        return gramsPerPortion;
    }

    public void setGramsPerPortion(Double gramsPerPortion) {
        this.gramsPerPortion = gramsPerPortion;
    }
    
    
    @JsonProperty
    @JacksonXmlProperty
    Double totalGrams;


    public void scaleAll() {
        IngredientService service = new IngredientService(this);
        service.scaleAll(1D);
        if ( gramsPerPortion  < 1000) {
            BigDecimal bd = new BigDecimal(gramsPerPortion);
            gramsPerPortion = bd.setScale(0, RoundingMode.HALF_UP).doubleValue();
        }
    }


    public void add(IngredientPOJO obj, Double mult)  throws  BadParameterException  {
         IngredientService service = new IngredientService( this );
        Logger.getAnonymousLogger().log(Level.FINEST, "Multiplier is {0}", mult.toString());
        service.add(obj, mult);
        this.gramsPerPortion = Double.sum(this.gramsPerPortion, 100 * mult);
    }
    
    
    public void scaleAll( Double scale ) {
    	IngredientService service = new IngredientService(this);
    	service.scaleAll(scale);
    }

    public void setAll( IngredientPOJO obj, Double mult) {
        IngredientService service = new IngredientService( this );
        if (obj != null &&  obj.getUid() !=null &&  !obj.getUid().equals("0")) {
            service.setAll(obj, mult);
        }
    }

    @JsonProperty
    @JacksonXmlProperty
    Double gramsPerPortion = 100D;

    public void updateField(Field i) {
        try {
            Field f = this.getClass().getDeclaredField(i.getName());
            f.set(this, i);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            Logger.getLogger(NutrientProfile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String getHeader() {
        StringBuilder sb = new StringBuilder();
        for(String s : getSet() ) {
            sb.append( WordUtils.capitalize(s) ).append(",");
        }
        return sb.toString();
    }
    
    public Double getRef() {
    	return ref;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
       
       for( String s  : getSet() ) {
            try {           
                String str = getField(s);
                sb.append( str ).append(",");
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(NutrientProfile.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       
        return sb.toString();
    }

    public String getField(String s) throws NoSuchMethodException, InvocationTargetException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String m = String.format( "get%s", WordUtils.capitalize(s) );
        Method my = this.getClass().getMethod(m);
        String str = (String) my.invoke(this);
        return str;
    }
    
    public Double getFieldValue( String s ) {
          try {
              String strVal = getField( s );
              Matcher restMatch = removeTrailingLetters.matcher(strVal);
              if ( restMatch.find() ) {
              String resStr = restMatch.group(1);	  
              Double val;
               if ( resStr.isEmpty()) {
                   val = Double.NaN;
               }else {
                   val = Double.parseDouble(resStr);
               }
               
              return val;
              }
          } catch (NoSuchMethodException | InvocationTargetException | SecurityException | IllegalArgumentException | IllegalAccessException  ex) {
              Logger.getLogger(NutrientProfile.class.getName()).log(Level.SEVERE, null, ex);
          }
          return BigDecimal.ZERO.doubleValue();
    }

    public void subtract(IngredientPOJO changed) throws BadParameterException {
        IngredientService service = new IngredientService( this );
        service.subtract(changed);
    }

    public void divide( IngredientPOJO changed) {
        IngredientService service = new IngredientService (  this );
        service.divide(changed);
    }

    @Override
    public NutrientProfile clone() {
        IngredientService service = new IngredientService(this);
       
            try {
				return service.clone(this);
			} catch (IOException e) {
				Logger.getLogger( getClass().getName()).log( Level.SEVERE, "cloning", e);
			}
       
        return null;
    }
}
