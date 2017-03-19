/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Taking a TopNList from R exported as a list and a map of column number to
 * food, show the suggested additional ingredients for each recipe.
 *
 * @author richardthorne
 */
public class ShowSuggestions {
    
    public static void main( String[] args ) {
        try {
            new ShowSuggestions(new FileReader("suggest.csv"), ';');
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ShowSuggestions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    Pattern pat = Pattern.compile("(fd\\d+)");
    final Reader fr;
    final char sep;

    public ShowSuggestions(Reader fr, char sep) {
         this.fr = fr;
         this.sep = sep;

        try {
            // read ingredient map - map.csv
            CsvReader map = new CsvReader(new FileReader("map.csv"), ',');
            map.readHeaders();
            HashMap<String, String> ingredients = new HashMap();
           
            while (map.readRecord()) {
                 String lbl = "fd" + map.get(0);
                ingredients.put(lbl, map.get(1));
            }
            map.close();

            // read full_map_data 
            CsvReader recipesRead = new CsvReader(new FileReader("full_map_data.csv"), '^');
            HashMap<String, recipe> recipes = new HashMap();
            while (recipesRead.readRecord()) {
                String recipeid = recipesRead.get(0);
                String title = recipesRead.get(1);
                recipes.put(recipeid, new recipe(recipeid, title));
            }
            recipesRead.close();

            // suggestions are in suggest.csv
            List<String[]> output = new ArrayList();
            CsvReader suggestion = new CsvReader(fr, sep);
            suggestion.readHeaders();
            while (suggestion.readRecord()) {
                String recipeid = suggestion.get(1);
                String recommended = suggestion.get(2);
                ArrayList<String> row = new ArrayList();
                recipe r = recipes.get( recipeid );
                if (r == null) continue;
                row.add( r.recipeid);
                row.add( r.title );
                Matcher mat = pat.matcher(recommended);
                while (mat.find()) {
                    String fld = mat.group(1);
                    String food = ingredients.get( fld );
                    row.add( food );
                }
                String[] arr = row.toArray(new String[row.size()]);
                output.add(arr);
                StringWriter sw = new StringWriter();
                
                
            }
            CsvWriter create = new CsvWriter(new BufferedWriter( new FileWriter("recommendation2.csv")), ';');
                output.forEach(i -> {
                    try {
                        create.writeRecord(i);
                    } catch (IOException ex) {
                        Logger.getLogger(ShowSuggestions.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                );
                create.close();

        } catch (IOException ex) {
            Logger.getLogger(ShowSuggestions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class recipe {

        public String recipeid, title;

        public recipe(String recipeid, String title) {
            this.recipeid = recipeid;
            this.title = title;
        }
    }

}
