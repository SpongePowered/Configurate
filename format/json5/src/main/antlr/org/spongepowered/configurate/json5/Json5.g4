grammar Json5;
/**
 * A grammar for the JSON5 document format.
 *
 * Based on the example grammar for JSON from The Definitive ANTLR 4 Reference ch6,
 * and the JSON5 and ECMAScript 5.1 specifications.
 */

@header {
package org.spongepowered.configurate.json5;
}

// Value types //

document: object EOF;

value: object#Compound
    | array#Compound
    | Json5NumericLiteral#Literal
    | Json5String#Literal
    | BooleanLiteral#Literal
    | NullLiteral#Literal
    ;

object: '{' member (',' member)* ','? '}'
    | '{' '}'
    ;
member : memberName ':' value;
memberName: IdentifierName
          | Json5String
          ;

array: '[' value (',' value)* ','? ']'
    | '[' ']'
    ;

NullLiteral: 'null';
BooleanLiteral: 'true'
                | 'false';

// ECMA Identifiers and Keywords //

IdentifierName: IdentifierStart IdentifierPart*;

Keyword: 'break' | 'do' | 'instanceof' | 'typeof'
        | 'case' | 'else' | 'new' | 'var'
        | 'catch' | 'finally' | 'return' | 'void'
        | 'continue' | 'for' | 'switch' | 'while'
        | 'debugger' | 'function' | 'this' | 'with'
        | 'default' | 'if' | 'throw'
        | 'delete' | 'in' | 'try'
        ;

FutureReservedWord: 'class' | 'enum' | 'extends' | 'super'
                     | 'const' | 'export' | 'import'
                     ;


fragment IdentifierStart: UnicodeLetter | '$' | '_' | ('\\' UnicodeEscapeSequence);
fragment IdentifierPart: IdentifierStart
    | UnicodeCombiningMark
    | UnicodeDigit
    | UnicodeConnectorPunctuation
    ;

fragment UnicodeLetter: [\p{L}\p{Nl}]; // Unicode Letter or letterlike numeric character
fragment UnicodeCombiningMark: [\p{Mn}\p{Mc}]; // Non-spacing mark or Combining spacing mark
fragment UnicodeDigit: [\p{Nd}]; // Decimal number
fragment UnicodeConnectorPunctuation: [\p{Pc}];


// Strings //

WS: [ \t\n\r\u000B\u000C\u2028\u2029\ufeff]+ -> skip;

Json5String: '"' Json5DoubleStringCharacter* '"'
            | '\'' Json5SingleStringCharacter* '\'';

fragment Json5DoubleStringCharacter: ~('\\' | '"' | '\r' | '\n')
                            | '\\' EscapeSequence
                            | LineContinuation
                            | '\u2028'
                            | '\u2029';

fragment Json5SingleStringCharacter: ~('\\' | '\'' | '\r' | '\n')
                            | '\\' EscapeSequence
                            | LineContinuation
                            | '\u2028'
                            | '\u2029';

fragment EscapeSequence: '0' DecimalDigit
                        | HexEscapeSequence
                        | UnicodeEscapeSequence
                        | ~[\r\n];

fragment LineContinuation: '\\' NL;


fragment HexEscapeSequence: 'x' HexDigit HexDigit;
fragment UnicodeEscapeSequence: 'u' HexDigit HexDigit HexDigit HexDigit;

// Numbers //

Json5NumericLiteral: [+-]? (NumericLiteral
                | 'Inifinity'
                | 'NaN');

fragment NumericLiteral: DecimalLiteral
                | HexIntegerLiteral;

DecimalLiteral:  DecimalIntegerLiteral '.' DecimalDigit* ExponentPart?
                | '.' DecimalDigit+ ExponentPart?
                | DecimalIntegerLiteral ExponentPart?;

HexIntegerLiteral: '0x' HexDigit+;
fragment DecimalIntegerLiteral: '0'
                                | NonZeroDigit DecimalDigit*;
fragment HexDigit: [0-9a-fA-F];
fragment DecimalDigit: '0' | NonZeroDigit;
fragment NonZeroDigit: [1-9];
fragment ExponentPart: [eE] [+-]? DecimalDigit+;

// Comments and newlines //

fragment NL : '\r'? '\n';

LINE_COMMENT: '//' .*? NL -> channel(HIDDEN);
BLOCK_COMMENT: '/*' .*?  '*/' -> channel(HIDDEN);
