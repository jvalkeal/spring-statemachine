parser grammar SsmlParser;

options {
  tokenVocab=SsmlLexer;
}

definitions          :   ( statemachine | machineObjectList) EOF ;
machineObjectList    :   ( state | transition | action | guard )* ;

statemachine         :   STATEMACHINE id LBRACE machineObjectList RBRACE ;
state                :   STATE id LBRACE stateParameters RBRACE ;
transition           :   TRANSITION id? LBRACE transitionParameters RBRACE ;
action               :   ACTION id LBRACE actionParameters RBRACE ;
guard                :   GUARD id LBRACE guardParameters RBRACE ;

// state structure
stateParameters      :   ( stateParameter SEMI? )* ;
stateParameter       :   stateType id? ;
stateType            :   INITIAL | END | EXIT | DO | ENTRY ;

// transition structure
transitionParameters :   ( transitionParameter SEMI? )* ;
transitionParameter  :   transitionType id? ;
transitionType       :   SOURCE sourceId | TARGET targetId | EVENT | ACTION | GUARD ;

// action structure
actionParameters     :   ( actionParameter SEMI? )* ;
actionParameter      :   actionType id? ;
actionType           :   BEAN ;

// guard structure
guardParameters      :   ( guardParameter SEMI? )* ;
guardParameter       :   guardType id? ;
guardType            :   BEAN ;

sourceId             :   ID ;
targetId             :   ID ;
id                   :   ID ;
