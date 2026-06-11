package com.jsoncrud.service;

import com.jsoncrud.model.Record;
import com.jsoncrud.repository.JsonFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RecordServiceTest {

    public static int passed = 0;
    public static int failed = 0;

    private static final List<Path> temps = new ArrayList<>();

    public static void main(String[] args) {
        passed = 0; failed = 0;
        try {
            testCreateFirstRecord();
            testCreateSecondRecord();
            testCreateAfterDelete();
            testCreateNullFields();
            testCreateEmptyFields();
            testCreateReturnValue();
            testFindAllEmpty();
            testFindAllMultiple();
            testFindByIdFound();
            testFindByIdNotFound();
            testFindByIdNotFoundWithRecords();
            testFindByFieldMatch();
            testFindByFieldNoMatch();
            testFindByFieldKeyNotInRecord();
            testFindByFieldNullValue();
            testFindByFieldNullKey();
            testFindByFieldBlankKey();
            testUpdateNormal();
            testUpdateIdNotFound();
            testUpdateFieldNotFound();
            testUpdateImmutability();
            testDeleteNormal();
            testDeleteIdNotFound();
            testDeleteIdGap();
        } catch (IOException e) {
            System.out.println("  [IO 오류] " + e.getMessage());
            failed++;
        } finally {
            cleanup();
        }
        System.out.println("\n[RecordServiceTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    // ── create ────────────────────────────────────────────────

    static void testCreateFirstRecord() throws IOException {
        section("create — 첫 레코드 id=1");
        RecordService svc = newService();
        Record r = svc.create(map("name", "Alice"));
        assert_(r.getId() == 1, "id=1");
    }

    static void testCreateSecondRecord() throws IOException {
        section("create — 두 번째 레코드 id=2");
        RecordService svc = newService();
        svc.create(map("k", "v"));
        Record r2 = svc.create(map("k", "v2"));
        assert_(r2.getId() == 2, "id=2");
    }

    static void testCreateAfterDelete() throws IOException {
        section("create — 삭제 후 max+1 (재사용 없음)");
        RecordService svc = newService();
        svc.create(map("k", "a")); // id=1
        svc.create(map("k", "b")); // id=2
        svc.delete(1);
        Record r = svc.create(map("k", "c"));
        assert_(r.getId() == 3, "id=3 (gap 재사용 안 함)");
    }

    static void testCreateNullFields() throws IOException {
        section("create — null fields 예외");
        RecordService svc = newService();
        assertThrows(() -> svc.create(null), IllegalArgumentException.class, "null fields");
    }

    static void testCreateEmptyFields() throws IOException {
        section("create — 빈 fields 예외");
        RecordService svc = newService();
        assertThrows(() -> svc.create(new LinkedHashMap<>()), IllegalArgumentException.class, "빈 fields");
    }

    static void testCreateReturnValue() throws IOException {
        section("create — 반환값 검증");
        RecordService svc = newService();
        Map<String, String> fields = map("email", "a@b.com");
        Record r = svc.create(fields);
        assert_("a@b.com".equals(r.getFields().get("email")), "반환 Record fields 일치");
    }

    // ── findAll ───────────────────────────────────────────────

    static void testFindAllEmpty() throws IOException {
        section("findAll — 빈 저장소");
        assert_(newService().findAll().isEmpty(), "빈 리스트");
    }

    static void testFindAllMultiple() throws IOException {
        section("findAll — 다건");
        RecordService svc = newService();
        svc.create(map("k", "a"));
        svc.create(map("k", "b"));
        assert_(svc.findAll().size() == 2, "2건 반환");
    }

    // ── findById ──────────────────────────────────────────────

    static void testFindByIdFound() throws IOException {
        section("findById — 존재하는 id");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        Optional<Record> r = svc.findById(1);
        assert_(r.isPresent(),                             "Optional 존재");
        assert_("Alice".equals(r.get().getFields().get("name")), "name 일치");
    }

    static void testFindByIdNotFound() throws IOException {
        section("findById — 없는 id");
        assert_(newService().findById(999).isEmpty(), "Optional.empty()");
    }

    static void testFindByIdNotFoundWithRecords() throws IOException {
        section("findById — 레코드 있지만 없는 id (filter λ false 분기)");
        // 빈 저장소에서는 filter 람다가 실행되지 않아 false 분기 미커버
        // 레코드가 있어야 r.getId() == id → false 분기가 실행됨
        RecordService svc = newService();
        svc.create(map("name", "Alice")); // id=1
        Optional<Record> r = svc.findById(99); // 존재하지 않는 id → filter false
        assert_(r.isEmpty(), "레코드 있어도 없는 id → Optional.empty()");
    }

    // ── findByField ───────────────────────────────────────────

    static void testFindByFieldMatch() throws IOException {
        section("findByField — 부분 일치");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        svc.create(map("name", "Bob"));
        List<Record> res = svc.findByField("name", "ali"); // 대소문자 무시
        assert_(res.size() == 1,                             "1건 반환");
        assert_("Alice".equals(res.get(0).getFields().get("name")), "Alice 반환");
    }

    static void testFindByFieldNoMatch() throws IOException {
        section("findByField — 일치 없음");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        assert_(svc.findByField("name", "xyz").isEmpty(), "빈 리스트");
    }

    static void testFindByFieldKeyNotInRecord() throws IOException {
        section("findByField — key 없는 레코드 제외");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        svc.create(map("city", "Seoul")); // name 키 없음
        List<Record> res = svc.findByField("name", "Alice");
        assert_(res.size() == 1, "name 있는 레코드만 반환");
    }

    static void testFindByFieldNullValue() throws IOException {
        section("findByField — value null → 빈 문자열 처리");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        // value=null 이면 "" 로 처리되어 모든 name 값이 "" 를 contains → Alice 반환
        List<Record> res = svc.findByField("name", null);
        assert_(res.size() == 1, "null value는 빈 문자열 처리");
    }

    static void testFindByFieldNullKey() throws IOException {
        section("findByField — null key 예외");
        RecordService svc = newService();
        assertThrows(() -> svc.findByField(null, "v"), IllegalArgumentException.class, "null key");
    }

    static void testFindByFieldBlankKey() throws IOException {
        section("findByField — 공백 key 예외");
        RecordService svc = newService();
        assertThrows(() -> svc.findByField("  ", "v"), IllegalArgumentException.class, "공백 key");
    }

    // ── update ────────────────────────────────────────────────

    static void testUpdateNormal() throws IOException {
        section("update — 정상 수정");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        Record updated = svc.update(1, "name", "Alice Kim");
        assert_("Alice Kim".equals(updated.getFields().get("name")), "반환값 변경 반영");
        assert_("Alice Kim".equals(svc.findById(1).get().getFields().get("name")), "파일 반영");
    }

    static void testUpdateIdNotFound() throws IOException {
        section("update — 없는 id 예외");
        RecordService svc = newService();
        assertThrows(() -> svc.update(99, "k", "v"), IllegalArgumentException.class, "없는 id");
    }

    static void testUpdateFieldNotFound() throws IOException {
        section("update — 없는 fieldKey 예외");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        assertThrows(() -> svc.update(1, "phone", "010"), IllegalArgumentException.class, "없는 fieldKey");
    }

    static void testUpdateImmutability() throws IOException {
        section("update — 기존 Record 불변");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        Record before = svc.findById(1).get();
        svc.update(1, "name", "Alice Kim");
        assert_("Alice".equals(before.getFields().get("name")), "기존 객체 불변");
    }

    // ── delete ────────────────────────────────────────────────

    static void testDeleteNormal() throws IOException {
        section("delete — 정상 삭제");
        RecordService svc = newService();
        svc.create(map("name", "Alice"));
        Record removed = svc.delete(1);
        assert_(removed.getId() == 1,            "삭제된 Record 반환");
        assert_(svc.findAll().isEmpty(),          "삭제 후 목록 비어있음");
    }

    static void testDeleteIdNotFound() throws IOException {
        section("delete — 없는 id 예외");
        RecordService svc = newService();
        assertThrows(() -> svc.delete(99), IllegalArgumentException.class, "없는 id");
    }

    static void testDeleteIdGap() throws IOException {
        section("delete — 삭제 후 생성 시 id 재사용 안 함");
        RecordService svc = newService();
        svc.create(map("k", "a")); // id=1
        svc.create(map("k", "b")); // id=2
        svc.create(map("k", "c")); // id=3
        svc.delete(2);
        Record r = svc.create(map("k", "d"));
        assert_(r.getId() == 4, "id=4 (gap 재사용 안 함)");
    }

    // ── 헬퍼 ────────────────────────────────────────────────

    static RecordService newService() throws IOException {
        Path p = Files.createTempFile("svc_test_", ".json");
        Files.deleteIfExists(p);
        temps.add(p);
        return new RecordService(new JsonFileRepository(p.toString()));
    }

    static Map<String, String> map(String... kvs) {
        Map<String, String> m = new LinkedHashMap<>();
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

    static void assertThrows(Runnable r, Class<? extends Throwable> type, String label) {
        try {
            r.run();
            System.out.println("    FAIL (예외 미발생): " + label);
            failed++;
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                System.out.println("    PASS (예외 발생): " + label + " -> " + t.getMessage());
                passed++;
            } else {
                System.out.println("    FAIL (예외 타입 불일치 " + t.getClass().getSimpleName() + "): " + label);
                failed++;
            }
        }
    }
}
