public class Token {
    public enum Type {
        START_EQUALS, COMPARE_EQUALS, INT, CELL, IDENTIFIER,
        PLUS, MINUS, STAR, SLASH, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL,
        LPAREN, RPAREN, COMMA, COLON, EOF, INVALID
    }

    public final Type type;
    public final String lexeme;
    public final int pos;

    public Token(Type type, String lexeme, int pos) {
        this.type = type;
        this.lexeme = lexeme;
        this.pos = pos;
    }

    @Override
    public String toString() {
        return String.format("[%s: '%s' at %d]", type, lexeme, pos);
    }
}