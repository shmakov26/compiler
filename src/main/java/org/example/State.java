package org.example;

public enum State {
    H,      // Начальное состояние
    WORD,   // Ключевое слово или идентификатор
    NUM,    // Цифры
    OP,     // +, -, *, /, >, <, =, ==, !=
    PAREN,  // Скобки
    DELIM,  // ;, :
    T       // Состояние ловушки
}