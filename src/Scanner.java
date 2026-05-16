    import java.util.ArrayList;
    import java.util.List;

    public class Scanner {
        private final String input;
        private int pos = 0;
        private boolean isFirstToken = true;

        public Scanner(String input) {
            this.input = input;
        }

        public List<Token> scanTokens() {
            List<Token> tokens = new ArrayList<>();
            while (pos < input.length()) {
                char c = input.charAt(pos);
                if (Character.isWhitespace(c)) { pos++; continue; }

                if (c == '=') tokens.add(handleEquals());
                else if (c == '+') tokens.add(new Token(Token.Type.PLUS, "+", pos++));
                else if (c == '-') tokens.add(new Token(Token.Type.MINUS, "-", pos++));
                else if (c == '*') tokens.add(new Token(Token.Type.STAR, "*", pos++));
                else if (c == '/') tokens.add(new Token(Token.Type.SLASH, "/", pos++));
                else if (c == '>') tokens.add(handleComparison('>', Token.Type.GREATER, Token.Type.GREATER_EQUAL));
                else if (c == '<') tokens.add(handleComparison('<', Token.Type.LESS, Token.Type.LESS_EQUAL));
                else if (c == '(') tokens.add(new Token(Token.Type.LPAREN, "(", pos++));
                else if (c == ')') tokens.add(new Token(Token.Type.RPAREN, ")", pos++));
                else if (c == ',') tokens.add(new Token(Token.Type.COMMA, ",", pos++));
                else if (c == ':') tokens.add(new Token(Token.Type.COLON, ":", pos++));
                else if (Character.isDigit(c)) tokens.add(scanNumber());
                else if (Character.isLetter(c)) tokens.add(scanAlphaNumeric());
                else tokens.add(new Token(Token.Type.INVALID, String.valueOf(c), pos++));

                isFirstToken = false;
            }
            tokens.add(new Token(Token.Type.EOF, "", pos));
            return tokens;
        }

        private Token handleEquals() {
            if (peekNext() == '=') {
                int start = pos; pos += 2;
                return new Token(Token.Type.COMPARE_EQUALS, "==", start);
            }
            return new Token(isFirstToken ? Token.Type.START_EQUALS : Token.Type.INVALID, "=", pos++);
        }

        private Token handleComparison(char c, Token.Type simple, Token.Type compound) {
            int start = pos;
            if (peekNext() == '=') { pos += 2; return new Token(compound, c + "=", start); }
            pos++; return new Token(simple, String.valueOf(c), start);
        }

        private char peekNext() { return (pos + 1 >= input.length()) ? '\0' : input.charAt(pos + 1); }

        private Token scanNumber() {
            int start = pos;
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) pos++;
            return new Token(Token.Type.INT, input.substring(start, pos), start);
        }

        private Token scanAlphaNumeric() {
            int start = pos;
            while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) pos++;
            String lexeme = input.substring(start, pos);
            Token.Type type = lexeme.matches("^[A-Z]+[0-9]+$") ? Token.Type.CELL : Token.Type.IDENTIFIER;
            return new Token(type, lexeme, start);
        }
    }