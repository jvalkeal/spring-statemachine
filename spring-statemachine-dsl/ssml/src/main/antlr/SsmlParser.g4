parser grammar SsmlParser;

options {
  tokenVocab=SsmlLexer;
}

definitions          : ( statemachine | machineObjectList) EOF
                     ;

machineObjectList    : ( state | transition | action | guard )*
                     ;

statemachine         : STATEMACHINE id LBRACE machineObjectList RBRACE
                     ;

state                : STATE id LBRACE stateParameters RBRACE
                     ;

transition           : TRANSITION id? LBRACE transitionParameters RBRACE
                     ;

action               : ACTION id LBRACE actionParameters RBRACE
                     ;

guard                : GUARD id LBRACE guardParameters RBRACE
                     ;

// state structure
stateParameters      : ( stateParameter SEMI? )*
                     ;
stateParameter       : stateType
                     ;

stateType            : INITIAL actionId?
                     | END
                     | PARENT parentId
                     | EXIT exitId
                     | DO doId
                     | ENTRY entryId
                     ;

// transition structure
transitionParameters : ( transitionParameter SEMI? )*
                     ;

transitionParameter  : transitionType
                     ;

transitionType       : EXTERNAL
                     | INTERNAL
                     | LOCAL
                     | SOURCE sourceId
                     | TARGET targetId
                     | EVENT eventId
                     | ACTION actionId
                     | GUARD guardId
                     ;

// action structure
actionParameters     : ( actionParameter SEMI? )*
                     ;

actionParameter      : actionType
                     ;

actionType           : BEAN beanId
                     ;

// guard structure
guardParameters      : ( guardParameter SEMI? )*
                     ;

guardParameter       : guardType
                     ;

guardType            : BEAN beanId
                     ;

parentId             : ID
                     ;

targetId             : ID
                     ;

doId                 : ID
                     ;

sourceId             : ID
                     ;

exitId               : ID
                     ;

entryId              : ID
                     ;

eventId              : ID
                     ;

guardId              : ID
                     ;

actionId             : ID
                     ;

beanId               : ID
                     ;

id                   : ID
                     ;
