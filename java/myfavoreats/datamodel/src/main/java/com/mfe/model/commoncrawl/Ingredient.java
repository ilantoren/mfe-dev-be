package com.mfe.model.commoncrawl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mfe.model.utils.IngredientSubstitution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author richardthorne
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "ingredient")
public class Ingredient implements Serializable {

    public Ingredient() {
    }

    private String measure, ingredientLn, food, original, ndb, hint, cannonical;
    private boolean isDish = false;
    private String amount;
    
    @JacksonXmlElementWrapper(localName = "substitution")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    private List<IngredientSubstitution> subs = new ArrayList();

    @JsonProperty(value = "unit")
    String portionText;

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredientLn() {
        return ingredientLn;
    }

    public void setIngredientLn(String ingredientLn) {
        this.ingredientLn = ingredientLn;
    }

    public String getPortionText() {
        return portionText;
    }

    public void setPortionText(String portionText) {
        this.portionText = portionText;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getNdb() {
        return ndb;
    }

    public void setNdb(String ndb) {
        this.ndb = ndb;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public boolean isDish() {
        return isDish;
    }

    public void setDish(boolean isDish) {
        this.isDish = isDish;
    }

    public String getCannonical() {
        return cannonical;
    }

    public void setCannonical(String cannonical) {
        this.cannonical = cannonical;
    }

    public Collection<IngredientSubstitution> getSubs() {
        return subs;
    }

    public void setSubs(List<IngredientSubstitution> subs) {
        this.subs = subs;
    }
    
    public void addSubstitution( String key, String targetId,String optionId,  String value) {
        if ( value == null ) value = "0";
        IngredientSubstitution x = new IngredientSubstitution( key,targetId,optionId, value);
        this.subs.add(x);
    }
    
    public void addSubstitution( String key, String targetId,  String value) {
    	 String s = String.format( "%s,%s", key,targetId);
    	 Integer hc = s.hashCode();
    	addSubstitution(  key,  targetId, hc.toString(),   value);
    }
}
