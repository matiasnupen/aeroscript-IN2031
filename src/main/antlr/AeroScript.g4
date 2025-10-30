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
ARROW   : '->';

// Define all the elements of the language for the various keywords that you need
RANDOM  : 'random';
POINT   : 'point';

// Keywords for actions
RETURN  : 'return';
TO      : 'to';
BASE    : 'base';
MOVE    : 'move';
BY      : 'by';
ASCEND  : 'ascend';
DESCEND : 'descend';
TURN    : 'turn';
RIGHT   : 'right';
LEFT    : 'left';
GROUND  : 'ground';
FOR     : 'for';
SECONDS : 'seconds';
AT      : 'at';
SPEED   : 'speed';

// Keyword for reactions
ON      : 'on';
OBSTACLE : 'obstacle';
LOW_BATTERY : 'low battery';
MESSAGE : 'message';

// Keywords for identifiers and literals
ID: [a-zA-Z_]+; // Allow underscore in ID
NUMBER: [0-9]+('.'[0-9]+)?;

/** Declare the starting rule
*  A program consists of one or more mode declarations
*/
program : modeDecl+ ;

/** Declare mode (had to name it modeDecl because mode is a reserved word in ANTLR)
*  A mode can optionally start with an arrow (for the initial mode)
*  and can optionally end with an arrow followed by the next mode name
*  Statements can be empty (shown by *)
*  (for transitions between modes)
*/
modeDecl : ARROW? ID LCURL statement* RCURL (ARROW ID)?;

// declare statements
statement : action | reaction ;

/** Declare actions 
*  An action can be one of the five actions defined below
*  An action can optionally be followed by a duration (FOR ... SECONDS)
*  or a speed (AT SPEED ...)
*/
action
    : ( acDock | acMove | acTurn | acAscend | acDescend )
      ( FOR expression SECONDS | AT SPEED expression )? ;

// Define each action
acDock    : RETURN TO BASE ;
acMove    : MOVE ( TO POINT point | BY NUMBER ) ;
acTurn    : TURN ( RIGHT | LEFT )? BY expression ;
acAscend  : ASCEND BY expression ;
acDescend : DESCEND BY expression | DESCEND TO GROUND ;


// Define reactions
reaction : ON event ARROW ID ;

// Define events
event : OBSTACLE | LOW_BATTERY | MESSAGE LSQUARE ID RSQUARE ;

// Define expressions
expression : NEG expression #NegExp
            | left = expression TIMES right = expression #TimesExp
            | left = expression PLUS right = expression #PlusExp
            | left = expression MINUS right = expression #MinusExp
            | NUMBER #NumExp
            | RANDOM range #RangeExp
            | POINT point #PointExp
            | LPAREN expression RPAREN #ParentExp
            ;

// Define point and range
point : LPAREN left = expression COMMA right = expression RPAREN ;
range : LSQUARE left = expression COMMA right = expression RSQUARE ;