package org.example;

public enum TokenType {
    // Ключевые слова и типы
    INT,
    BOOL,
    BEGIN,
    END,
    IF,
    THEN,
    ELSE,
    WHILE,
    PRINT,
    TRUE,
    FALSE,
    // Лексемы
    ID,        // Идентификатор
    NUM,       // Число
    // Операторы и разделители
    EQ,        // ==
    NEQ,       // !=
    GT,        // >
    LT,        // <
    PLUS,      // +
    MINUS,     // -
    MUL,       // *
    DIV,       // /
    LPAREN,    // (
    RPAREN,    // )
    COLON,     // :
    SEMICOLON, // :
    ASSIGN,    // =
    EOF        // &
}