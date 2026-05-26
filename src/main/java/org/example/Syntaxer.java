package org.example;

import java.util.*;

public class Syntaxer {
    // Список токенов
    private final List<Token> tokens;
    // Индекс текущего токена
    private int tokenIndex = 0;
    // Таблица переменных: имя -> {тип, инициализирован}
    private final Map<String, VarInfo> symbolTable = new HashMap<>();
    // Стек типов
    private final Deque<Type> typeStack = new ArrayDeque<>();
    // Представления кода программы в форме ПОЛИЗ
    private final List<String> poliz = new ArrayList<>();
    // Счётчик уникальных меток
    private int labelCounter = 0;

    public Syntaxer(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void parse() {
        parseProgram();
        if (current().type != TokenType.EOF) {
            error("Unexpected tokens after program end");
        }
        for (int i = 0; i < poliz.size(); i++) {
            System.out.printf("%s ", poliz.get(i));
        }
    }

    // Program -> VarDecls Body &
    private void parseProgram() {
        parseVarDecls();
        parseBody();
    }

    // VarDecls -> {VarDecl;}
    private void parseVarDecls() {
        while (current().type == TokenType.ID) {
            parseVarDecl();
            if (current().type == TokenType.SEMICOLON) nextToken();
        }
    }

    // VarDecl -> Id: Type
    private void parseVarDecl() {
        String id = current().value;
        checkDecl(id, true);
        nextToken();

        check(TokenType.COLON);
        Type type = null;
        if (current().type == TokenType.INT) {
            type = Type.INT;
            nextToken();
        }
        else if (current().type == TokenType.BOOL) {
            type = Type.BOOL;
            nextToken();
        }
        else error("Expected 'int' or 'bool' after ':', received:" + current().value);

        symbolTable.put(id, new VarInfo(type, false));
    }

    // Body -> begin {Statement;} end
    private void parseBody() {
        check(TokenType.BEGIN);
        while (current().type != TokenType.END && current().type != TokenType.EOF) {
            parseStatement();
            if (current().type == TokenType.SEMICOLON) nextToken();
            else break;
        }
        check(TokenType.END);
    }

    // Statement -> Id = Comp
    // | if Comp then Body
    // | if Comp then Body else Body
    // | while Comp Body
    // | print(Expr)
    private void parseStatement() {
        switch (current().type) {
            case ID -> {
                String id = current().value;
                checkDecl(id, false);
                nextToken();
                poliz.add("@" + id);
                check(TokenType.ASSIGN);
                parseComp();
                // Проверка совпадения типов
                if (typeStack.pop() != symbolTable.get(id).getType())
                    error("Type mismatch in assignment for '" + id + "'");

                poliz.add(":=");
                symbolTable.get(id).setInitialized(true);
            }
            case IF -> {
                nextToken();
                parseComp();
                checkTypeBool("For 'if'");
                poliz.add("!F");
                int elseIdx = poliz.size() - 1;

                check(TokenType.THEN);
                parseBody();

                if (current().type == TokenType.ELSE) {
                    nextToken();
                    poliz.add("!");
                    int endLabel = labelCounter++;
                    int endIdx = poliz.size() - 1;

                    poliz.set(elseIdx, "!F " + endLabel);
                    parseBody();
                    poliz.set(endIdx, "! " + labelCounter);
                } else {
                    poliz.set(elseIdx, "!F " + labelCounter);
                }
            }
            case WHILE -> {
                nextToken();
                int startLabel = labelCounter++;
                poliz.add("LABEL " + startLabel);

                parseComp();
                checkTypeBool("For 'while'");
                poliz.add("!F");
                int endLabel = labelCounter++;
                int jumpIdx = poliz.size() - 1;

                parseBody();
                poliz.add("! " + startLabel);
                poliz.set(jumpIdx, "!F " + endLabel);
                poliz.add("LABEL " + endLabel);
            }
            case PRINT -> {
                nextToken();
                check(TokenType.LPAREN);
                parseComp(); // Вывод выражения
                check(TokenType.RPAREN);
                poliz.add("W");
            }
            default -> error("Expected statement, received: " + current().value);
        }
    }

    // Comp -> ArithExpr == ArithExpr
    // | ArithExpr != ArithExpr
    // | ArithExpr > ArithExpr
    // | ArithExpr < ArithExpr
    // | BoolNum
    // | ArithExpr
    private void parseComp() {
        if (current().type == TokenType.TRUE || current().type == TokenType.FALSE) {
            typeStack.push(Type.BOOL);
            poliz.add(current().value);
            nextToken();
            return;
        }

        parseArithExpr();
        Type t1 = typeStack.pop();

        // Если дальше идёт оператор отношения
        if (isRelOp(current().type)) {
            if (t1 != Type.INT) error("Left operand is not int");
            String op = current().value;
            nextToken();
            parseArithExpr();
            Type t2 = typeStack.pop();
            if (t2 != Type.INT) error("Right operand is not int");

            typeStack.push(Type.BOOL);
            poliz.add(op);
        } else {
            // Возвращаем тип на стек
            typeStack.push(t1);
        }
    }

    // ArithExpr -> ArithExpr + Term
    // | ArithExpr − Term
    // | Term
    private void parseArithExpr() {
        parseTerm();
        while (current().type == TokenType.PLUS || current().type == TokenType.MINUS) {
            String op = current().value;
            nextToken();
            parseTerm();
            checkArithOp(op);
            poliz.add(op);
        }
    }

    // Term -> Term ∗ Factor
    // | Term / Factor
    // | Factor
    private void parseTerm() {
        parseFactor();
        while (current().type == TokenType.MUL || current().type == TokenType.DIV) {
            String op = current().value;
            nextToken();
            parseFactor();
            checkArithOp(op);
            poliz.add(op);
        }
    }

    // Factor -> Id
    // | Num
    // | (ArithExpr)
    private void parseFactor() {
        if (current().type == TokenType.ID) {
            String id = current().value;
            checkDeclAndInit(id);
            typeStack.push(symbolTable.get(id).getType());
            poliz.add(id);
            nextToken();
        } else if (current().type == TokenType.NUM) {
            typeStack.push(Type.INT);
            poliz.add(current().value);
            nextToken();
        } else if (current().type == TokenType.LPAREN) {
            nextToken();
            parseArithExpr();
            check(TokenType.RPAREN);
        } else {
            error("Expected factor, received: " + current().value);
        }
    }

    // Проверка, что переменная объявлена, но не инициализирована
    private void checkDecl(String id, boolean allowDecl) {
        if (!symbolTable.containsKey(id)) {
            if (allowDecl) return; // Ожидается объявление
            error("Undeclared variable: " + id);
        }
    }

    // Проверка, что переменная объявлена и инициализирована
    private void checkDeclAndInit(String id) {
        checkDecl(id, false);
        if (!symbolTable.get(id).isInitialized())
            error("Uninitialized variable used: " + id);
    }

    // Проверка, что на вершине стека bool
    private void checkTypeBool(String context) {
        if (typeStack.pop() != Type.BOOL)
            error(context + " must be of type 'bool'");
    }

    // Проверка, что арифметические операции выполняются над типом int
    private void checkArithOp(String op) {
        Type t2 = typeStack.pop();
        Type t1 = typeStack.pop();
        if (t1 != Type.INT || t2 != Type.INT)
            error("Operands of '" + op + "' must be of type 'int'");
        typeStack.push(Type.INT);
    }

    // Проверка, что токен принадлежит операциям из Comp
    private boolean isRelOp(TokenType t) {
        return t == TokenType.EQ || t == TokenType.NEQ || t == TokenType.GT || t == TokenType.LT;
    }

    // Проверка типа токена
    private void check(TokenType expected) {
        if (current().type != expected)
            error("Expected " + expected + ", received: " + current().value);
        nextToken();
    }

    // Текущий токен
    private Token current() {
        return tokenIndex < tokens.size() ? tokens.get(tokenIndex) : new Token(TokenType.EOF, "EOF", -1);
    }

    // Следующий токен
    private void nextToken() {
        tokenIndex++;
    }

    // Вывод ошибки
    private void error(String msg) {
        System.out.println("Error: " + msg);
        System.exit(0);
    }
}