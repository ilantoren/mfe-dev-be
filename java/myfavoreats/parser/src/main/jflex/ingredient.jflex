package  com.mfe.parser;
import java.util.*;

%%
%{
  private  StringBuilder sb = new StringBuilder();
  private StringBuilder lineBuffer = new StringBuilder();
  private String lastLine;
  private boolean toggle = false;
  private boolean isPortionToken = false;
  private void echo () { System . out . print (yytext ()); }
  public int position () { return yycolumn; }
  public String getLastLine( ) {return lastLine; }
  public Boolean hasPortionToken() { return isPortionToken; }

%}


%class    IngredientLexer
%function nextToken
%type	  RecipeToken
%unicode
%line
%debug
%column
%ignorecase
%eofval{
  { lastLine = sb.toString(); return getToken(TokenClass.EOF); }
%eofval}

%{
   // user code
    RecipeToken getToken( TokenClass sym) {  return new RecipeToken( sym , yytext(), yyline, yycolumn, yycolumn + yylength() -1); }
    RecipeToken getToken( TokenClass sym, String text) {  return new RecipeToken( sym , text, yyline, yycolumn, yycolumn + yylength() -1); }
%}
WhiteSpace = [ \t]
LineTerminator = \r|\n|\r\n
PUNC    = {WhiteSpace}|\p{Punctuation}|\f|{LineTerminator}
INTEGER =   [0-9]+
FRACTION  = {INTEGER}\/{INTEGER}
Fr1_4  =  \u00BC
Fr1_2  =  \u00BD
Fr3_4  =  \u00BE
Fr1_3  =  \u2153
Fr2_3  =  \u2154
Fr1_5  =  \u2155
Fr2_5  =  \u2156
Fr3_5  =  \u2157
Fr4_5  =  \u2158
Fr1_6  =  \u2159
Fr5_6  =  \u215A
Fr1_8  =  \u215B
Fr3_8  =  \u215C
Fr5_8  =  \u215D
Fr7_8  =  \u215E
DECFRAC   = {INTEGER}\.{INTEGER}
REPLACE   =  [\+\-\!\(\)\{\}\[\]\^\|\”\~\*\?\:\;\&\\\"\'\/\.\,]
RESERVED  =  " OR " |  " AND " | " AND" EOL | " OR" EOL
NUMBER    =  ( {INTEGER} | {DECFRAC} | {FRACTION} )?
EOL       =  [\n\r]|"      "
CUP =      (cup|c){PLURAL}?{PUNC} 
PINT =     (pint|pnt){PLURAL}?{PUNC} 
LITER =    (liter|l){PLURAL}?{PUNC} 
ML =       ml{PUNC}
GRAM =    (gram|gm|g){PLURAL}?{PUNC} 
MG   =    mg{PUNC} 
KILO =    (kg|kilo){PLURAL}?{PUNC}
OZ =       (ounce|oz| "fl oz" | fl | "fluid oz"){PLURAL}?{PUNC}
TSP =      (t | ts | tsp | teaspoon){PLURAL}?{PUNC}
TBL =     (tb|tbl|tablespoon|tablesp\.| tbsp){PLURAL}?{PUNC}
LB     =  (lb|pound){PLURAL}?{PUNC}
INCH =    (in\.|inch){PLURAL}?{PUNC}
GALLON =  (gal|gallon){PLURAL}?{PUNC}
AnySpace =  {WhiteSpace} | [\f]
OTHER  =    \w+
PORTION = {PORTION_ST}{PLURAL}?{PUNC}
PLURAL  =   y|s|es|ies
PORTION_ST =    bag
 | ball
 | bar
 | base
 | "bite size"
 | block
 | bottle
 | bowl
 | box
 | bunch
 | can
 | chew
 | "child size"
 | chip
 | chop
 | chunk
 | can
 | cn
 | cone
 | container
 | corner
 | crisp
 | crumb
 | crust
 | cube
 | "cubic inch"
 | dash
 | dozen
 | ds
 | ea
 | each
 | floweret
 | frank
 | frond
 | glass
 | gourd
 | half
 | halve
 | head
 | "inside drum"
 | item
 | jar
 | jigger
 | jumbo 
 | large
 | layer
 | leaf
 | leg
 | lg
 | link
 | loaf
 | mash
 | medallion
 | melt
 | miniature
 | nugget
 | order
 | oval
 | pack
 | package
 | packet
 | pad
 | pan
 | pat
 | patt
 | peel
 | piece
 | pinch
 | pk
 | pkg
 | pn
 | pod
 | portion
 | pouch
 | puff
 | pulp
 | rack
 | rectangle
 | ring
 | roll
 | root
 | round
 | rump
 | rusk
 | sac
 | sachet
 | sack
 | saltine
 | scoop
 | section
 | seed
 | serving
 | set
 | shank
 | sheet
 | shoot
 | shred
 | side
 | single
 | sl
 | sleeve
 | slice
 | small
 | "small shell"
 | "small square"
 | "snack size"
 | spar
 | spear
 | spiral
 | spray
 | sprig
 | sprout
 | square
 | stalk
 | stem
 | stick
 | strip
 | "super colossal"
 | "super size"
 | tablet
 | teeth
 | thigh
 | "thin square"
 | tube
 | twist
 | unit
 | wedge
 | wrapper

%state TK, ET
 
%%


<YYINITIAL,ET> {
{EOL}                  {  yybegin( TK);  sb.append( "\n"); lastLine = sb.toString(); lineBuffer.append( lastLine ); sb = new StringBuilder(); return getToken(TokenClass.EOL);   }
{AnySpace}             {  yybegin(TK);  sb.append( yytext()); return getToken( TokenClass.SPACE, " " );  }
{PUNC}                 {  yybegin( TK ); sb.append(" "); return getToken(TokenClass.SPACE, " "); }
{OTHER}                {  yybegin( TK);   sb.append( yytext()); return getToken( TokenClass.OTHER ); }
{REPLACE}              {  yybegin( TK );  sb.append(" ");  return getToken( TokenClass.SPACE, " ");  }
:[alpha]:                     {  yybegin( ET );  sb.append( yytext() );  return getToken( TokenClass.OTHER); }
   .                   {  yybegin( ET ); sb.append( yytext()); return  getToken( TokenClass.OTHER ); }
}

<TK> {
{CUP}        { yybegin(ET); yypushback(1); return getToken( TokenClass.   CUP, "cup"); }
{PINT}       { yybegin(ET); yypushback(1);return getToken( TokenClass.  PINT, "pint" ); }
{LITER}      { yybegin(ET); yypushback(1);return getToken( TokenClass. LITER, "liter" ); }
{ML}         { yybegin(ET); yypushback(1);return getToken( TokenClass.    ML, "ml" ); }
{GRAM}       { yybegin(ET); yypushback(1);return getToken( TokenClass.  GRAM, "gram" ); }   
{MG}         { yybegin(ET); yypushback(1);return getToken( TokenClass.    MG, "mg" ); }
{KILO}       { yybegin(ET); yypushback(1);return getToken( TokenClass.  KILO, "kilo" ); }
{OZ}         { yybegin(ET); yypushback(1);return getToken( TokenClass.    OZ, "oz" ); }
{TBL}        { yybegin(ET); yypushback(1);return getToken( TokenClass.   TBL, "tbl" ); }
{LB}         { yybegin(ET); yypushback(1);return getToken( TokenClass.    LB, "lb" ); }
{INCH}       { yybegin(ET); yypushback(1);return getToken( TokenClass.  INCH, "in." ); }  
{GALLON}     { yybegin(ET); yypushback(1);return getToken( TokenClass.GALLON, "gal" ); }
{TSP}        { yybegin(ET); yypushback(1);return getToken( TokenClass.   TSP, "tsp" ); }

{PUNC}          { yybegin(TK);  sb.append(" ");  return getToken(TokenClass.SPACE, " "); }
{DECFRAC}        { yybegin(ET);           return getToken( TokenClass.DECFRAC); }
{FRACTION}          { yybegin(ET);           return getToken( TokenClass.FRACTION); }
{INTEGER}        { yybegin(ET);           return getToken( TokenClass.INTEGER); }
{REPLACE}       { yybegin(TK); sb.append( " "); return getToken(TokenClass.SPACE, " ");}
{PORTION}       {yybegin(TK); yypushback(1);    return  getToken( TokenClass. PORTION); }                  
{RESERVED}      { yybegin(TK); sb.append(" "); return getToken(TokenClass.SPACE, " ");}
{EOL}          { yybegin(ET); yypushback(1); lastLine = sb.toString(); return getToken(TokenClass.SPACE, " ");  }
{Fr1_4}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "1/4"); }
{Fr1_2}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "1/2"); }
{Fr3_4}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "3/4"); }
{Fr1_3}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "1/3"); }
{Fr2_3}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "2/3"); }
{Fr1_5}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "1/5"); }
{Fr2_5}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "2/5"); }
{Fr3_5}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "3/5"); }
{Fr4_5}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "4/5"); }
{Fr1_6}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "1/6"); }
{Fr5_6}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "5/6"); }
{Fr1_8}	{ yybegin(ET);  return getToken( TokenClass.FRACTION,"1/8"); }
{Fr3_8}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "3/8"); }
{Fr5_8}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "5/8"); }
{Fr7_8}	{ yybegin(ET);  return getToken( TokenClass.FRACTION, "7/8"); }
 .              {  yybegin(ET); yypushback(1); }
 }