
// pico statements excerpt

%%

Program : 'b' Stats 'e' ;

Stats : Statsn | ;

Statsn : Stat ';' Statsn | Stat ;

Stat : 'i' Id 't' Stats 'e' Stats 'f' 
     | 'w' Id 'd' Stats 'o'
     | Id '=' Id
;

Id : Char Id | Char ' ' ;

Char : 'i' | 't' | 'e' | 'f' | 'w' | 'd' | 'o' ;


