package org.example;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("test.txt");
        List<Token> tokens = lexer.tokenize();
        for (Token tok : tokens) System.out.println(tok);
        System.out.println();

        Syntaxer parser = new Syntaxer(tokens);
        parser.parse();
    }
}