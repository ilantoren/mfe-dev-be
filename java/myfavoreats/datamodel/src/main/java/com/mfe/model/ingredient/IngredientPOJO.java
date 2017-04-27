/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.ingredient;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.mfe.model.recipe.Conversions;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;


/**
 *
 * @author freda
 */

@JacksonXmlRootElement(localName = "data")
public class IngredientPOJO {

    @Override
    public String toString() {
        return uid + "  " + desc;
    }

    public IngredientPOJO() {
        conversions = null;
        fgId = "";
        desc = "";
        id = null;
    }
    
    

    @Id
    @JsonProperty
    @JacksonXmlProperty
    String id;
    
    @JacksonXmlElementWrapper(localName = "conversions")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    Conversions[] conversions;
    
    @JacksonXmlElementWrapper(localName = "factors")
    @JacksonXmlProperty
    @JsonProperty
    @JsonUnwrapped
    Factor[] factors;

    @JacksonXmlProperty
    @JsonProperty
    String energy = "0kJ";

    @JsonProperty
    @JacksonXmlProperty
    String adj_protein = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String alcohol = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String caffeine = "0mg";

    @JsonProperty
    @JacksonXmlProperty
    String calcium = "0mg";

    @JsonProperty
    @JacksonXmlProperty
    String carbohydrate = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String choFact;

    @JsonProperty
    @JacksonXmlProperty
    String cholesterol = "0mg";

    @JsonProperty
    @JacksonXmlProperty
    String desc;

    @JsonProperty
    @JacksonXmlProperty
    String fatFact;

    @JsonProperty
    @JacksonXmlProperty
    String fgId;

    @JsonProperty
    @JacksonXmlProperty
    String fiber = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String foodGroup;

    @JsonProperty
    @JacksonXmlProperty
    String glucose = "0g";

    @JsonProperty
    @JacksonXmlProperty(localName="uid")
    String uid;

    @JsonProperty
    @JacksonXmlProperty
    String canonical;
    
    @JsonProperty
    @JacksonXmlProperty
    String iron = "0mg";

    @JsonProperty
    @JacksonXmlProperty
    String lactose = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String magnesium = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String monoFat = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String nfactor;

    @JsonProperty
    @JacksonXmlProperty
    String polyFat = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String proFact;

    @JsonProperty
    @JacksonXmlProperty
    String protein = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String satFat = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String sodium = "0mg";

    @JsonProperty
    @JacksonXmlProperty
    String sucrose = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String sugars = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String theobromine = "0mg";

    @JsonProperty
    @JacksonXmlProperty
    String transFat = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String vitA = "0IU";

    @JsonProperty
    @JacksonXmlProperty
    String vitC = "0mg";

    @JsonProperty
    @JacksonXmlProperty
    String vitD = "0IU";

    @JsonProperty
    @JacksonXmlProperty
    String totalFat = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String water = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String fructose = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String calories = "0kcal";

    @JsonProperty
    @JacksonXmlProperty
    String starch = "0g";

    @JsonProperty
    @JacksonXmlProperty
    String fatFlag;

    @JsonProperty
    @JacksonXmlProperty
    String carbFlag;

    @JsonProperty
    @JacksonXmlProperty
    String NaFlag;
    
    @JsonProperty
    @JacksonXmlProperty
    String source;
    
    String potassium;
    String phosphorous;
    String contains;   // new Apr 2017 for ingredient list of branded
    String ru;        // either g or ml which is a sign of liquid v solid
    
    
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Factor[] getFactors() {
		return factors;
	}

	public void setFactors(Factor[] factors) {
		this.factors = factors;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Conversions[] getConversions() {
        return conversions;
    }

    public void setConversions(Conversions[] conversions) {
        this.conversions = conversions;
    }

    public String getEnergy() {
        return energy;
    }

    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getAdj_protein() {
        return adj_protein;
    }

    public void setAdj_protein(String adj_protein) {
        this.adj_protein = adj_protein;
    }

    public String getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(String alcohol) {
        this.alcohol = alcohol;
    }

    public String getCaffeine() {
        return caffeine;
    }

    public void setCaffeine(String caffeine) {
        this.caffeine = caffeine;
    }

    public String getCalcium() {
        return calcium;
    }

    public void setCalcium(String calcium) {
        this.calcium = calcium;
    }

    public String getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(String carbohydrate) {
        this.carbohydrate = carbohydrate;
    }

    public String getChoFact() {
        return choFact;
    }

    public void setChoFact(String choFact) {
        this.choFact = choFact;
    }

    public String getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(String cholesterol) {
        this.cholesterol = cholesterol;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFatFact() {
        return fatFact;
    }

    public void setFatFact(String fatFact) {
        this.fatFact = fatFact;
    }

    public String getFgId() {
        return fgId;
    }

    public void setFgId(String fgId) {
        this.fgId = fgId;
    }

    public String getFiber() {
        return fiber;
    }

    public void setFiber(String fiber) {
        this.fiber = fiber;
    }

    public String getFoodGroup() {
        return foodGroup;
    }

    public void setFoodGroup(String foodGroup) {
        this.foodGroup = foodGroup;
    }

    public String getGlucose() {
        return glucose;
    }

    public void setGlucose(String glucose) {
        this.glucose = glucose;
    }

    public String getIron() {
        return iron;
    }

    public void setIron(String iron) {
        this.iron = iron;
    }

    public String getLactose() {
        return lactose;
    }

    public void setLactose(String lactose) {
        this.lactose = lactose;
    }

    public String getMagnesium() {
        return magnesium;
    }

    public void setMagnesium(String magnesium) {
        this.magnesium = magnesium;
    }

    public String getMonoFat() {
        return monoFat;
    }

    public void setMonoFat(String monoFat) {
        this.monoFat = monoFat;
    }

    public String getNfactor() {
        return nfactor;
    }

    public void setNfactor(String nfactor) {
        this.nfactor = nfactor;
    }

    public String getPolyFat() {
        return polyFat;
    }

    public void setPolyFat(String polyFat) {
        this.polyFat = polyFat;
    }

    public String getProFact() {
        return proFact;
    }

    public void setProFact(String proFact) {
        this.proFact = proFact;
    }

    public String getProtein() {
        return protein;
    }

    public void setProtein(String protein) {
        this.protein = protein;
    }

    public String getSatFat() {
        return satFat;
    }

    public void setSatFat(String satFat) {
        this.satFat = satFat;
    }

    public String getSodium() {
        return sodium;
    }

    public void setSodium(String sodium) {
        this.sodium = sodium;
    }

    public String getSucrose() {
        return sucrose;
    }

    public void setSucrose(String sucrose) {
        this.sucrose = sucrose;
    }

    public String getSugars() {
        return sugars;
    }

    public void setSugars(String sugars) {
        this.sugars = sugars;
    }

    public String getTheobromine() {
        return theobromine;
    }

    public void setTheobromine(String theobromine) {
        this.theobromine = theobromine;
    }

    public String getTransFat() {
        return transFat;
    }

    public void setTransFat(String transFat) {
        this.transFat = transFat;
    }

    public String getVitA() {
        return vitA;
    }

    public void setVitA(String vitA) {
        this.vitA = vitA;
    }

    public String getVitC() {
        return vitC;
    }

    public void setVitC(String vitC) {
        this.vitC = vitC;
    }

    public String getVitD() {
        return vitD;
    }

    public void setVitD(String vitD) {
        this.vitD = vitD;
    }

    public String getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(String totalFat) {
        this.totalFat = totalFat;
    }

    public String getCalories() {
        return calories;
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    public String getStarch() {
        return starch;
    }

    public void setStarch(String starch) {
        this.starch = starch;
    }

    public String getWater() {
        return water;
    }

    public void setWater(String water) {
        this.water = water;
    }

    public String getFructose() {
        return fructose;
    }

    public void setFructose(String fructose) {
        this.fructose = fructose;
    }
    
    

    public String getFatFlag() {
        return fatFlag;
    }

    public void setFatFlag(String fatFlag) {
        this.fatFlag = fatFlag;
    }

    public String getCarbFlag() {
        return carbFlag;
    }

    public void setCarbFlag(String carbFlag) {
        this.carbFlag = carbFlag;
    }

    public String getNaFlag() {
        return NaFlag;
    }

    public void setNaFlag(String NaFlag) {
        this.NaFlag = NaFlag;
    }

	public String getCanonical() {
		return canonical;
	}

	public void setCanonical(String canonical) {
		this.canonical = canonical;
	}

	public String getContains() {
		return contains;
	}

	public void setContains(String contains) {
		this.contains = contains;
	}

	public String getRu() {
		return ru;
	}

	public void setRu(String ru) {
		this.ru = ru;
	}

	public String getPhosphorous() {
		return phosphorous;
	}

	public void setPhosphorous(String phosphorous) {
		this.phosphorous = phosphorous;
	}

	public String getPotassium() {
		return potassium;
	}

	public void setPotassium(String potassium) {
		this.potassium = potassium;
	}
    
    

}
