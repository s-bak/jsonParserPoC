package com.jsoncrud.menu;

import com.jsoncrud.repository.JsonFileRepository;
import com.jsoncrud.service.RecordService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MenuHandlerTest {

    public static int passed = 0;
    public static int failed = 0;

    private static final List<Path> temps = new ArrayList<>();

    public static void main(String[] args) {
        passed = 0; failed = 0;
        try {
            testExit();
            testInvalidMenuInput();
            testCreateNormal();
            testCreateEmptyFields();
            testReadAllEmpty();
            testReadAllWithRecords();
            testReadByIdFound();
            testReadByIdNotFound();
            testReadByFieldFound();
            testReadByFieldNotFound();
            testReadSubMenuInvalid();
            testUpdateNormal();
            testUpdateIdNotFound();
            testUpdateFieldNotFound();
            testUpdateEmptyFieldName();
            testUpdateNonNumericId();
            testDeleteConfirmY();
            testDeleteConfirmN();
            testDeleteIdNotFound();
            testDeleteNonNumericId();
        } catch (IOException e) {
            System.out.println("  [IO 오류] " + e.getMessage());
            failed++;
        } finally {
            cleanup();
        }
        System.out.println("\n[MenuHandlerTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    // ── 메인 메뉴 ─────────────────────────────────────────────

    static void testExit() throws IOException {
        section("Exit (0)");
        String out = run(newService(), "0\n");
        assert_(out.contains("종료합니다."), "종료 메시지");
    }

    static void testInvalidMenuInput() throws IOException {
        section("잘못된 메뉴 입력");
        String out = run(newService(), "9\n0\n");
        assert_(out.contains("잘못된 입력입니다."), "잘못된 입력 메시지");
    }

    // ── Create ────────────────────────────────────────────────

    static void testCreateNormal() throws IOException {
        section("Create 정상 흐름");
        String input = "1\nname\nAlice\nemail\nalice@example.com\n\n0\n";
        String out = run(newService(), input);
        assert_(out.contains("저장 완료. 부여된 ID: 1"), "저장 완료 메시지");
    }

    static void testCreateEmptyFields() throws IOException {
        section("Create 빈 필드");
        String out = run(newService(), "1\n\n0\n");
        assert_(out.contains("필드를 하나 이상 입력해야 합니다."), "빈 필드 메시지");
    }

    // ── Read ──────────────────────────────────────────────────

    static void testReadAllEmpty() throws IOException {
        section("Read 전체 목록 (빈)");
        String out = run(newService(), "2\n1\n0\n");
        assert_(out.contains("저장된 레코드가 없습니다."), "빈 목록 메시지");
    }

    static void testReadAllWithRecords() throws IOException {
        section("Read 전체 목록 (데이터 있음)");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        svc.create(map("name", "Bob"));
        String out = run(svc, "2\n1\n0\n");
        assert_(out.contains("총 2건"), "총 2건 출력");
    }

    static void testReadByIdFound() throws IOException {
        section("Read ID 검색 — 존재");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "2\n2\n1\n0\n");
        assert_(out.contains("Alice"), "레코드 내용 출력");
    }

    static void testReadByIdNotFound() throws IOException {
        section("Read ID 검색 — 비존재");
        String out = run(newService(), "2\n2\n99\n0\n");
        assert_(out.contains("찾을 수 없습니다."), "없는 ID 메시지");
    }

    static void testReadByFieldFound() throws IOException {
        section("Read 필드 검색 — 결과 있음");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "2\n3\nname\nali\n0\n");
        assert_(out.contains("총 1건"), "총 1건 출력");
    }

    static void testReadByFieldNotFound() throws IOException {
        section("Read 필드 검색 — 결과 없음");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "2\n3\nname\nxyz\n0\n");
        assert_(out.contains("검색 결과가 없습니다."), "검색 결과 없음 메시지");
    }

    static void testReadSubMenuInvalid() throws IOException {
        section("Read 서브메뉴 잘못된 입력");
        String out = run(newService(), "2\n9\n0\n");
        assert_(out.contains("잘못된 입력입니다."), "서브메뉴 잘못된 입력");
    }

    // ── Update ────────────────────────────────────────────────

    static void testUpdateNormal() throws IOException {
        section("Update 정상 흐름");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "3\n1\nname\nAlice Kim\n0\n");
        assert_(out.contains("수정 완료."), "수정 완료 메시지");
        assert_(out.contains("Alice Kim"), "변경값 출력");
    }

    static void testUpdateIdNotFound() throws IOException {
        section("Update — 없는 id");
        String out = run(newService(), "3\n99\n0\n");
        assert_(out.contains("찾을 수 없습니다."), "없는 ID 메시지");
    }

    static void testUpdateFieldNotFound() throws IOException {
        section("Update — 없는 필드명");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "3\n1\nphone\n0\n");
        assert_(out.contains("존재하지 않습니다."), "없는 필드 메시지");
    }

    static void testUpdateEmptyFieldName() throws IOException {
        section("Update — 빈 필드명");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "3\n1\n\n0\n");
        assert_(out.contains("필드명은 비어있을 수 없습니다."), "빈 필드명 메시지");
    }

    static void testUpdateNonNumericId() throws IOException {
        section("Update — 숫자 아닌 id");
        String out = run(newService(), "3\nabc\n0\n");
        assert_(out.contains("숫자를 입력해 주세요."), "숫자 입력 요청");
    }

    // ── Delete ────────────────────────────────────────────────

    static void testDeleteConfirmY() throws IOException {
        section("Delete — Y 확인");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "4\n1\nY\n0\n");
        assert_(out.contains("삭제되었습니다."), "삭제 완료 메시지");
    }

    static void testDeleteConfirmN() throws IOException {
        section("Delete — N 취소");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        String out = run(svc, "4\n1\nN\n0\n");
        assert_(out.contains("삭제를 취소하였습니다."), "취소 메시지");
    }

    static void testDeleteIdNotFound() throws IOException {
        section("Delete — 없는 id");
        String out = run(newService(), "4\n99\n0\n");
        assert_(out.contains("찾을 수 없습니다."), "없는 ID 메시지");
    }

    static void testDeleteNonNumericId() throws IOException {
        section("Delete — 숫자 아닌 id");
        String out = run(newService(), "4\nabc\n0\n");
        assert_(out.contains("숫자를 입력해 주세요."), "숫자 입력 요청");
    }

    // ── 헬퍼 ────────────────────────────────────────────────

    static String run(RecordService svc, String input) {
        InputStream oldIn   = System.in;
        PrintStream oldOut  = System.out;
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(capture, true, StandardCharsets.UTF_8));
        try {
            new MenuHandler(svc).run();
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
        return capture.toString(StandardCharsets.UTF_8);
    }

    static RecordService newService() throws IOException {
        Path p = Files.createTempFile("menu_test_", ".json");
        Files.deleteIfExists(p);
        temps.add(p);
        return new RecordService(new JsonFileRepository(p.toString()));
    }

    static java.util.Map<String, String> map(String... kvs) {
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) m.put(kvs[i], kvs[i + 1]);
        return m;
    }

    static void cleanup() {
        for (Path p : temps) { try { Files.deleteIfExists(p); } catch (IOException ignored) {} }
        temps.clear();
    }

    static void section(String name) { System.out.println("\n  [" + name + "]"); }

    static void assert_(boolean cond, String label) {
        if (cond) { System.out.println("    PASS: " + label); passed++; }
        else       { System.out.println("    FAIL: " + label); failed++; }
    }
}
