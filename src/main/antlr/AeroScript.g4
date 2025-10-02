grammar AeroScript;

@header {
package no.uio.aeroscript.antlr;
}

// Whitespace and comments added
WS           : [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT      : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~[\r\n]* -> channel(HIDDEN) ;

LCURL   : '{';
RCURL   : '}';
LSQUARE : '[';
RSQUARE : ']';
LPAREN  : '(';
RPAREN  : ')';

NEG     : '--';
SEMI    : ';';
COMMA   : ',';
GREATER : '>';

PLUS    : '+';
MINUS   : '-';
TIMES   : '*';

// Define all the elements of the language for the various keywords that you need
RANDOM  : 'random';
POINT   : 'point';

// Keywords
ID: [a-zA-Z_]+; // Allow underscore in ID
NUMBER: [0-9]+('.'[0-9]+)?;

// Entry point
program : (execution)+ ;

 execution : 'implement' ;


expression : NEG expression #NegExp
            | left = expression TIMES right = expression #TimesExp
            | left = expression PLUS right = expression #PlusExp
            | left = expression MINUS right = expression #MinusExp
            | NUMBER #NumExp
            | RANDOM range #RangeExp
            | POINT point #PointExp
            | LPAREN expression RPAREN #ParentExp
            ;

point : LPAREN left = expression COMMA right = expression RPAREN ;
range : LSQUARE left = expression COMMA right = expression RSQUARE ;