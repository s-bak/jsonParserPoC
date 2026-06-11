package com.jsoncrud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainTest {

    public static int passed = 0;
    public static int failed = 0;

    public static void main(String[] args) {
        passed = 0; failed = 0;
        testMainRuns();
        System.out.println("\n[MainTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    static void testMainRuns() {
        section("Main.main() — 0 입력 시 정상 종료");
        InputStream oldIn  = System.in;
        PrintStream oldOut = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setIn(new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));
        try {
            Main.main(new String[0]);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
            // Main이 생성한 data/records.json은 그대로 둠 (기존 파일 보존)
        }
        String out = capture.toString(StandardCharsets.UTF_8);
        assert_(out.contains("종료합니다."), "종료 메시지 출력됨");
    }

    static void section(String name) { System.out.println("\n  [" + name + "]"); }

    static void assert_(boolean cond, String label) {
        if (cond) { System.out.println("    PASS: " + label); passed++; }
        else       { System.out.println("    FAIL: " + label); failed++; }
    }
}
