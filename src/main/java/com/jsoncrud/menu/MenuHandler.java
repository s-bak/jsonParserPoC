package com.jsoncrud.menu;

import com.jsoncrud.model.Record;
import com.jsoncrud.service.RecordService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class MenuHandler {

    private static final String DIVIDER = "----------------------------------------";

    private final RecordService service;
    private final Scanner scanner;

    public MenuHandler(RecordService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        while (true) {
            printMainMenu();
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1": handleCreate(); break;
                case "2": handleRead();   break;
                case "3": handleUpdate(); break;
                case "4": handleDelete(); break;
                case "0":
                    System.out.println("종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // ── 메인 메뉴 ────────────────────────────────────────────

    private void printMainMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("        JSON CRUD Manager");
        System.out.println("========================================");
        System.out.println(" 1. Create  — 새 레코드 추가");
        System.out.println(" 2. Read    — 목록 보기 / 검색");
        System.out.println(" 3. Update  — 레코드 수정");
        System.out.println(" 4. Delete  — 레코드 삭제");
        System.out.println(" 0. Exit");
        System.out.println("========================================");
        System.out.print("선택: ");
    }

    // ── Create ───────────────────────────────────────────────

    private void handleCreate() {
        System.out.println("\n[새 레코드 추가]");
        Map<String, String> fields = new LinkedHashMap<>();

        while (true) {
            System.out.print("필드명 입력 (완료 시 빈 줄): ");
            String key = scanner.nextLine().trim();
            if (key.isEmpty()) break;
            System.out.print("값 입력: ");
            String value = scanner.nextLine();
            fields.put(key, value);
        }

        if (fields.isEmpty()) {
            System.out.println("필드를 하나 이상 입력해야 합니다.");
            return;
        }

        Record created = service.create(fields);
        System.out.println("\n저장 완료. 부여된 ID: " + created.getId());
    }

    // ── Read ─────────────────────────────────────────────────

    private void handleRead() {
        System.out.println("\n[목록 보기 / 검색]");
        System.out.println(" 1) 전체 목록");
        System.out.println(" 2) ID 검색");
        System.out.println(" 3) 필드 검색");
        System.out.print("선택: ");
        String input = scanner.nextLine().trim();

        switch (input) {
            case "1": handleReadAll();         break;
            case "2": handleReadById();        break;
            case "3": handleReadByField();     break;
            default:  System.out.println("잘못된 입력입니다.");
        }
    }

    private void handleReadAll() {
        List<Record> records = service.findAll();
        if (records.isEmpty()) {
            System.out.println("저장된 레코드가 없습니다.");
            return;
        }
        printTable(records);
    }

    private void handleReadById() {
        Integer id = readInt("조회할 ID: ");
        if (id == null) return;

        Optional<Record> found = service.findById(id);
        if (found.isPresent()) {
            System.out.println();
            printRecord(found.get());
        } else {
            System.out.println("ID " + id + " 를 찾을 수 없습니다.");
        }
    }

    private void handleReadByField() {
        System.out.print("검색할 필드명: ");
        String key = scanner.nextLine().trim();
        if (key.isEmpty()) {
            System.out.println("필드명은 비어있을 수 없습니다.");
            return;
        }
        System.out.print("검색할 값 (부분 일치): ");
        String value = scanner.nextLine();

        try {
            List<Record> results = service.findByField(key, value);
            if (results.isEmpty()) {
                System.out.println("검색 결과가 없습니다.");
            } else {
                printTable(results);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // ── Update ───────────────────────────────────────────────

    private void handleUpdate() {
        System.out.println("\n[레코드 수정]");
        Integer id = readInt("수정할 ID: ");
        if (id == null) return;

        Optional<Record> found = service.findById(id);
        if (found.isEmpty()) {
            System.out.println("ID " + id + " 를 찾을 수 없습니다.");
            return;
        }
        System.out.println();
        printRecord(found.get());

        System.out.print("\n수정할 필드명: ");
        String fieldKey = scanner.nextLine().trim();
        if (fieldKey.isEmpty()) {
            System.out.println("필드명은 비어있을 수 없습니다.");
            return;
        }
        System.out.print("새로운 값: ");
        String newValue = scanner.nextLine();

        try {
            Record updated = service.update(id, fieldKey, newValue);
            System.out.println("\n수정 완료.");
            printRecord(updated);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // ── Delete ───────────────────────────────────────────────

    private void handleDelete() {
        System.out.println("\n[레코드 삭제]");
        Integer id = readInt("삭제할 ID: ");
        if (id == null) return;

        Optional<Record> found = service.findById(id);
        if (found.isEmpty()) {
            System.out.println("ID " + id + " 를 찾을 수 없습니다.");
            return;
        }
        System.out.println();
        printRecord(found.get());

        System.out.print("\n정말 삭제하시겠습니까? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            try {
                service.delete(id);
                System.out.println("ID " + id + " 이(가) 삭제되었습니다.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("삭제를 취소하였습니다.");
        }
    }

    // ── 출력 헬퍼 ────────────────────────────────────────────

    private void printRecord(Record r) {
        int maxKeyLen = r.getFields().keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);
        System.out.println(DIVIDER);
        System.out.println(" ID: " + r.getId());
        for (Map.Entry<String, String> e : r.getFields().entrySet()) {
            String fmt = " %-" + (maxKeyLen + 1) + "s: %s";
            System.out.printf(fmt + "%n", e.getKey(), e.getValue());
        }
        System.out.println(DIVIDER);
    }

    private void printTable(List<Record> records) {
        System.out.println(DIVIDER);
        System.out.println(" ID │ 필드");
        System.out.println(DIVIDER);
        for (Record r : records) {
            StringBuilder fields = new StringBuilder();
            for (Map.Entry<String, String> e : r.getFields().entrySet()) {
                if (fields.length() > 0) fields.append(", ");
                fields.append(e.getKey()).append("=").append(e.getValue());
            }
            System.out.printf(" %2d │ %s%n", r.getId(), fields);
        }
        System.out.println(DIVIDER);
        System.out.println("총 " + records.size() + "건");
    }

    private Integer readInt(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("숫자를 입력해 주세요.");
            return null;
        }
    }
}
