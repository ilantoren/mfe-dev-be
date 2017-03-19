/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.recipe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import org.apache.commons.math3.fraction.BigFraction;

/**
 *
 * @author freda
 */
@JacksonXmlRootElement(localName = "conversions")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Conversions extends AbstractConversions {

    @JacksonXmlElementWrapper(localName = "measure")
    @JacksonXmlProperty
    @JsonProperty
    String measure;

    @JacksonXmlProperty
    @JacksonXmlElementWrapper(localName = "gramPerUnit")
    @JsonProperty
    String gramPerUnit;
    
    @JacksonXmlProperty
    @JacksonXmlElementWrapper(localName = "amount")
    @JsonProperty
    String amount;

    public Conversions(String measure, String gramsConversion, String amount ) {
        this.measure = measure;
        this.amount = amount;
        this.gramPerUnit = gramsConversion;
    }

    public Conversions() {
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getGramPerUnit() {
        return gramPerUnit;
    }

    public void setGramPerUnit(String gramPerUnit) {
        this.gramPerUnit = gramPerUnit;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
    
    

    public static BigDecimal convertComplexQuantity(String quantityString) {
        String[] parts = quantityString.trim().split("\\s");
        BigDecimal sum = BigDecimal.ZERO;
        for (String part : parts) {
            if (part.matches("([0-9]|\\.)+")) {
                Double dbl = Double.parseDouble(part);
                sum = sum.add(BigDecimal.valueOf(dbl));
            } else if (part.matches("([0-9]|\\/)+")) {
                String[] frac = part.split("/");
                BigDecimal num = new BigDecimal(frac[0]);
                BigDecimal denom = new BigDecimal(frac[1]);
                if (denom.compareTo(BigDecimal.ZERO) != 0) {
                    BigFraction bf = new BigFraction(num.longValue(), denom.longValue());
                    sum = sum.add(bf.bigDecimalValue(3, BigDecimal.ROUND_DOWN));
                }
            }
        }

        return sum;

    }

}
