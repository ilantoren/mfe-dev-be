/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.mfe.model.ingredient.Measures;

/**
 *
 * @author richardthorne
 */
public class AbstractConversions {

    public static List<Conversions> getDefaults() {
        List<Conversions> defaults = new ArrayList();
        defaults.add(   new Conversions("cup", "240", "1"));
        defaults.add( new Conversions("oz", "28", "1"));
        defaults.add( new Conversions("lb", "454", "1" ));
        defaults.add( new Conversions("tbsp", "15", "1"));
        defaults.add( new Conversions( "tsp", "5", "1"));
        return defaults;
    }

    public static BigDecimal convertToCup(String m) {
        Measures meas = Measures.valueOf(m.toUpperCase());
        switch (meas) {
            case GAL:
                return new BigDecimal(16);
            case PINT:
                return new BigDecimal(2);
            case QUART:
                return new BigDecimal(4);
            case QT:
                return new BigDecimal(4);
            case TBL:
                return new BigDecimal(0.0625);
            case TBSP:
                return new BigDecimal(0.0625);
            case TSP:
                return new BigDecimal( 0.0208333 );
                
        }
        return BigDecimal.ONE;
    }

    public static boolean canCovert(String meas) {
        try {
            Measures.valueOf(meas.toLowerCase());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public AbstractConversions() {
    }
    
    
    
}
