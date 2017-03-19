/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.joining;

/**
 *
 * @author Owner
 */
public final class Lexer  implements JflexLex {

    private final JflexLex lexer;
    private List<RecipeToken> tokens;
    private String solrQueryText;
    private final HashSet<String> queryBuilder = new HashSet();
    private final StringBuilder modifiers = new StringBuilder();
    private final StringBuilder hintBuilder = new StringBuilder();
    private final StringBuilder measure = new StringBuilder();
    private final StringBuilder amount = new StringBuilder();
    private final StringBuilder measurePortion = new StringBuilder();
    private boolean isDish = false;

    public static Lexer instance(Reader in, String type) throws IOException {
        JflexLex lexer = getLex(in, JflexType.valueOf(type));
        return new Lexer(in, lexer);
    }
    private final Reader in;

    public String getModifiers() {
        return modifiers.toString().trim();
    }

    private Lexer(Reader in, JflexLex lexer) throws IOException {
        this.lexer = lexer;
        this.in = in;
        this.parse();
    }
    
   // @Timeable(limit = 1, unit = TimeUnit.SECONDS)
    public void parse() throws IOException {
        tokens = new ArrayList();
        RecipeToken token;
        boolean latch = true;
        int tknCnt = 0;
        solrQueryText = null;
        do {
            if (tknCnt++ > 300) {
                break;
            }
            token = lexer.nextToken();
            tokens.add(token);

            if (isMeasureToken(token.symbol()) || isNumber(token.symbol()) || (token.symbol().equals( TokenClass.ADJ) && isSizeToken( token.lexeme()))  ) {
                measure.append(" ").append(token.lexeme());
            } else if (token.symbol().equals(com.mfe.parser.TokenClass.OTHER)
                    || token.symbol().equals(com.mfe.parser.TokenClass.SPACE)) {
                modifiers.append(token.lexeme());
            } else if (token.symbol().equals(TokenClass.FOOD)) { // || token.symbol().equals(TokenClass.DISH)
                queryBuilder.add( token.lexeme());
            } else if (token.symbol().equals(TokenClass.DISH)) {
                queryBuilder.add(token.lexeme());
                isDish = true;
            } else if (token.symbol().equals(TokenClass.HINT)) {
                hintBuilder.append(token.lexeme());
            } else if (token.symbol().equals(TokenClass.EOF)) {
                // EOF DO NOTHING
            }else if ( token.symbol().equals(TokenClass.ADJ) ) {
                    modifiers.append(token.lexeme());
            }

            //  JUst capture the measure e.g. cup medieum liter
            if (isMeasureToken(token.symbol())) {
                measurePortion.append(token.lexeme()).append(" ");
                latch = false;
            }

            // only adj that are related to size
            if ( token.symbol().equals( TokenClass.ADJ) && isSizeToken( token.lexeme() )) {
                measurePortion.append( token.lexeme() ).append(" ");
            }
            // just the amount
            if (isNumber(token.symbol()) && latch) {
                amount.append(token.lexeme()).append(" ");
            }
        } while (!token.symbol().equals(com.mfe.parser.TokenClass.EOF));
        solrQueryText = queryBuilder.stream().sorted().collect(Collectors.joining(" "));
    }

    public String getSolrQueryText() {
        if (solrQueryText == null) {
            return "";
        } else {
            return solrQueryText.trim();
        }
    }
    
    public String longestLengthFood() {
        List<String> list = getFoods();
        int len = 0;
        String res = "";
        if ( list.isEmpty()) {
            return "";
        }
        for( String s : list ) {
            if( s.length() > len) {
                res = s;
                len = s.length();
            }
        }
        return res;
    }

    public List<String> getFoods() {
        return queryBuilder.stream().collect(toList());
    }

    public String getFoodString() {
        return queryBuilder.stream().collect(joining(","));
    }

    public String getMeasureText() {
        return measure.toString().trim();
    }

    public boolean isDish() {
        return isDish;
    }

    public void setIsDish(boolean isDish) {
        this.isDish = isDish;
    }

    public String getHint() {
        return hintBuilder.toString();
    }

    public boolean isMeasureToken(TokenClass a) {
        return (a.equals(TokenClass.CUP)
                || a.equals(TokenClass.GALLON)
                || a.equals(TokenClass.GRAM)
                || a.equals(TokenClass.INCH)
                || a.equals(TokenClass.KILO)
                || a.equals(TokenClass.LITER)
                || a.equals(TokenClass.LB)
                || a.equals(TokenClass.LITER)
                || a.equals(TokenClass.MG)
                || a.equals(TokenClass.ML)
                || a.equals(TokenClass.OZ)
                || a.equals(TokenClass.PINT)
                || a.equals(TokenClass.TBL)
                || a.equals(TokenClass.SIZE)
                || a.equals(TokenClass.TSP));
    }

    public boolean isNumber(TokenClass tk) {
        if (tk.equals(TokenClass.NUMBER)
                || tk.equals(TokenClass.FRACTION)
                || tk.equals(TokenClass.INTEGER)
                || tk.equals(TokenClass.DECFRAC)) {
            return true;
        }

        return false;
    }

    public String getMeasurePortionField() {
        return measurePortion.toString().trim();
    }

    public String getIngredientLine() {
        String line = tokens.stream().filter(tk -> !(tk.lexeme().isEmpty() || tk.symbol().equals(TokenClass.EOF))).map(tk -> getLexemeText( tk)).collect(Collectors.joining(""));
        line = line.replaceAll("\\s+", " ");
        return line.trim();
    }
    private String getLexemeText(Token tk) {
        if( tk.symbol().equals( TokenClass.FOOD)  || tk.symbol().equals( TokenClass.DISH)) {
            return " " + tk.lexeme();
        }
        return tk.lexeme();
    }

    public String getAmount() {
        return amount.toString().trim();
    }

    @Override
    public RecipeToken getToken(TokenClass sym) {
        return lexer.getToken(sym);
    }

    @Override
    public RecipeToken nextToken() throws IOException {
        return lexer.nextToken();
    }

    public enum JflexType {

        Food, Dish
    }

    public static JflexLex getLex(Reader in, JflexType t) {
        switch (t) {
            case Food:
                return new FoodLexer(in);
            case Dish:
                return new DishLexer(in);
        }
        return null;
    }
    
    protected boolean isSizeToken( String s ) {
        Optional<String> bool = Arrays.stream( size ).filter( x -> s.startsWith( x.toLowerCase()) ).findFirst();
        return bool.isPresent();
    }

    private static final String[] size = {
        "pinch",
        "small",
        "medium",
        "large",
        "jumbo",
        "extra-large",
        "bunch",
        "bundle",
        "whole",
        "half",
        "quarter",
        "third",
        "jar",
        "can",
        "container"
    };

}
