package  com.mfe.parser;
import java_cup.runtime.Symbol;


%%
%{
  private void echo ( String label ) { label = (label==null) ? "": label; System . out . print ( label + " " + yytext ()); }
  public int position () { return yycolumn; }
  
  StringBuilder string = new StringBuilder();
 
  private Symbol symbol(int type, Object value) {
     return new Symbol(type, yyline, yycolumn, value);
   }
%}





%class    SpamLexer
%function nextToken
%type	  Token
%unicode
%line
%column
%eofval{
  { return new Token (TokenClass . EOF); }
%eofval}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace = {LineTerminator} | [ \t\f]
INTEGER =   [0-9]*
FRACTION = {INTEGER}\.{INTEGER}|{INTEGER}"/"{INTEGER}
ADJ = 		packed
		|not packed
		|level
		|chopped
		|large
		|extra
		|shredded
		|sifted
		|small
		|heaping
		|medium




%%

[ \t\n]		{  echo (null); }
{INTEGER} {  return new Token (TokenClass . INTEGER, yytext (), yyline  ); }
{FRACTION} {  return new Token (TokenClass . FRACTION, yytext (), yyline ); }
{ADJ}      {  return new Token (TokenClass . ADJ, yytext (), yyline ); }
 . { echo(null); return new Token( TokenClass.OTHER, "", yyline); }
