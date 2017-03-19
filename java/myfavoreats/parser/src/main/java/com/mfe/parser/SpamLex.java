package com.mfe.parser;


import com.mfe.parser.SpamLexer;
import java.io.File;
import java.io.FileReader;
import java.net.URL;


public class SpamLex {

  private static final int MAX_TOKENS = 100000;

  public static void main (String args []) throws java.io.IOException {

    int i, n;
    Token [] token = new Token [MAX_TOKENS];
    
    String parseFile = ( args.length ==0 ) ? "./test.txt" : args[0];
   
    File testFile = new File( parseFile );

    SpamLexer lexer = new SpamLexer ( new FileReader( testFile));

    System . out . println ("Source Program");
    System . out . println ("--------------");
    System . out . println ();

    n = -1;
    Token nextToken;
    do {
      if (n < MAX_TOKENS) {
          nextToken = lexer . nextToken ();
       token [++n] = nextToken;
        System.out.println(n + "  " +  nextToken.toString());
        
      }else
	ErrorMessage.print ( 0, "Maximum number of tokens exceeded");
    } while (token [n] . symbol () != TokenClass . EOF);

    System . out . println ();
    System . out . println ("List of Tokens");
    System . out . println ("--------------");
    System . out . println ();
    for (i = 0; i < n; i++)
      System . out . println (token [i]);
    System . out . println ();
  }

}
