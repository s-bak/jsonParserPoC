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
            testInitNullParent();
            testFindAllEmptyFile();
            testFindAllNoRecordsKey();
            testFindAllRecordsNotArray();
            testSaveAndFindAll();
            testSaveEmptyList();
            testFindAllPreservesOrder();
            testInitIOException();
            testFindAllIOException();
            testSaveAllIOException();
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

    static void testInitNullParent() throws IOException {
        section("init() — getParent()==null (파일명만 있는 경로)");
        // java.nio.file.Paths.get("filename").getParent() == null → if 분기 skip
        String name = "null_parent_test_repo.json";
        Path p = java.nio.file.Paths.get(name);
        temps.add(p);
        Files.deleteIfExists(p);
        new JsonFileRepository(name);
        assert_(Files.exists(p), "부모 없는 경로에서도 파일 생성됨");
        Files.deleteIfExists(p);
    }

    static void testFindAllRecordsNotArray() throws IOException {
        section("findAll() — records 키 있지만 배열 아닌 값 → 빈 리스트");
        Path p = tempPath();
        Files.writeString(p, "{\"records\":\"not_an_array\"}", StandardCharsets.UTF_8);
        JsonFileRepository repo = new JsonFileRepository(p.toString());
        Files.writeString(p, "{\"records\":\"not_an_array\"}", StandardCharsets.UTF_8);
        List<Record> result = repo.findAll();
        assert_(result.isEmpty(), "배열 아닌 records → 빈 리스트 반환");
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

    // ── IOException 경로 ─────────────────────────────────────

    static void testInitIOException() throws IOException {
        section("init() IOException — 부모가 파일인 경우");
        // 정규 파일을 부모로 지정 → Files.createDirectories(regularFile) → FileAlreadyExistsException
        Path parentFile = Files.createTempFile("init_parent_", ".txt");
        temps.add(parentFile);
        Path target = parentFile.resolve("records.json");
        assertThrowsRuntime(() -> new JsonFileRepository(target.toString()),
                "init IOException → RuntimeException");
    }

    static void testFindAllIOException() throws IOException {
        section("findAll() IOException — 파일 경로가 디렉토리인 경우");
        Path dir = Files.createTempDirectory("findall_dir_");
        Path fileAsDir = dir.resolve("records.json");
        Files.createDirectory(fileAsDir);
        // cleanup 순서: 자식 먼저, 부모 나중
        temps.add(fileAsDir);
        temps.add(dir);
        // init()은 성공(디렉토리가 이미 존재하므로 writeString 건너뜀)
        // findAll()에서 Files.readString(directory) → IOException
        JsonFileRepository repo = new JsonFileRepository(fileAsDir.toString());
        assertThrowsRuntime(() -> repo.findAll(), "findAll IOException → RuntimeException");
    }

    static void testSaveAllIOException() throws IOException {
        section("saveAll() IOException — 파일 경로가 디렉토리인 경우");
        Path dir = Files.createTempDirectory("saveall_dir_");
        Path fileAsDir = dir.resolve("records.json");
        Files.createDirectory(fileAsDir);
        temps.add(fileAsDir);
        temps.add(dir);
        JsonFileRepository repo = new JsonFileRepository(fileAsDir.toString());
        assertThrowsRuntime(() -> repo.saveAll(new ArrayList<>()),
                "saveAll IOException → RuntimeException");
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

    static void assertThrowsRuntime(Runnable r, String label) {
        try {
            r.run();
            System.out.println("    FAIL (예외 미발생): " + label);
            failed++;
        } catch (RuntimeException e) {
            System.out.println("    PASS (예외 발생): " + label + " -> " + e.getMessage());
            passed++;
        }
    }
}
