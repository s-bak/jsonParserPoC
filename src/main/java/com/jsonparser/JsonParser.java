package com.jsonparser;

import com.jsonparser.exception.JsonParseException;
import com.jsonparser.model.*;
import java.util.List;

public class JsonParser {

    private final List<JsonToken> tokens;
    private int pos;

    private JsonParser(List<JsonToken> tokens) {
        this.tokens = tokens;
        this.pos    = 0;
    }

    // --- Public API ---

    public static JsonValue parse(String json) {
        if (json == null || json.isBlank()) {
            throw new JsonParseException("Input is null or empty");
        }
        List<JsonToken> tokens = new JsonLexer(json).tokenize();
        JsonParser parser = new JsonParser(tokens);
        JsonValue result  = parser.parseValue();
        if (parser.current().getType() != JsonToken.Type.EOF) {
            throw new JsonParseException("Unexpected token after root value", parser.current().getPosition());
        }
        return result;
    }

    public static JsonObject parseObject(String json) {
        JsonValue v = parse(json);
        if (!v.isObject()) throw new JsonParseException("Expected JSON object at root");
        return v.asObject();
    }

    public static JsonArray parseArray(String json) {
        JsonValue v = parse(json);
        if (!v.isArray()) throw new JsonParseException("Expected JSON array at root");
        return v.asArray();
    }

    // --- Internal parsing ---

    private JsonValue parseValue() {
        JsonToken token = current();
        switch (token.getType()) {
            case LEFT_BRACE:   return parseObject();
            case LEFT_BRACKET: return parseArray();
            case STRING:       advance(); return new JsonString(token.getValue());
            case NUMBER:       advance(); return new JsonNumber(token.getValue());
            case TRUE:         advance(); return JsonBoolean.TRUE;
            case FALSE:        advance(); return JsonBoolean.FALSE;
            case NULL:         advance(); return JsonNull.INSTANCE;
            default:
                throw new JsonParseException("Unexpected token: " + token, token.getPosition());
        }
    }

    private JsonObject parseObject() {
        expect(JsonToken.Type.LEFT_BRACE);
        JsonObject obj = new JsonObject();

        if (current().getType() == JsonToken.Type.RIGHT_BRACE) {
            advance();
            return obj;
        }

        while (true) {
            JsonToken keyToken = expect(JsonToken.Type.STRING);
            expect(JsonToken.Type.COLON);
            JsonValue value = parseValue();
            obj.put(keyToken.getValue(), value);

            if (current().getType() == JsonToken.Type.COMMA) {
                advance();
                if (current().getType() == JsonToken.Type.RIGHT_BRACE) {
                    throw new JsonParseException("Trailing comma in object", current().getPosition());
                }
            } else {
                break;
            }
        }
        expect(JsonToken.Type.RIGHT_BRACE);
        return obj;
    }

    private JsonArray parseArray() {
        expect(JsonToken.Type.LEFT_BRACKET);
        JsonArray arr = new JsonArray();

        if (current().getType() == JsonToken.Type.RIGHT_BRACKET) {
            advance();
            return arr;
        }

        while (true) {
            arr.add(parseValue());
            if (current().getType() == JsonToken.Type.COMMA) {
                advance();
                if (current().getType() == JsonToken.Type.RIGHT_BRACKET) {
                    throw new JsonParseException("Trailing comma in array", current().getPosition());
                }
            } else {
                break;
            }
        }
        expect(JsonToken.Type.RIGHT_BRACKET);
        return arr;
    }

    // --- Token helpers ---

    private JsonToken current() {
        return tokens.get(pos);
    }

    private void advance() {
        if (pos < tokens.size() - 1) pos++;
    }

    private JsonToken expect(JsonToken.Type type) {
        JsonToken token = current();
        if (token.getType() != type) {
            throw new JsonParseException(
                "Expected " + type + " but got " + token.getType(),
                token.getPosition()
            );
        }
        advance();
        return token;
    }
}
