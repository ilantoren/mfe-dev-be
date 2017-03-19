package com.mfe.parser;

%%
%class   MeasureLexer
%public
%function nextToken
%type	  Token
%unicode
%line
%column
%ignorecase
%eofval{
  { return new Token (TokenClass . EOF); }
%eofval}


INTEGER =   [0-9]+
FRACTION  = {INTEGER}\/{INTEGER}
DECFRAC   = {INTEGER}\.{INTEGER}
MG   =     " mg " 
CUP =      cup|" c "
LITER =    liter|" l "
GRAM =    gram|gm|" g "
OZ =      ounce|ounces|" oz" | "fl oz"
LB     =  lb |pound | Lb
TSP =     " t "|" ts"|tsp|teaspoon
TBL =     " tb "|tbl|tablespoon|"tablesp. "
GALLON =  gal|gallon

%%
{CUP}     {  return new Token (TokenClass . CUP, yytext (), yyline ); }
{LITER}   {  return new Token (TokenClass . LITER, yytext (), yyline ); }
{GRAM}    {  return new Token (TokenClass . GRAM, yytext (), yyline ); }
{OZ}      {  return new Token (TokenClass . OZ, yytext (), yyline ); }
{TSP}     {  return new Token (TokenClass . TSP, yytext (), yyline ); }
{TBL}     {  return new Token (TokenClass . TBL, yytext (), yyline ); }
{LB}      {  return new Token (TokenClass . LB,  yytext (), yyline ); }
{GALLON}  {  return new Token (TokenClass . GALLON, yytext (), yyline ); }
