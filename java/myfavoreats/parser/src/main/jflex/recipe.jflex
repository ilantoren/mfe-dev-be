package  com.mfe.parser;
import java.util.*;
import java.io.IOException;

%%
%{
  // user code
  RecipeToken getToken( TokenClass sym) {  return new RecipeToken( sym , yytext(), yyline, yycolumn, yycolumn + yylength() -1); }
  private StringBuilder lineBuffer = new StringBuilder();
  private  StringBuilder sb = new StringBuilder();
  private  ArrayList<String> recipes = new ArrayList<String>();
  private String lastLine = "";
  private void echo () { System . out . print (yytext ()); }
  public int position () { return yycolumn; }
  public ArrayList<String> getRecipes() { return recipes; }
  public String getCurrentRecipe() { return lineBuffer.toString(); }
  public String getLastLine( ) {return lastLine; }
  public int getCurrentLineNumber() { return yyline; }
%}


%class    RecipeLexer
%standalone
%function nextToken
%pack
%type	  RecipeToken
%unicode
%line
%scanerror IOException
%column
%ignorecase
%eofval{
  { return getToken (TokenClass . EOF); }
%eofval}

WhiteSpace = [ \t]
LineTerminator = \r|\n|\r\n
PUNC    = {WhiteSpace}|\p{Punctuation}|\f|{LineTerminator}
INTEGER =   [0-9]+
FRACTION  = {INTEGER}"/"{INTEGER}
DECFRAC   = {INTEGER}"."{INTEGER}
RECIPE_END = "MMMMM"[\n\r]
RECIPE_START = "TT" |  "MMMMM----- Now You're Cooking! v5.65 "[^\n\r] | "MMMMM----- Now"
INGRED_SUB_LIST = MMMMM------------------------------------------\s+[:jletterdigit:]+\s+-+
BULLET =   " "*[1-9]". "
TITLE = "TITLE:"
EOL = {LineTerminator}|"      "
YIELD = "YIELD:"
CATEGORY =  Category:|Categories:
ATTRIBUTION = "from:"|"recipe by"|"contributor"|"submitted by" |"posted to" | "posted by" |"NYC Nutrilink:"
CUP =      (cup|c)(s)?{PUNC}
PINT =     (pint|pnt)
LITER =    (liter|l){PUNC}
ML =       ml{PUNC}
GRAM =     (gram|gm|g){PUNC}
MG   =     mg 
KILO =    (kg|kilo|k){PUNC}
OZ =      (ounce|oz|"fl oz"|fl|"fluid oz"){PLURAL}?{PUNC}
TSP =     (t|ts|tsp|teaspoon){PUNC}
TBL =     (tb|tbl|tablespoon|tablesp.|tbsp|T){PLURAL}?{PUNC}
LB     =  (lb|pound){PLURAL}?{PUNC}
INCH =    (in\.|inch)
GALLON =  (gal|gallon){PLURAL}?{PUNC}
QUART  =  (q|quart|qt){PLURAL}?{PUNC}


PLURAL =  s|es|ies|ii

FOOD = {FOOD_S}{PLURAL}?{PUNC}
DISH = {DISH_S}{PUNC}
ADJ  = {ADJ_S}{PUNC}
AnySpace =  {WhiteSpace} | [\f]



ADJ_S = 	packed
		|"not packed"
		| level
		| chopped
		| large
		| extra
		| shredded
		| sifted
		| small
		| heaping
		| medium
        | lg
        | sm
        | diced
        | boiled
        | bbq
        | baked
        | fried
        | dried
        | diced
        | sliced
        | picked
        | frozen
        | salted
        | stewed


FOOD_S = spam
DISH_S = "refried beans"


%state TK, ET

%%

<YYINITIAL,ET> {
{EOL}                  {  yybegin( TK);  sb.append( "\n"); lastLine = sb.toString(); lineBuffer.append( sb.toString() ); sb = new StringBuilder(); return getToken(TokenClass.EOL);   }
{AnySpace}             {  yybegin(TK);  sb.append( yytext()); return getToken( TokenClass.SPACE );  }
{PUNC}                 {  yybegin( TK ); sb.append( yytext() ); return getToken( TokenClass.OTHER ); }
   .                   {  yybegin( ET ); sb.append( yytext()); return  getToken( TokenClass.OTHER ); }
}

<TK> {
{EOL}                {  sb.append( "\n" );  lastLine = sb.toString(); lineBuffer.append( sb.toString() ); sb = new StringBuilder(); return getToken( TokenClass.EOL ); }
{FOOD}               { yybegin( ET );  sb.append( yytext()); yypushback ( 1 );      return getToken( TokenClass.FOOD );      }
{DISH}               { yybegin( ET );  sb.append( yytext()); yypushback ( 1 );      return getToken( TokenClass.DISH );      }
{FRACTION}           { yybegin( ET );  sb.append( yytext());                       return  getToken( TokenClass.FRACTION );  }
{DECFRAC}             { yybegin( ET ); sb.append( yytext());                        return getToken( TokenClass.DECFRAC );   }
{INTEGER}             { yybegin( ET ); sb.append( yytext());                        return getToken( TokenClass.INTEGER );  }
{CUP}                 { yybegin( ET ); sb.append( yytext());   yypushback( 1) ;     return getToken( TokenClass.CUP );  }
{PINT}                { yybegin( ET ); sb.append( yytext());                        return getToken( TokenClass.PINT );  }
{LITER}               { yybegin( ET ); sb.append( yytext());                        return getToken( TokenClass.LITER );   }
{ML}                  { yybegin( ET ); sb.append( yytext());     yypushback( 1) ;   return getToken( TokenClass.ML );   }
{GRAM}                { yybegin( ET );  sb.append( yytext());    yypushback( 1) ;   return getToken( TokenClass.GRAM );  }
{MG}                  { yybegin( ET );  sb.append( yytext());    yypushback( 1) ;   return getToken( TokenClass.MG );  }
{KILO}                { yybegin( ET );  sb.append( yytext());    yypushback( 1) ;   return getToken( TokenClass.KILO );  }
{OZ}                  { yybegin( ET ); sb.append( yytext());     yypushback( 1) ;   return getToken( TokenClass.OZ );  } 
{TSP}                  { yybegin( ET ); sb.append( yytext());                        return getToken( TokenClass.TSP );  } 
{TBL}                  { yybegin( ET ); sb.append( yytext());     yypushback( 1) ;   return getToken( TokenClass.TBL );  } 
{LB}                   { yybegin( ET );                                             return getToken( TokenClass.LB );  }
{QUART}                { yybegin( ET);   sb.append( yytext());  yypushback(1);      return getToken( TokenClass.QUART); }
{INCH}                { yybegin( ET ); sb.append( yytext());                        return getToken( TokenClass.INCH );  }
{GALLON}              { yybegin( ET );  sb.append( yytext());                       return getToken( TokenClass.GALLON );  }
{AnySpace}             { yybegin( TK ) ; sb.append( yytext()); return getToken( TokenClass.SPACE ); }
{RECIPE_START}       { yybegin( ET ); lineBuffer = new StringBuilder(); return  getToken( TokenClass.RECIPE_START);}
{INGRED_SUB_LIST}     { yybegin( ET );  return  getToken( TokenClass.INGRED_SUB_LIST);}
{RECIPE_END}         {  yybegin(ET); sb = new StringBuilder();return  getToken( TokenClass.RECIPE_END);}
{TITLE}              {  yybegin(ET); sb.append( yytext() ); yypushback(1); return  getToken( TokenClass.TITLE); }
{CATEGORY}           {  yybegin(ET); sb.append( yytext() ); yypushback(1); return  getToken( TokenClass.CATEGORY); }
{YIELD}              {  yybegin(ET); sb.append( yytext() ); yypushback(1); return  getToken( TokenClass.YIELD);  }
{ATTRIBUTION}        {  yybegin(ET); sb.append("\n").append( yytext() ); return getToken( TokenClass.ATTRIBUTION ); }
{BULLET}             {  yybegin(ET); sb.append( yytext() ); return  getToken( TokenClass.BULLET);  }
{ADJ}                {  yybegin(ET); sb.append( yytext() ); return getToken( TokenClass.ADJ ); }
 .                   {  yybegin( ET ); sb.append( yytext() ); return getToken(TokenClass.OTHER); }
 
}

