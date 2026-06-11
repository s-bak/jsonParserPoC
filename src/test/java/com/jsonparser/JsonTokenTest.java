package com.jsonparser;

public class JsonTokenTest {

    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        passed = 0; failed = 0;
        testGetType();
        testGetValue();
        testGetPosition();
        testToString();
        System.out.println("\n[JsonTokenTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    static void testGetType() {
        section("getType — 모든 타입");
        for (JsonToken.Type t : JsonToken.Type.values()) {
            JsonToken tok = new JsonToken(t, "v", 0);
            assert_(tok.getType() == t, t.name());
        }
    }

    static void testGetValue() {
        section("getValue");
        JsonToken tok = new JsonToken(JsonToken.Type.STRING, "hello", 5);
        assert_("hello".equals(tok.getValue()), "value 반환");
        assert_("".equals(new JsonToken(JsonToken.Type.EOF, "", 0).getValue()), "빈 value");
    }

    static void testGetPosition() {
        section("getPosition");
        assert_(new JsonToken(JsonToken.Type.NUMBER, "1", 99).getPosition() == 99, "position 반환");
        assert_(new JsonToken(JsonToken.Type.NULL, "null", 0).getPosition() == 0, "position=0");
    }

    static void testToString() {
        section("toString");
        String s = new JsonToken(JsonToken.Type.STRING, "hi", 3).toString();
        assert_(s.contains("STRING"), "타입 포함");
        assert_(s.contains("hi"),     "value 포함");
        assert_(s.contains("3"),      "position 포함");
    }

    // ── 헬퍼 ────────────────────────────────────────────────

    static void section(String name) { System.out.println("\n  [" + name + "]"); }

    static void assert_(boolean cond, String label) {
        if (cond) { System.out.println("    PASS: " + label); passed++; }
        else       { System.out.println("    FAIL: " + label); failed++; }
    }
}
