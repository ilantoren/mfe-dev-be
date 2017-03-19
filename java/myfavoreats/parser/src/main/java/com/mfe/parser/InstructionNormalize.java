/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author richardthorne
 */
public class InstructionNormalize {

    public InstructionNormalize(String text) {
        this.text = text;
    }

    private final String text;
    private String conditionText;
    private List<RecipeToken> tokens = new ArrayList();

    public List<String> parseText() {
       
        try {
          
            String conditionText = getConditionedText() + " ";
            Lexer lex = Lexer.instance(new StringReader(conditionText),"Food");
            

            int ind = -1;
            do {
                ind++;
                tokens.add(lex.nextToken());
            } while (tokens.get(ind).symbol() != TokenClass.EOF);

        } catch (IOException ex) {
            Logger.getLogger(InstructionNormalize.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> mylist = tokens.stream().filter(x -> collect(x)).map(x -> x.lexeme()).collect(toList());
        return mylist;
    }

    public String getConditionedText() {
        String transformed = unescapeText(text);
        return transformed.replaceAll("\\p{Ps}|\\p{Pe}", " ");
    }

    public String getText() {
        return text;
    }

    public List<RecipeToken> getTokens() {
        if ( tokens.isEmpty()) parseText();
        return tokens;
    }

    private boolean collect(Token tk) {
        boolean res = tk.symbol().equals(TokenClass.FOOD) || tk.symbol().equals(TokenClass.DISH);
        // Logger.getLogger(InstructionNormalize.class.getName()).info( tk + ", " + res );
        return res;
    }

    public List<String> parse() {
        return parseText();
    }

    private String unescapeText(String text) {
        return StringEscapeUtils.escapeHtml3(text);
    }

    public List<RecipeToken> getFoodTokens() {
        List<RecipeToken> tkns = getTokens().stream().filter(t -> collect(t)).collect(toList());
        return tkns;
    }

    public String replaceSequences(String input, List<RecipeToken> tkns, List<String> wrds) {
        Map<Integer, Integer> exosplice = tkns.stream().collect(Collectors.toConcurrentMap((recipeToken) -> recipeToken.position(), RecipeToken::endPosition));
        if ( tkns.isEmpty() ) return input;
        StringBuilder sb = new StringBuilder();
        final AtomicInteger indx = new AtomicInteger(0);
        final AtomicInteger left = new AtomicInteger(0);
        final AtomicInteger right = new AtomicInteger(input.length());
        int wordIndx = 0;
        boolean keep = true;
        for (char x : input.toCharArray()) {
           int curIndx = indx.getAndIncrement();
           if (  exosplice.containsKey( curIndx )) {
               right.set(  exosplice.get(curIndx));  
               String wrd = wrds.get(wordIndx);
               wordIndx++;
               sb.append( wrd );
               keep = false;
           }
           if (keep || curIndx > right.get()) {
               keep = true;
               sb.append( x );
           }
           
           
        }
        return sb.toString();
    }

}
