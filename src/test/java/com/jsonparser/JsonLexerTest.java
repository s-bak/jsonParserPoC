package com.jsonparser;

import com.jsonparser.exception.JsonParseException;
import java.util.List;

public class JsonLexerTest {

    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        passed = 0; failed = 0;
        testStructuralTokens();
        testStringTokens();
        testEscapeSequences();
        testUnicodeEscape();
        testNumberTokens();
        testLiteralTokens();
        testWhitespaceSkip();
        testEofToken();
        testErrors();
        System.out.println("\n[JsonLexerTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    // ── 정상 경로 ────────────────────────────────────────────

    static void testStructuralTokens() {
        section("구조 토큰 6종");
        Object[][] cases = {
            {"{", JsonToken.Type.LEFT_BRACE},
            {"}", JsonToken.Type.RIGHT_BRACE},
            {"[", JsonToken.Type.LEFT_BRACKET},
            {"]", JsonToken.Type.RIGHT_BRACKET},
            {":", JsonToken.Type.COLON},
            {",", JsonToken.Type.COMMA},
        };
        for (Object[] c : cases) {
            List<JsonToken> toks = tokenize((String) c[0]);
            assert_(toks.get(0).getType() == c[1], "토큰: " + c[0]);
        }
    }

    static void testStringTokens() {
        section("문자열 토큰");
        List<JsonToken> toks = tokenize("\"hello\"");
        assert_(toks.get(0).getType() == JsonToken.Type.STRING, "STRING 타입");
        assert_("hello".equals(toks.get(0).getValue()),          "STRING 값");
        assert_("".equals(tokenize("\"\"").get(0).getValue()),   "빈 문자열");
    }

    static void testEscapeSequences() {
        section("이스케이프 시퀀스");
        assertStringValue("\"\\\"\"", "\"",  "\\\"");
        assertStringValue("\"\\\\\"", "\\",  "\\\\");
        assertStringValue("\"\\/\"",  "/",   "\\/");
        assertStringValue("\"\\b\"",  "\b",  "\\b");
        assertStringValue("\"\\f\"",  "\f",  "\\f");
        assertStringValue("\"\\n\"",  "\n",  "\\n");
        assertStringValue("\"\\r\"",  "\r",  "\\r");
        assertStringValue("\"\\t\"",  "\t",  "\\t");
    }

    static void testUnicodeEscape() {
        section("유니코드 이스케이프");
        assertStringValue("\"\\u0041\"", "A", "\\u0041 → A");
        assertStringValue("\"\\u0042\"", "B", "\\u0042 → B");
        assertStringValue("\"\\u0020\"", " ", "\\u0020 → 공백");
    }

    static void testNumberTokens() {
        section("숫자 토큰");
        assertNumber("42",     "정수");
        assertNumber("-1",     "음수 정수");
        assertNumber("3.14",   "실수");
        assertNumber("1e10",   "지수 e");
        assertNumber("2.5E-3", "지수 E-");
        assertNumber("1e+2",   "지수 e+");
        assertNumber("-0",     "-0");
    }

    static void testLiteralTokens() {
        section("리터럴 토큰");
        assert_(tokenize("true").get(0).getType()  == JsonToken.Type.TRUE,  "true");
        assert_(tokenize("false").get(0).getType() == JsonToken.Type.FALSE, "false");
        assert_(tokenize("null").get(0).getType()  == JsonToken.Type.NULL,  "null");
    }

    static void testWhitespaceSkip() {
        section("공백 건너뜀");
        List<JsonToken> toks = tokenize("  42  ");
        assert_(toks.get(0).getType() == JsonToken.Type.NUMBER, "공백 후 NUMBER");
        assert_(toks.get(1).getType() == JsonToken.Type.EOF,    "그 다음 EOF");
    }

    static void testEofToken() {
        section("EOF 토큰");
        List<JsonToken> toks = tokenize("1");
        assert_(toks.get(toks.size() - 1).getType() == JsonToken.Type.EOF, "마지막 토큰 EOF");
        List<JsonToken> empty = tokenize("");
        assert_(empty.get(0).getType() == JsonToken.Type.EOF, "빈 입력 → EOF");
    }

    // ── 오류 경로 ────────────────────────────────────────────

    static void testErrors() {
        section("오류 경로");
        assertThrows("\"hello",    "닫히지 않은 문자열");
        assertThrows("\"\\q\"",    "잘못된 이스케이프");
        assertThrows("\"\\u04\"",  "짧은 유니코드");
        assertThrows("\"\\uGGGG\"","유효하지 않은 유니코드 hex");
        assertThrows("tru",        "불완전한 리터럴");
        assertThrows("-",          "단독 마이너스");
        assertThrows("@",          "알 수 없는 문자");
        assertThrows("\"\\",       "역슬래시 후 입력 끝");
    }

    // ── 헬퍼 ────────────────────────────────────────────────

    static List<JsonToken> tokenize(String input) {
        return new JsonLexer(input).tokenize();
    }

    static void assertStringValue(String input, String expected, String label) {
        List<JsonToken> toks = tokenize(input);
        assert_(expected.equals(toks.get(0).getValue()), label);
    }

    static void assertNumber(String input, String label) {
        List<JsonToken> toks = tokenize(input);
        assert_(toks.get(0).getType() == JsonToken.Type.NUMBER, label);
    }

    static void assertThrows(String input, String label) {
        try {
            new JsonLexer(input).tokenize();
            System.out.println("    FAIL (예외 미발생): " + label);
            failed++;
        } catch (JsonParseException e) {
            System.out.println("    PASS (예외 발생): " + label + " -> " + e.getMessage());
            passed++;
        }
    }

    static void section(String name) { System.out.println("\n  [" + name + "]"); }

    static void assert_(boolean cond, String label) {
        if (cond) { System.out.println("    PASS: " + label); passed++; }
        else       { System.out.println("    FAIL: " + label); failed++; }
    }
}
