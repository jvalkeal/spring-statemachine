grammar Ssml;

@header {
package org.springframework.statemachine.dsl.ssml;
}

machine              :   objectList EOF ;
objectList           :   ( state | transition | action | guard )* ;
state                :   STATE id '{' stateParameters '}' ;
transition           :   TRANSITION id? '{' transitionParameters '}' ;
action               :   ACTION id '{' actionParameters '}' ;
guard                :   GUARD id '{' guardParameters '}' ;

// state structure
stateParameters      :   ( stateParameter ';'? )* ;
stateParameter       :   stateType id? ;
stateType            :   INITIAL | END | EXIT | DO | ENTRY ;

// transition structure
transitionParameters :   ( transitionParameter ';'? )* ;
transitionParameter  :   transitionType id? ;
transitionType       :   SOURCE | TARGET | EVENT | ACTION | GUARD ;

// action structure
actionParameters     :   ( actionParameter ';'? )* ;
actionParameter      :   actionType id? ;
actionType           :   BEAN ;

// guard structure
guardParameters      :   ( guardParameter ';'? )* ;
guardParameter       :   guardType id? ;
guardType            :   BEAN ;

id                   :   ID | STRING | NUMBER ;

// The keywords are case-independent
STATE                :   [Ss][Tt][Aa][Tt][Ee] ;
TRANSITION           :   [Tt][Rr][Aa][Nn][Ss][Ii][Tt][Ii][Oo][Nn] ;
ACTION               :   [Aa][Cc][Tt][Ii][Oo][Nn] ;
GUARD                :   [Gg][Uu][Aa][Rr][Dd] ;
INITIAL              :   [Ii][Nn][Ii][Tt][Ii][Aa][Ll] ;
END                  :   [Ee][Nn][Dd] ;
SOURCE               :   [Ss][Oo][Uu][Rr][Cc][Ee] ;
TARGET               :   [Tt][Aa][Rr][Gg][Ee][Tt] ;
EVENT                :   [Ee][Vv][Ee][Nn][Tt] ;

ENTRY                :   [Ee][Nn][Tt][Rr][Yy] ;
EXIT                 :   [Ee][Xx][Ii][Tt] ;
DO                   :   [Dd][Oo] ;
BEAN                 :   [Bb][Ee][Aa][Nn] ;

// a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? )
NUMBER               :   '-'? ('.' DIGIT+ | DIGIT+ ('.' DIGIT*)? ) ;
fragment
DIGIT                :   [0-9] ;

// any double-quoted string ("...") possibly containing escaped quotes
STRING               :   '"' ('\\"'|.)*? '"' ;

// Any string of alphabetic ([a-zA-Z\200-\377]) characters, underscores
// ('_') or digits ([0-9]), not beginning with a digit
ID                   :   LETTER (LETTER|DIGIT)*;
fragment
LETTER               :   [a-zA-Z\u0080-\u00FF_] ;

// HTML strings, angle brackets must occur in matched pairs, and
// unescaped newlines are allowed.
fragment
TAG                  :   '<' .*? '>' ;

COMMENT              :   '/*' .*? '*/'       -> skip ;
LINE_COMMENT         :   '//' .*? '\r'? '\n' -> skip ;

// a '#' character is considered a line output from a C preprocessor (e.g.,
// # 34 to indicate line 34 ) and discarded
PREPROC              :   '#' .*? '\n' -> skip ;

WS                   :   [ \t\n\r]+ -> skip ;
