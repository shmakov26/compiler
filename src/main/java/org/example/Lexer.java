package org.example;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    //текущий элемент
    private int curr;
    //поток чтения
    private final FileInputStream fis;
    //текущее состояние автомата
    private State state = State.H;
    //текущий номер строки
    private int line = 1;

    public Lexer(String filename) throws IOException {
        fis = new FileInputStream(filename);
        nextSymbol();
    }

    // Считывает всё до EOF
    public List<Token> tokenize() throws IOException {
        List<Token> tokens = new ArrayList<>();
        Token tok = nextToken();
        while (tok.type != TokenType.EOF) {
            tokens.add(tok);
            tok = nextToken();
        }
        tokens.add(tok);
        fis.close();
        return tokens;
    }

    // Получение следующего токена
    private Token nextToken() throws IOException {
        state = State.H;
        while (true) {
            switch (state) {
                case H -> { Token t = H(); if (t != null) return t; }
                case WORD -> { return WORD(); }
                case NUM -> { return NUM(); }
                case OP -> { return OP(); }
                case PAREN -> { return PAREN(); }
                case DELIM -> { return DELIM(); }
                case T -> { return T(); }
            }
        }
    }

    // Основное состояние
    private Token H() throws IOException {
        while (curr != -1 && CharUtils.isWhitespace((char) curr)) nextSymbol();
        if (curr == -1) return new Token(TokenType.EOF, "EOF", line);
        char ch = (char) curr;
        if (CharUtils.isLetter(ch)) state = State.WORD;
        else if (CharUtils.isDigit(ch)) state = State.NUM;
        else if (CharUtils.isParen(ch)) state = State.PAREN;
        else if (CharUtils.isOp(ch)) state = State.OP;
        else if (CharUtils.isDelim(ch)) state = State.DELIM;
        else state = State.T;
        return null;
    }

    private Token WORD() throws IOException {
        int tokenLine = line;
        StringBuilder sb = new StringBuilder();
        while (curr != -1 && (CharUtils.isLetter((char)curr) || CharUtils.isDigit((char)curr))) {
            sb.append((char)curr);
            nextSymbol();
        }
        String val = sb.toString();
        return switch(val) {
            case "int" -> new Token(TokenType.INT, val, tokenLine);
            case "bool" -> new Token(TokenType.BOOL, val, tokenLine);
            case "begin" -> new Token(TokenType.BEGIN, val, tokenLine);
            case "end" -> new Token(TokenType.END, val, tokenLine);
            case "if" -> new Token(TokenType.IF, val, tokenLine);
            case "then" -> new Token(TokenType.THEN, val, tokenLine);
            case "else" -> new Token(TokenType.ELSE, val, tokenLine);
            case "while" -> new Token(TokenType.WHILE, val, tokenLine);
            case "print" -> new Token(TokenType.PRINT, val, tokenLine);
            case "True" -> new Token(TokenType.TRUE, val, tokenLine);
            case "False" -> new Token(TokenType.FALSE, val, tokenLine);
            default -> new Token(TokenType.ID, val, tokenLine);
        };
    }

    private Token NUM() throws IOException {
        int tokenLine = line;
        StringBuilder sb = new StringBuilder();
        while (curr != -1 && CharUtils.isDigit((char)curr)) {
            sb.append((char)curr);
            nextSymbol();
        }
        return new Token(TokenType.NUM, sb.toString(), tokenLine);
    }

    private Token OP() throws IOException {
        int tokenLine = line;
        char ch = (char) curr;
        nextSymbol();
        return switch(ch) {
            case '+' -> new Token(TokenType.PLUS, "+", tokenLine);
            case '-' -> new Token(TokenType.MINUS, "-", tokenLine);
            case '*' -> new Token(TokenType.MUL, "*", tokenLine);
            case '/' -> new Token(TokenType.DIV, "/", tokenLine);
            case '>' -> new Token(TokenType.GT, ">", tokenLine);
            case '<' -> new Token(TokenType.LT, "<", tokenLine);
            case '=' -> {
                if (curr == '=') { nextSymbol(); yield new Token(TokenType.EQ, "==", tokenLine); }
                yield new Token(TokenType.ASSIGN, "=", tokenLine);
            }
            case '!' -> {
                if (curr == '=') { nextSymbol(); yield new Token(TokenType.NEQ, "!=", tokenLine); }
                else { state = State.T; yield null; }
            }
            default -> { state = State.T; yield null; }
        };
    }

    private Token PAREN() throws IOException {
        int tokenLine = line;
        char ch = (char) curr;
        nextSymbol();
        return ch == '(' ? new Token(TokenType.LPAREN, "(", tokenLine) : new Token(TokenType.RPAREN, ")", tokenLine);
    }

    private Token DELIM() throws IOException {
        char ch = (char) curr;
        nextSymbol();
        return ch == ';' ? new Token(TokenType.SEMICOLON, ";", line) : new Token(TokenType.COLON, ":", line);
    }

    private Token T() throws IOException {
        char ch = (char) curr;
        nextSymbol();
        throw new RuntimeException("Incorrect char: '" + ch + "' at line " + line);
    }

    private void nextSymbol() throws IOException {
        curr = fis.read();
        if (curr == '\n') line++;
    }
}