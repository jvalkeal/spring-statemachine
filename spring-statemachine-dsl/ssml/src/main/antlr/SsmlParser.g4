parser grammar SsmlParser;

options {
  tokenVocab=SsmlLexer;
}

// explicitly expect eof after stamemachine or
// generic machine object list.
definitions          : ( statemachine | machineObjectList) EOF
                     ;
// on a machine level we simply have states, transitions, actions
// or guards.
machineObjectList    : ( state | transition | action | guard )*
                     ;
// statemachine always have an id and object list
// is within brackets.
statemachine         : STATEMACHINE id LBRACE machineObjectList RBRACE
                     ;
// state always have an id and its parameters are within brackets.
state                : STATE id LBRACE stateParameters RBRACE
                     ;

// transition have optional id and its parameters are within brackets.
transition           : TRANSITION id? LBRACE transitionParameters RBRACE
                     ;

// action always have an id and its parameters are within brackets.
action               : ACTION id LBRACE actionParameters RBRACE
                     ;

// guard always have an id and its parameters are within brackets.
guard                : GUARD id LBRACE guardParameters RBRACE
                     ;

// state structure
stateParameters      : ( stateParameter SEMI? )*
                     ;
stateParameter       : stateType
                     ;

// if marked as initial, it may have optional action id,
// having marked as end state,
// having parent state where parent id is mandatory,
// having exit state where exit id is mandatory,
// having do action where action id is mandatory,
// having entry state where entry id is mandatory,
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
                     
// generic id mappings
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
