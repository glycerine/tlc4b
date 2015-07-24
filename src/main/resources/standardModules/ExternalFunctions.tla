------------------------------ MODULE ExternalFunctions ------------------------------
EXTENDS Sequences, Integers, TLC, FiniteSets

--------------------------------------------------------------------------------------
                                (* Strings *)
RECURSIVE SPLIT1(_,_,_,_)
LOCAL SPLIT1(s,c,start,i) ==
    CASE i = Len(s)+1 -> IF i /= start 
                         THEN <<SubSeq(s,start,i-1)>> 
                         ELSE <<>>
    [] i+Len(c)-1 > Len(s) -> <<SubSeq(s,start,Len(s))>>
    [] SubSeq(s,i,i+Len(c)-1) = c -> IF i /= start 
                                   THEN <<SubSeq(s,start,i-1)>> \o SPLIT1(s,c,i+Len(c),i+Len(c)) 
                                   ELSE <<>> \o SPLIT1(s,c,i+Len(c),i+Len(c)) 
    [] OTHER -> SPLIT1(s,c,start,i+1)

STRING_SPLIT(s, c) == SPLIT1(s,c,1,1)


LOCAL DIGIT_TO_STRING(x) == 
    CASE x = 0 -> "0"
    [] x = 1 -> "1"
    [] x= 2 -> "2" 
    [] x = 3 -> "3"
    [] x = 4 -> "4"
    [] x= 5 -> "5"
    [] x= 6 -> "6"
    [] x= 7 -> "7"
    [] x=8 -> "8"
    [] x=9 -> "9"

RECURSIVE INT_TO_STRING1(_)
LOCAL INT_TO_STRING1(i) == 
    IF i < 10
    THEN DIGIT_TO_STRING(i)
    ELSE INT_TO_STRING1(i\div10) \o DIGIT_TO_STRING(i%10)

INT_TO_STRING(i) == 
    IF i < 0 
    THEN "-" \o INT_TO_STRING1(-i)
    ELSE INT_TO_STRING1(i)

LOCAL Max(S) == CHOOSE x \in S : \A p \in S : x >= p    
RECURSIVE SORT_SET(_)
SORT_SET(s) ==
    IF s = {}
    THEN {}
    ELSE LET max == Max(s)
         IN SORT_SET(s\{max}) \cup {<<Cardinality(s), max>>}
         
STRING_APPEND(a,b) == a \o b         
-----------------------------------------------------------------------------
printf(s,v) == PrintT(s) /\ PrintT(v)
    
=============================================================================