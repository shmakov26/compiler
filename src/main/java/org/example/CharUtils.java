package org.example;

public class CharUtils {
    // Буквенные значения
    public static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    // Числовые значения
    public static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }
    // Пробельные символы
    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }
    // Скобки
    public static boolean isParen(char c) {
        return c == '(' || c == ')';
    }
    // Операторы
    public static boolean isOp(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '>' || c == '<' || c == '=' || c == '!';
    }
    // Разделители
    public static boolean isDelim(char c) {
        return c == ';' || c == ':';
    }
}