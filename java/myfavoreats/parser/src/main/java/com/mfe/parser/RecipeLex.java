package com.mfe.parser;

import com.mfe.parser.RecipeLexer;
import java.io.FileReader;
import java.io.IOException;

public class RecipeLex {

    private static final int MAX_TOKENS = 10000000;

    public static void main(String args[]) throws java.io.IOException {
        
        String filepath;
        if ( args.length > 0) {
            filepath = args[0];
        }
        else {
            filepath = "recipe.txt";
        }
      
        FileReader fr = new FileReader( filepath );
        RecipeLex app = new RecipeLex( fr );
        app.run();
    }

    Token[] token = new Token[MAX_TOKENS];
    FileReader fr;
    String filepath;

    public RecipeLex(FileReader fr) {
        this.fr = fr;
    }

    public Token[] getTokens() {
        return token;
    }
    
    public void run() throws IOException {
        String[] lines = new String[MAX_TOKENS];
        int n, i;
        RecipeLexer lexer = new RecipeLexer(fr);
        n = -1;
       
        do {
            if (n < MAX_TOKENS) {
                token[++n] = lexer.nextToken();
            } else {
                ErrorMessage.print(0, "Maximum number of tokens exceeded");
            }
        } while (token[n].symbol() != TokenClass.EOF);

        System.out.println();
        System.out.println("List of Tokens");
        System.out.println("--------------");
        System.out.println();
        StringBuilder sb ;
        sb = new StringBuilder();
        int lastLineNum = -1;
        for( Token tk : token) {
          if ( tk!= null && tk.lineNum()!= null) {
              if (! tk.lineNum().equals(lastLineNum)) {
                  lastLineNum = tk.lineNum();
                //  System.out.println( sb.toString() );
                  sb = new StringBuilder();
              }
              if ( !tk.symbol().equals(TokenClass.OTHER)) {
                  sb.append( tk.symbol() ).append( " " ).append(tk.lexeme().trim() ).append(" ");
              }
              else {
                  sb.append( tk.lexeme() );
              }
              
              if ( tk.symbol().equals(TokenClass.RECIPE_END)) {
                  System.out.println("$$line " + tk.lineNum()  );
              } else if( !tk.symbol().equals( TokenClass.OTHER) ) {
                  System.out.println( tk.symbol() + ":" + tk.lexeme() );
              }
          }
            
        }
        System.out.println();
    }

}
