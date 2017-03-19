package com.mfe.parser;



public class Token {

  protected TokenClass symbol;	// current token
  protected String lexeme;	// lexeme
  protected Integer lineNum;

  public Token () { }

  public Token (TokenClass symbol) {
    this (symbol, null, null);
  }

  public Token (TokenClass symbol, String lexeme, Integer lineNum) {
    this . symbol = symbol;
    this . lexeme  = lexeme;
    this . lineNum     = lineNum;
  }

  public TokenClass symbol () { return symbol; }

  public String lexeme () { return lexeme; }
  
  public Integer lineNum() { return lineNum; }
  
  public String getFirstLexeme() {
      String fulLexeme = lexeme();
      return fulLexeme.split("\\s+")[0];
      
  }

  public String toString () {
    switch (symbol) {
      case FRACTION :    return "(frac, " + lexeme + ") ";
      case PORTION :    return "(por, " + lexeme + ") ";
      case MEASURE :    return "(meas, " + lexeme + ") ";
      case ADJ :     return "(adj, " + lexeme + ") ";
      case LINE :    return "(line, .) ";
      case OTHER :     return "";
      case INTEGER :   return "(integer, " + lexeme + ") ";
      case TEXT :      return "(text, " + lexeme + ")  ";
      case SPACE :     return lexeme;
      case DECFRAC :     return lexeme;
      case FOOD    :     return "(food, " + lexeme + ") ";
      default :      
	ErrorMessage . print (0, "Unrecognized token" +lexeme);
        return null;
    }
  }

}
