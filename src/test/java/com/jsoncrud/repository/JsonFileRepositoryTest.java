package com.jsoncrud.repository;

import com.jsoncrud.model.Record;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonFileRepositoryTest {

    public static int passed = 0;
    public static int failed = 0;

    private static final List<Path> temps = new ArrayList<>();

    public static void main(String[] args) {
        passed = 0; failed = 0;
        try {
            testInitCreatesFile();
            testInitKeepsExistingFile();
            testFindAllEmptyFile();
            testFindAllNoRecordsKey();
            testSaveAndFindAll();
            testSaveEmptyList();
            testFindAllPreservesOrder();
        } catch (IOException e) {
            System.out.println("  [IO 오류] " + e.getMessage());
            failed++;
        } finally {
            cleanup();
        }
        System.out.println("\n[JsonFileRepositoryTest 결과] " + passed + " 통과 / " + failed + " 실패");
    }

    static void testInitCreatesFile() throws IOException {
        section("파일 없을 때 자동 생성");
        Path p = tempPath();
        Files.deleteIfExists(p);
        new JsonFileRepository(p.toString());
        assert_(Files.exists(p), "파일 생성됨");
        String content = Files.readString(p, StandardCharsets.UTF_8);
        assert_(content.contains("records"), "초기 내용 records 키 포함");
    }

    static void testInitKeepsExistingFile() throws IOException {
        section("파일 이미 존재 시 덮어쓰지 않음");
        Path p = tempPath();
        String original = "{\"records\":[{\"id\":1,\"fields\":{\"name\":\"Alice\"}}]}";
        Files.writeString(p, original, StandardCharsets.UTF_8);
        new JsonFileRepository(p.toString());
        List<Record> records = new JsonFileRepository(p.toString()).findAll();
        assert_(records.size() == 1,                         "기존 레코드 보존");
        assert_(records.get(0).getId() == 1,                 "id 보존");
        assert_("Alice".equals(records.get(0).getFields().get("name")), "name 보존");
    }

    static void testFindAllEmptyFile() throws IOException {
        section("빈 파일 findAll → 빈 리스트");
        Path p = tempPath();
        Files.writeString(p, "", StandardCharsets.UTF_8);
        JsonFileRepository repo = new JsonFileRepository(p.toString());
        // 파일이 이미 있으므로 init은 덮어쓰지 않음. findAll에서 빈 파일 처리
        Files.writeString(p, "", StandardCharsets.UTF_8); // 강제로 비움
        List<Record> result = repo.findAll();
        assert_(result.isEmpty(), "빈 리스트 반환");
    }

    static void testFindAllNoRecordsKey() throws IOException {
        section("records 키 없는 JSON → 빈 리스트");
        Path p = tempPath();
        Files.writeString(p, "{\"other\":[]}", StandardCharsets.UTF_8);
        JsonFileRepository repo = new JsonFileRepository(p.toString());
        Files.writeString(p, "{\"other\":[]}", StandardCharsets.UTF_8);
        assert_(repo.findAll().isEmpty(), "빈 리스트 반환");
    }

    static void testSaveAndFindAll() throws IOException {
        section("saveAll → findAll 왕복");
        JsonFileRepository repo = newRepo();
        List<Record> toSave = new ArrayList<>();
        toSave.add(record(1, "name", "Alice"));
        toSave.add(record(2, "name", "Bob"));
        repo.saveAll(toSave);
        List<Record> loaded = repo.findAll();
        assert_(loaded.size() == 2,                          "레코드 수 일치");
        assert_(loaded.get(0).getId() == 1,                  "id[0] 일치");
        assert_(loaded.get(1).getId() == 2,                  "id[1] 일치");
        assert_("Alice".equals(loaded.get(0).getFields().get("name")), "name[0] 일치");
        assert_("Bob".equals(loaded.get(1).getFields().get("name")),   "name[1] 일치");
    }

    static void testSaveEmptyList() throws IOException {
        section("빈 리스트 saveAll");
        JsonFileRepository repo = newRepo();
        repo.saveAll(new ArrayList<>());
        assert_(repo.findAll().isEmpty(), "저장 후 빈 리스트");
    }

    static void testFindAllPreservesOrder() throws IOException {
        section("findAll 순서 보존");
        JsonFileRepository repo = newRepo();
        List<Record> list = new ArrayList<>();
        for (int i = 1; i <= 5; i++) list.add(record(i, "k", "v" + i));
        repo.saveAll(list);
        List<Record> loaded = repo.findAll();
        for (int i = 0; i < 5; i++) {
            assert_(loaded.get(i).getId() == i + 1, "순서[" + i + "] id=" + (i + 1));
        }
    }

    // ── 헬퍼 ────────────────────────────────────────────────

    static JsonFileRepository newRepo() throws IOException {
        Path p = tempPath();
        Files.deleteIfExists(p);
        return new JsonFileRepository(p.toString());
    }

    static Path tempPath() throws IOException {
        Path p = Files.createTempFile("repo_test_", ".json");
        temps.add(p);
        return p;
    }

    static Record record(int id, String k, String v) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(k, v);
        return new Record(id, m);
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
