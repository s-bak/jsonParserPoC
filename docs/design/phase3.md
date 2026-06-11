# Phase 3 설계 — 콘솔 UI (Menu & Main)

## 목적

`RecordService`를 호출하는 대화형 콘솔 메뉴 루프를 구현한다.
잘못된 입력이나 예외가 발생해도 앱이 종료되지 않고 메뉴로 복귀한다.

---

## 1. 파일 구성

| 파일 | 패키지 | 역할 |
|------|--------|------|
| `Main.java` | `com.jsoncrud` | 진입점, 의존성 조립, 메인 루프 실행 |
| `MenuHandler.java` | `com.jsoncrud.menu` | 메뉴 출력, 입력 수집, Service 호출, 결과 출력 |

---

## 2. `Main.java`

```java
public static void main(String[] args) {
    JsonFileRepository repo    = new JsonFileRepository("data/records.json");
    RecordService      service = new RecordService(repo);
    MenuHandler        handler = new MenuHandler(service);
    handler.run();
}
```

- 의존성 조립만 담당하고 루프 로직은 `MenuHandler.run()`에 위임한다.

---

## 3. `MenuHandler.java`

### 3-1. 메인 루프

```
run() {
  while (true) {
    printMainMenu()
    입력 읽기
    switch(선택):
      1 → handleCreate()
      2 → handleRead()
      3 → handleUpdate()
      4 → handleDelete()
      0 → "종료합니다." 출력 후 return
      else → "잘못된 입력입니다." 출력
  }
}
```

### 3-2. 메인 메뉴 출력 형식

```
========================================
        JSON CRUD Manager
========================================
 1. Create  — 새 레코드 추가
 2. Read    — 목록 보기 / 검색
 3. Update  — 레코드 수정
 4. Delete  — 레코드 삭제
 0. Exit
========================================
선택: 
```

---

## 4. 각 기능 흐름

### 4-1. Create

```
[새 레코드 추가]
필드명 입력 (완료 시 빈 줄): name
값 입력: Alice
필드명 입력 (완료 시 빈 줄): email
값 입력: alice@example.com
필드명 입력 (완료 시 빈 줄):         ← 빈 줄 → 입력 종료

저장 완료. 부여된 ID: 1
```

- 필드가 하나도 없으면 저장하지 않고 `"필드를 하나 이상 입력해야 합니다."` 출력 후 메뉴 복귀.

### 4-2. Read — 서브메뉴

```
[목록 보기 / 검색]
 1) 전체 목록
 2) ID 검색
 3) 필드 검색
선택: 
```

**전체 목록 출력 형식**

```
----------------------------------------
 ID │ 필드
----------------------------------------
  1 │ name=Alice, email=alice@example.com
  2 │ name=Bob, email=bob@example.com
----------------------------------------
총 2건
```

- 레코드가 없으면 `"저장된 레코드가 없습니다."` 출력.

**ID 검색**

```
조회할 ID: 1
----------------------------------------
 ID: 1
 name  : Alice
 email : alice@example.com
----------------------------------------
```

- 없는 ID면 `"ID 1 를 찾을 수 없습니다."` 출력.

**필드 검색**

```
검색할 필드명: name
검색할 값 (부분 일치): ali
----------------------------------------
 ID │ 필드
----------------------------------------
  1 │ name=Alice, email=alice@example.com
----------------------------------------
총 1건
```

- 결과 없으면 `"검색 결과가 없습니다."` 출력.

### 4-3. Update

```
[레코드 수정]
수정할 ID: 1

 ID: 1
 name  : Alice
 email : alice@example.com

수정할 필드명: name
새로운 값: Alice Kim

수정 완료.
 ID: 1
 name  : Alice Kim
 email : alice@example.com
```

- 없는 ID 입력 시 `"ID 1 를 찾을 수 없습니다."` 출력 후 메뉴 복귀.
- 필드명이 비어있으면 `"필드명은 비어있을 수 없습니다."` 출력 후 메뉴 복귀.

### 4-4. Delete

```
[레코드 삭제]
삭제할 ID: 1

 ID: 1
 name  : Alice
 email : alice@example.com

정말 삭제하시겠습니까? (Y/N): Y
ID 1 이(가) 삭제되었습니다.
```

- `N` 또는 그 외 입력 시 `"삭제를 취소하였습니다."` 출력 후 메뉴 복귀.
- 없는 ID 입력 시 `"ID 1 를 찾을 수 없습니다."` 출력 후 메뉴 복귀.

---

## 5. 공통 UX 규칙

| 상황 | 처리 |
|------|------|
| 숫자 입력 필요한데 문자 입력 | `"숫자를 입력해 주세요."` 출력 후 메뉴 복귀 |
| 없는 ID | Service에서 던진 `IllegalArgumentException` 메시지 출력 후 메뉴 복귀 |
| 빈 필드명 | 안내 메시지 출력 후 메뉴 복귀 |
| 모든 작업 완료 후 | 메인 메뉴로 자동 복귀 (별도 "계속" 입력 없음) |

- `Scanner`는 `MenuHandler`가 소유하고 `Main`에서 닫는다.

---

## 6. 레코드 단건 출력 공통 헬퍼

반복 사용되는 레코드 상세 출력을 `printRecord(Record r)` 메서드로 통일한다.

```
----------------------------------------
 ID: 1
 name  : Alice
 email : alice@example.com
----------------------------------------
```

- 필드명 열을 가장 긴 키 길이에 맞춰 좌측 정렬한다.

---

## 7. 디렉토리 구조 (Phase 3 완료 시)

```
src/main/java/com/jsoncrud/
├── Main.java
├── menu/
│   └── MenuHandler.java
├── model/
│   └── Record.java
├── repository/
│   └── JsonFileRepository.java
└── service/
    └── RecordService.java
```

---

## 8. 완료 기준

- [ ] 메인 메뉴 0 입력 시 정상 종료
- [ ] Create → 파일에 레코드 저장 확인, 반환 ID 출력
- [ ] Read 전체 목록 테이블 형식 출력
- [ ] Read ID 검색 존재/비존재 양쪽 동작
- [ ] Read 필드 검색 부분 일치 결과 출력
- [ ] Update 필드 수정 후 변경 내용 출력
- [ ] Delete Y/N 확인 후 삭제 / 취소 분기
- [ ] 숫자 아닌 입력, 없는 ID 등 예외 시 앱 종료 없이 메뉴 복귀
