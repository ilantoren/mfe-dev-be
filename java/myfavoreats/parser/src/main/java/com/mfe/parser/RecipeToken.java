/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mfe.parser;

/**
 *
 * @author richardthorne
 */
public class RecipeToken extends Token {

    
  
    private Integer m_charBegin;
    private Integer m_charEnd;
    
    
    /**
     *
     * @param sym
     * @param text
     * @param line
     * @param charBegin
     * @param charEnd
     */
    public RecipeToken(TokenClass sym, String text, Integer line, Integer charBegin, Integer charEnd) {
        symbol = sym;
        lexeme = text;
        lineNum = line;
        m_charBegin = charBegin;
        m_charEnd = charEnd;
    }

     public RecipeToken(TokenClass sym, String text, Integer line, Integer charBegin) {
        symbol = sym;
        lexeme = text;
        lineNum = line;
        m_charBegin = charBegin;
        m_charEnd = charBegin + text.length() ;
    }
   
    public RecipeToken( TokenClass tk) {
        super();
        this.symbol = tk;
        this.lexeme  = tk.name();
    }
    

    public TokenClass symbol() {
        return symbol;
    }

    public String lexeme() {
        return lexeme;
    }

    public Integer lineNum() {
        return lineNum;
    }
    
    public Integer position() { return m_charBegin; }
    public Integer endPosition() { return m_charEnd; }

    public String getFirstLexeme() {
        String fulLexeme = lexeme();
        return fulLexeme.split("\\s+")[0];
    }

    /*
       MG, ML, INCH,PINT,KILO, FOOD,DISH,
      OTHER, FRACTION, MEASURE, PORTION,
      CUP,LITER,GRAM,OZ,TSP,TBL,GALLON
     */
    @Override
    public String toString() {
        switch (symbol) {
            case FRACTION:
                return "(frac, " + lexeme + ") ";
            case PORTION:
                return "(por, " + lexeme + ") ";
            case MEASURE:
                return "(meas, " + lexeme + ") ";
            case LINE:
                return "(line, .) ";
            case INTEGER:
                return "(integer, " + lexeme + ") ";
            case DECFRAC:
                return "(frac, " + lexeme + ") ";
            case FOOD:
                return "(food, " + lexeme + ") ";
            case NUMBER:
                return "(number, " + lexeme + ")";
            case DISH:
                return "(dish, " + lexeme + ")";
            case LITER:
                return "(liter, " + lexeme + ")";
            case MG:
                return "(mg, " + lexeme + ")";
            case GRAM:
                return "(gram, " + lexeme + ")";
            case OZ:
                return "(oz, " + lexeme + ")";
            case CUP:
                return "(cup, " + lexeme + ")";
            case TSP:
                return "(tsp, " + lexeme + ")";
            case TBL:
                return "(tbl, " + lexeme + ")";
            case LB:
                return "(lb, " + lexeme + ")";
            case GALLON:
                return "(gal, " + lexeme + ")";
            case KILO:
                return "(kilo, " + lexeme + ")";
            case ML:
                return "(ml, " + lexeme + ")";
            case PINT:
                return "(pint, " + lexeme + ")";
            case INCH:
                return "(in, " + lexeme + ")";
            case SPACE:
                return "(spc, " + lexeme + ")";
            case TEXT:  
                return String.format(" %s", lexeme  );
            case EOF: 
                 return "";
            default:
                ErrorMessage.print(0, "Unrecognized token " + symbol.toString());
                return null;
        }
    }

}
