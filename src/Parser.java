import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) { this.tokens = tokens; }

    public Node parse() throws Exception {
        consume(Token.Type.START_EQUALS, "start with =");
        Node expr = comparison();
        if (!isAtEnd()) throw error(peek(), "unexpected tokens after formula end");
        return new FormulaNode(expr);
    }

    private Node comparison() throws Exception {
        Node node = expression();
        while (match(Token.Type.GREATER, Token.Type.GREATER_EQUAL, Token.Type.LESS, Token.Type.LESS_EQUAL, Token.Type.COMPARE_EQUALS)) {
            String op = previous().lexeme;
            node = new BinaryOpNode(op, node, expression());
        }
        return node;
    }

    private Node expression() throws Exception {
        Node node = term();
        while (match(Token.Type.PLUS, Token.Type.MINUS)) {
            String op = previous().lexeme;
            node = new BinaryOpNode(op, node, term());
        }
        return node;
    }

    private Node term() throws Exception {
        Node node = factor();
        while (match(Token.Type.STAR, Token.Type.SLASH)) {
            String op = previous().lexeme;
            node = new BinaryOpNode(op, node, factor());
        }
        return node;
    }

    private Node factor() throws Exception {
        if (match(Token.Type.MINUS)) return new UnaryOpNode("-", factor());
        if (match(Token.Type.INT)) return new LiteralNode(previous().lexeme);
        if (match(Token.Type.CELL)){
            Token cellToken = previous();
            if (check(Token.Type.COLON)) {
                advance();
                consume(Token.Type.CELL, "Expected cell reference after ':' in range");
                throw new Exception("Range is not implemented and it's optional");
            }
            return new CellNode(previous().lexeme);
        }
        if (match(Token.Type.IDENTIFIER)) return parseFunction();
        if (match(Token.Type.LPAREN)) {
            Node node = comparison();
            consume(Token.Type.RPAREN, "expected token here')'");
            return node;
        }
        throw error(peek(), "mathematical expression expected");
    }

    private Node parseFunction() throws Exception {
        String name = previous().lexeme;
        consume(Token.Type.LPAREN, "expected function name')'");
        List<Node> args = new ArrayList<>();
        if (!check(Token.Type.RPAREN)) {
            do { args.add(comparison()); } while (match(Token.Type.COMMA));
        }
        consume(Token.Type.RPAREN, "\"Expected ')' after argument");
        return new FunctionNode(name, args);
    }

    private boolean match(Token.Type... types) {
        for (Token.Type t : types) { if (check(t)) { advance(); return true; } }
        return false;
    }
    private boolean check(Token.Type t) { return !isAtEnd() && peek().type == t; }
    private Token advance() { if (!isAtEnd()) current++; return previous(); }
    private boolean isAtEnd() { return peek().type == Token.Type.EOF; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
    private Token consume(Token.Type t, String msg) throws Exception {
        if (check(t)) return advance(); throw error(peek(), msg);
    }
    private Exception error(Token t, String msg) { return new Exception(msg + " at location " + t.pos); }
}