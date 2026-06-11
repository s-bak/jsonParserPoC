package com.jsonparser;

import com.jsonparser.exception.JsonParseException;
import java.util.ArrayList;
import java.util.List;

public class JsonLexer {

    private final String input;
    private int pos;

    public JsonLexer(String input) {
        this.input = input;
        this.pos   = 0;
    }

    public List<JsonToken> tokenize() {
        List<JsonToken> tokens = new ArrayList<>();
        while (pos < input.length()) {
            skipWhitespace();
            if (pos >= input.length()) break;

            char c = input.charAt(pos);
            int start = pos;

            switch (c) {
                case '{': tokens.add(new JsonToken(JsonToken.Type.LEFT_BRACE,    "{", pos++)); break;
                case '}': tokens.add(new JsonToken(JsonToken.Type.RIGHT_BRACE,   "}", pos++)); break;
                case '[': tokens.add(new JsonToken(JsonToken.Type.LEFT_BRACKET,  "[", pos++)); break;
                case ']': tokens.add(new JsonToken(JsonToken.Type.RIGHT_BRACKET, "]", pos++)); break;
                case ':': tokens.add(new JsonToken(JsonToken.Type.COLON,         ":", pos++)); break;
                case ',': tokens.add(new JsonToken(JsonToken.Type.COMMA,         ",", pos++)); break;
                case '"': tokens.add(readString(start)); break;
                case 't': tokens.add(readLiteral("true",  JsonToken.Type.TRUE,  start)); break;
                case 'f': tokens.add(readLiteral("false", JsonToken.Type.FALSE, start)); break;
                case 'n': tokens.add(readLiteral("null",  JsonToken.Type.NULL,  start)); break;
                default:
                    if (c == '-' || Character.isDigit(c)) {
                        tokens.add(readNumber(start));
                    } else {
                        throw new JsonParseException("Unexpected character: '" + c + "'", pos);
                    }
            }
        }
        tokens.add(new JsonToken(JsonToken.Type.EOF, "", pos));
        return tokens;
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private JsonToken readString(int start) {
        pos++; // opening "
        StringBuilder sb = new StringBuilder();
        while (pos < input.length()) {
            char c = input.charAt(pos);
            if (c == '"') {
                pos++; // closing "
                return new JsonToken(JsonToken.Type.STRING, sb.toString(), start);
            }
            if (c == '\\') {
                pos++;
                if (pos >= input.length()) break;
                char escaped = input.charAt(pos);
                switch (escaped) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'u':  sb.append(readUnicode()); continue;
                    default:   throw new JsonParseException("Invalid escape: \\" + escaped, pos);
                }
            } else {
                sb.append(c);
            }
            pos++;
        }
        throw new JsonParseException("Unterminated string", start);
    }

    private char readUnicode() {
        pos++;
        if (pos + 4 > input.length()) {
            throw new JsonParseException("Invalid unicode escape", pos);
        }
        String hex = input.substring(pos, pos + 4);
        pos += 4;
        try {
            return (char) Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            throw new JsonParseException("Invalid unicode sequence: " + hex, pos - 4);
        }
    }

    private JsonToken readLiteral(String literal, JsonToken.Type type, int start) {
        if (input.startsWith(literal, pos)) {
            pos += literal.length();
            return new JsonToken(type, literal, start);
        }
        throw new JsonParseException("Unexpected token at", pos);
    }

    private JsonToken readNumber(int start) {
        StringBuilder sb = new StringBuilder();
        if (input.charAt(pos) == '-') sb.append(input.charAt(pos++));

        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos++));
        }
        if (pos < input.length() && input.charAt(pos) == '.') {
            sb.append(input.charAt(pos++));
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                sb.append(input.charAt(pos++));
            }
        }
        if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
            sb.append(input.charAt(pos++));
            if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
                sb.append(input.charAt(pos++));
            }
            while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                sb.append(input.charAt(pos++));
            }
        }
        if (sb.length() == 0 || sb.toString().equals("-")) {
            throw new JsonParseException("Invalid number", start);
        }
        return new JsonToken(JsonToken.Type.NUMBER, sb.toString(), start);
    }
}
