public class Token {
    public enum Type {
        START_EQUALS, IDENTIFIER, LPAREN, RPAREN, COMMA, GREATER, STAR, PLUS,
        MINUS, INT, CELL, COLON, EOF, COMPARE_EQUALS, GREATER_EQUAL,
        LESS, LESS_EQUAL, SLASH,INVALID
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