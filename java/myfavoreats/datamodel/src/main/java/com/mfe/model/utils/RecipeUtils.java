/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.model.utils;

import com.mfe.parser.ErrorMessage;
import com.mfe.parser.Token;
import com.mfe.parser.TokenClass;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.math3.fraction.BigFraction;
import com.mfe.parser.MeasureLexer;
/**
 *
 * @author Owner
 */
public class RecipeUtils {

    private static final int MAX_TOKENS = 3;

    public String getAdjustedMeasure(String measure) {
        StringReader sr = new StringReader(measure);
        Token token = null;
        int n;
        String lexeme = "";
        MeasureLexer lexer = new MeasureLexer(sr);
        n = -1;

        do {
            if (n < MAX_TOKENS) {
                try {
                    token = lexer.nextToken();
                    if (token.lexeme() != null) {
                        lexeme = convertMeasure(token);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(RecipeUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                ErrorMessage.print(0, "Maximum number of tokens exceeded");
            }
        } while (token.symbol() != TokenClass.EOF);
        return lexeme;
    }

    private String convertMeasure(Token token) {
        switch (token.symbol()) {
            case GALLON:
                return "gal";

            case CUP:
                return "c";
            case GRAM:
                return "g";
            case TSP:
                if (token.lexeme().equals("T")) {
                    return "tbl";
                } else {
                    return "tsp";
                }
            case TBL:
                return "tbl";
            case OZ:
                if (token.lexeme().equals("fl oz")) {
                    return "fl oz";
                } else {
                    return "oz";
                }
            case LITER:
                return "l";
            case KILO:
            	return "kg";
        }
        return token.lexeme();
    }

    public BigDecimal convertFractionToken(Token token) {
        if (token.symbol().equals(TokenClass.FRACTION)) {
            String lexeme = token.lexeme();
            String[] parts = lexeme.split("/");
            BigDecimal num = new BigDecimal(parts[0]);
            BigDecimal denom = new BigDecimal(parts[1]);
            if (denom.compareTo(BigDecimal.ZERO) != 0) {
                BigFraction bf = new BigFraction(num.longValue(), denom.longValue());
                return bf.bigDecimalValue(3, BigDecimal.ROUND_DOWN);
            }

        }

        return BigDecimal.ZERO;

    }

    public String[] getRecipePreparationSteps(String content) {
        ArrayList<String> steps = new ArrayList<String>();
        Pattern pat = Pattern.compile("[\\r\\n]");
        Matcher match = pat.matcher(content);
        content = match.replaceAll(" ");
        String[] buf = content.split("\\.\\s+");
        for (String s : buf) {
            if (s.endsWith(".")) {
                steps.add(s);
            } else {
                steps.add(s + ".");
            }
        }
        return steps.toArray(new String[steps.size()]);
    }

    public String handleTitle(Token token, String text) {
        return handleSimpleToken(TokenClass.TITLE, token, text);
    }

    public String handleYield(Token token, String text) {
        return handleSimpleToken(TokenClass.YIELD, token, text);
    }

    private String handleSimpleToken(TokenClass tk, Token token, String text) {
        if (token.symbol().equals(tk)) {
            text = text.trim();
            if (token.lexeme().length() >= text.length()) {
                return "";
            } else {
                return text.trim().substring(token.lexeme().length());
            }
        } else {
            return text;
        }
    }

    public String handleAttribution(Token token, String text) {
        return handleSimpleToken(TokenClass.ATTRIBUTION, token, text);
    }

    public String[] handleCategory(Token token, String text) {
        if (text == null || text.trim().equals("")) {
            return new String[]{};
        }
        if (token.symbol().equals(TokenClass.CATEGORY) && token.lexeme() != null && text.contains("[,;]")) {
            return text.trim().substring(token.lexeme().length()).split("[,;]");
        } else if (token.symbol().equals(TokenClass.CATEGORY)) {
            return new String[]{text.trim().substring(token.lexeme().length())};
        } else {
            return new String[]{text};
        }
    }

    public static void main(String[] args) {
        RecipeUtils util = new RecipeUtils();
        System.out.println(util.getAdjustedMeasure("ounce"));
        System.out.println(util.getAdjustedMeasure("fl oz"));
        System.out.println(util.getAdjustedMeasure("gallon"));
        System.out.println(util.getAdjustedMeasure(" T "));
        System.out.println(util.convertFractionToken(new Token(TokenClass.FRACTION, "4/5", 1)));
        System.out.println(util.handleTitle(new Token(TokenClass.TITLE, "Title:", 1), "Title: test"));
        System.out.println(util.handleYield(new Token(TokenClass.YIELD, "Yield:", 1), "Yield: test"));
        printArray(util.handleCategory(new Token(TokenClass.CATEGORY, "Categories:", 1), "Categories: cookie,desert,snack"), null);
        printArray(util.handleCategory(new Token(TokenClass.CATEGORY, " Categories:", 1), " Categories: Cookies\r"), null);
        System.out.println(util.handleAttribution(new Token(TokenClass.ATTRIBUTION, "FROM:", 1), "from:  attribution is a long text"));
    }

    public static void printArray(String[] arr, Logger log) {
        StringBuilder sb = new StringBuilder();
        for (String st : arr) {
            sb.append(st).append("; ");
        }
        if (log == null) {
            System.out.println(sb.toString());
        } else {
            log.log(Level.FINE, sb.toString());
        }
    }
}
