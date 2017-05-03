/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfe.model.recipe.RecipeChange;
import com.mfe.model.recipe.RecipePOJO;
import com.mfe.model.recipe.RecipeSubsCalculation;
import com.mfe.model.recipe.RecipeSubsOption;

/**
 *
 * @author richardthorne
 */
public class RecipeChangeServiceTest {

    private final ObjectMapper mapper;
    private RecipePOJO parent;
    private RecipePOJO child;
    
    public RecipeChangeServiceTest() {
        mapper = new ObjectMapper();
    }
    
    @Before
    public void setUp() throws IOException {
        parent = mapper.readValue(new FileReader( "testFiles/parentPojo.json"),  RecipePOJO.class );
        child  = mapper.readValue( new FileReader( "testFiles/childPojo.json"),  RecipePOJO.class );
    }

    /**
     * Test of calculateChange method, of class RecipeChangeService.
     */
    @Test
    @Ignore   // remove the substitionRule content
    public void testCalculateChange() throws Exception {
        System.out.println("calculateChange");
        
        RecipeChange expResult = null;
        RecipeChange result = RecipeChangeService.calculateChange(parent, child);
        String resultStr = mapper.writeValueAsString(result);
        /*
        try ( FileWriter fw = new FileWriter("result.json")) {
            fw.write( resultStr);
        }*/
        
        
        byte[] x = Files.readAllBytes( new File("result.json").toPath() );
       // assertEquals( new String( x ),  resultStr );
    }

    /**
     * Test of prepareForDisplay method, of class RecipeChangeService.
     */
    @Test
    public void testPrepareForDisplay() throws IOException {
        System.out.println("prepareForDisplay");
        RecipeChange rc = mapper.readValue(new FileReader("testFiles/recipeChange.json"), RecipeChange.class );
        RecipeChange rcPrepared = mapper.readValue(new FileReader("testFiles/recipeChangePrepared.json"), RecipeChange.class );
        RecipeChangeService.prepareForDisplay(rc);
        assertTrue( rc.getSodium().equals( rcPrepared.getSodium()));
        assertTrue( rc.getProtein().equals( rcPrepared.getProtein() ));
    }
    
    
    /**
     * Test of prepareForDisplay method, of class RecipeChangeService.
     */
    @Test
    public void testPrepareForDisplaySodium() throws IOException {
        System.out.println("prepareForDisplay");
        RecipeChange rc = mapper.readValue(new FileReader("testFiles/recipeChangeInput.json"), RecipeChange.class );
        RecipeChange rcPrepared = mapper.readValue(new FileReader("testFiles/recipeChangeOutput.json"), RecipeChange.class );
        RecipeChangeService.prepareForDisplay(rc);
        assertTrue( rc.getSodium().equals( rcPrepared.getSodium()));
        assertTrue( rc.getProtein().equals( rcPrepared.getProtein() ));
    }

    

    
    

    /**
     * Test of createRecipeWithSubstitute method, of class RecipeChangeService.
     */
    @Test
    @Ignore //TODO
    public void testCreateRecipeWithSubstitute() {
        System.out.println("createRecipeWithSubstitute");
        RecipePOJO pojo = null;
        String recipeSubstitutionId = "";
        RecipeSubsOption option = null;
       // RecipeChangeService instance = new RecipeChangeServicen(null);
        RecipeSubsCalculation expResult = null;
      //  RecipeSubsCalculation result = instance.createRecipeWithSubstitute(pojo, recipeSubstitutionId, option);
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of changeRecipeWithSubstitution method, of class RecipeChangeService.
     */
    @Test
    @Ignore //TODO
    public void testChangeRecipeWithSubstitution() {
        System.out.println("changeRecipeWithSubstitution");
        RecipePOJO substituteRecipe = null;
        RecipeSubsCalculation recipeSubCalculation = null;
      //  RecipeChangeService instance = new RecipeChangeService();
        boolean expResult = false;
       // boolean result = instance.changeRecipeWithSubstitution(substituteRecipe, recipeSubCalculation);
      //  assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateRecipeNutrition method, of class RecipeChangeService.
     */
    @Test
    @Ignore // TODO
    public void testCalculateRecipeNutrition_RecipePOJO() throws Exception {
        System.out.println("calculateRecipeNutrition");
        RecipePOJO pojo = null;
       // RecipeChangeService instance = new RecipeChangeService();
        //instance.calculateRecipeNutrition(pojo);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of calculateRecipeNutrition method, of class RecipeChangeService.
     */
    @Test
    @Ignore // TODO
    public void testCalculateRecipeNutrition_RecipePOJO_IngredientPOJOService() throws Exception {
        System.out.println("calculateRecipeNutrition");
        RecipePOJO pojo = null;
        IngredientPOJOService ingredientPojo = null;
     //   RecipeChangeService instance = new RecipeChangeService();
       // instance.calculateRecipeNutrition(pojo, ingredientPojo);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllRecipeSubstitutes method, of class RecipeChangeService.
     */
    @Test
    @Ignore //TODO
    public void testGetAllRecipeSubstitutes() {
        System.out.println("getAllRecipeSubstitutes");
        RecipePOJO pojo = null;
 //       RecipeChangeService instance = new RecipeChangeService();
        Map<String, Set<RecipeSubsCalculation>> expResult = null;
    //    Map<String, Set<RecipeSubsCalculation>> result = instance.getAllRecipeSubstitutes(pojo);
     //   assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
