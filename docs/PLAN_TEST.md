# 테스트 플랜 — Unit Test (100% Coverage)

## 목표

`com.jsonparser` 라이브러리와 `com.jsoncrud` 앱의 모든 핵심 로직에 대해
단위 테스트를 작성하여 분기·예외 경로를 포함한 100% 커버리지를 달성한다.

외부 테스트 프레임워크 없이 순수 Java SE로 작성한다.
기존 `JsonParserTest.java` 방식(자체 assert 헬퍼 + `main()` 진입)을 통일 규약으로 사용한다.

---

## 테스트 파일 구조

```
src/test/java/
├── RunAllTests.java                          ← 전체 테스트 일괄 실행
└── com/
    ├── jsonparser/
    │   ├── JsonParserTest.java               ← 기존 (유지)
    │   ├── JsonLexerTest.java                ← 신규
    │   ├── JsonTokenTest.java                ← 신규
    │   └── model/
    │       └── JsonModelTest.java            ← 신규 (모델 클래스 전체)
    └── jsoncrud/
        ├── model/
        │   └── RecordTest.java               ← 신규
        ├── repository/
        │   └── JsonFileRepositoryTest.java   ← 신규
        ├── service/
        │   └── RecordServiceTest.java        ← 신규
        └── menu/
            └── MenuHandlerTest.java          ← 신규
```

빌드 스크립트: `compile_test.bat` (전체 소스 + 테스트 소스 컴파일 → `RunAllTests` 실행)

---

## 공통 규약

```java
// 각 테스트 클래스 공통 헬퍼
static int passed = 0, failed = 0;

static void assert_(boolean condition, String label) { ... }
static void assertThrows(Class<? extends Throwable> type, Runnable r, String label) { ... }
static void section(String name) { System.out.println("\n[" + name + "]"); }
```

---

## 1. `JsonTokenTest`

**대상**: `com.jsonparser.JsonToken`

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | 각 Type 열거값 생성 | `getType()` 12개 타입 모두 반환 확인 |
| 2 | `getValue()` | 생성자에 전달한 value 반환 |
| 3 | `getPosition()` | 생성자에 전달한 position 반환 |
| 4 | `toString()` | `"Token(STRING, hello, pos=0)"` 형식 확인 |

---

## 2. `JsonLexerTest`

**대상**: `com.jsonparser.JsonLexer`

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | 구조 토큰 6종 | `{ } [ ] : ,` 각각 올바른 Type 토큰 반환 |
| 2 | 문자열 토큰 | `"hello"` → STRING 토큰, value = `hello` |
| 3 | 이스케이프 시퀀스 전체 | `\" \\ \/ \b \f \n \r \t` 각각 올바른 문자 변환 |
| 4 | 유니코드 이스케이프 | `A` → `A` |
| 5 | 정수 토큰 | `42`, `-1` → NUMBER 토큰 |
| 6 | 실수 토큰 | `3.14` → NUMBER 토큰 |
| 7 | 지수 표기 | `1e10`, `2.5E-3`, `1e+2` → NUMBER 토큰 |
| 8 | `true / false / null` 리터럴 | 각각 TRUE, FALSE, NULL 토큰 |
| 9 | 공백 건너뜀 | `"  42  "` → NUMBER + EOF |
| 10 | EOF 토큰 추가 | 모든 tokenize 결과 마지막 토큰이 EOF |
| 11 | 닫히지 않은 문자열 | `"hello` → `JsonParseException` |
| 12 | 잘못된 이스케이프 | `"\q"` → `JsonParseException` |
| 13 | 짧은 유니코드 | `"\u004"` → `JsonParseException` |
| 14 | 유효하지 않은 유니코드 hex | `"\uGGGG"` → `JsonParseException` |
| 15 | 불완전한 리터럴 | `tru` → `JsonParseException` |
| 16 | 단독 `-` | `-` → `JsonParseException` |
| 17 | 알 수 없는 문자 | `@` → `JsonParseException` |
| 18 | 역슬래시 후 입력 끝 | `"\` → `JsonParseException` |

---

## 3. `JsonModelTest`

**대상**: `com.jsonparser.model.*` (JsonValue, JsonNull, JsonBoolean, JsonString, JsonNumber, JsonObject, JsonArray)

### 3-1. JsonNull

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | 싱글턴 | `JsonNull.INSTANCE == JsonNull.INSTANCE` |
| 2 | `getType()` | `Type.NULL` |
| 3 | `isNull()` | `true` |
| 4 | `toString()` | `"null"` |

### 3-2. JsonBoolean

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | TRUE 상수 | `JsonBoolean.TRUE.getValue() == true` |
| 2 | FALSE 상수 | `JsonBoolean.FALSE.getValue() == false` |
| 3 | `of(true)` | `JsonBoolean.TRUE` 반환 |
| 4 | `of(false)` | `JsonBoolean.FALSE` 반환 |
| 5 | `getType()` | `Type.BOOLEAN` |
| 6 | `isBoolean()` | `true` |
| 7 | `toString()` true | `"true"` |
| 8 | `toString()` false | `"false"` |

### 3-3. JsonString

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | `getValue()` | 생성자 값 그대로 반환 |
| 2 | `getType()` | `Type.STRING` |
| 3 | `toString()` 일반 | `"hello"` → `"\"hello\""` |
| 4 | `toString()` 이스케이프 `"` | 내부 `"` → `\"` 이스케이프 |
| 5 | `toString()` 이스케이프 `\` | 내부 `\` → `\\` 이스케이프 |
| 6 | `toString()` 이스케이프 `\n \r \t` | 각각 `\\n \\r \\t` |

### 3-4. JsonNumber

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | 정수 파싱 | `"42"` → `Long`, `asInt() == 42` |
| 2 | 음수 파싱 | `"-7"` → `asInt() == -7` |
| 3 | 실수 파싱 | `"3.14"` → `Double`, `asDouble() ≈ 3.14` |
| 4 | 지수 파싱 | `"1e2"` → `Double`, `asDouble() == 100.0` |
| 5 | `asLong()` | Long 범위 정수 정밀도 유지 |
| 6 | `getType()` | `Type.NUMBER` |
| 7 | `toString()` | 생성자에 전달한 raw 문자열 반환 |

### 3-5. JsonObject

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | `put` / `get` | 저장한 값 정확히 반환 |
| 2 | `has` 존재 | `true` |
| 3 | `has` 비존재 | `false` |
| 4 | `size()` | 추가한 개수 반환 |
| 5 | `keys()` | 삽입 순서 유지 (LinkedHashMap) |
| 6 | `entries()` | 불변 뷰 반환 |
| 7 | `toString()` 단일 | `{"k":"v"}` |
| 8 | `toString()` 복수 | 쉼표 구분 |
| 9 | `toString()` 빈 객체 | `{}` |
| 10 | `toPrettyString()` 빈 객체 | `{}` |
| 11 | `toPrettyString()` 일반 | 들여쓰기 및 줄바꿈 포함 |
| 12 | `toPrettyString()` 중첩 JsonObject | 재귀 들여쓰기 |
| 13 | `toPrettyString()` 중첩 JsonArray | 재귀 들여쓰기 |

### 3-6. JsonArray

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | `add` / `get` | 인덱스별 값 정확히 반환 |
| 2 | `size()` | 추가한 개수 반환 |
| 3 | `elements()` | 불변 리스트 반환 |
| 4 | `toString()` 빈 배열 | `[]` |
| 5 | `toString()` 복수 | `[1,2,3]` 형식 |
| 6 | `toPrettyString()` 빈 배열 | `[]` |
| 7 | `toPrettyString()` 일반 | 들여쓰기 및 줄바꿈 포함 |
| 8 | `toPrettyString()` 중첩 JsonObject | 재귀 들여쓰기 |
| 9 | `toPrettyString()` 중첩 JsonArray | 재귀 들여쓰기 |

### 3-7. JsonValue — 타입 캐스트 예외

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | `asObject()` on non-object | `ClassCastException` |
| 2 | `asArray()` on non-array | `ClassCastException` |
| 3 | `asString()` on non-string | `ClassCastException` |
| 4 | `asNumber()` on non-number | `ClassCastException` |
| 5 | `asBoolean()` on non-boolean | `ClassCastException` |
| 6 | `isObject/Array/String/Number/Boolean` 교차 확인 | 각 타입은 자신만 true |

---

## 4. `RecordTest`

**대상**: `com.jsoncrud.model.Record`

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | `getId()` | 생성자에 전달한 id 반환 |
| 2 | `getFields()` | 생성자에 전달한 fields 반환 |
| 3 | `getFields()` 불변성 | 반환된 Map에 put 시 `UnsupportedOperationException` |
| 4 | `toJsonObject()` id 필드 | JSON number로 직렬화 |
| 5 | `toJsonObject()` fields 필드 | 중첩 JsonObject로 직렬화 |
| 6 | `toJsonObject()` 빈 fields | `"fields": {}` |
| 7 | `fromJsonObject()` 정상 | id, fields 정확히 역직렬화 |
| 8 | `fromJsonObject()` fields 키 없음 | `fields` 없는 JsonObject → 빈 Map |
| 9 | `toString()` | `Record{id=1, fields={...}}` 형식 |
| 10 | 왕복 직렬화 | `toJsonObject()` → `fromJsonObject()` → 원본과 동일 |

---

## 5. `JsonFileRepositoryTest`

**대상**: `com.jsoncrud.repository.JsonFileRepository`

각 테스트는 `java.nio.file.Files.createTempFile()`로 격리된 임시 파일을 사용하고 테스트 후 삭제한다.

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | 파일 없을 때 초기화 | 생성자 호출 후 파일 존재, 내용 `{"records":[]}` |
| 2 | 파일 이미 존재 | 기존 내용 덮어쓰지 않음 |
| 3 | `findAll()` 빈 파일 | 빈 리스트 반환 |
| 4 | `findAll()` records 없는 JSON | 빈 리스트 반환 |
| 5 | `saveAll()` → `findAll()` 왕복 | 저장한 레코드 수, id, fields 일치 |
| 6 | `saveAll()` 빈 리스트 | `"records": []` 파일 저장 |
| 7 | `findAll()` 복수 레코드 | 순서 보존 확인 |

---

## 6. `RecordServiceTest`

**대상**: `com.jsoncrud.service.RecordService`

각 테스트마다 임시 파일 기반 `JsonFileRepository`를 생성해 격리한다.

### create

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | 첫 레코드 | id = 1 |
| 2 | 두 번째 레코드 | id = 2 (이전 max+1) |
| 3 | 삭제 후 생성 | 삭제로 공백 생긴 id 재사용 안 함 (max+1) |
| 4 | fields null | `IllegalArgumentException` |
| 5 | fields 빈 Map | `IllegalArgumentException` |
| 6 | 반환값 | 저장된 Record 반환, fields 일치 |

### findAll

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 7 | 빈 저장소 | 빈 리스트 |
| 8 | 다건 | 생성 순서대로 반환 |

### findById

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 9 | 존재하는 id | `Optional` 에 Record 포함 |
| 10 | 존재하지 않는 id | `Optional.empty()` |

### findByField

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 11 | 부분 일치 | 대소문자 무시, 일치 레코드 반환 |
| 12 | 일치 없음 | 빈 리스트 |
| 13 | 해당 key 없는 레코드 | 결과에서 제외 |
| 14 | value null 전달 | 빈 문자열로 처리 (예외 없음) |
| 15 | key null | `IllegalArgumentException` |
| 16 | key 공백 문자열 | `IllegalArgumentException` |

### update

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 17 | 정상 수정 | 반환 Record에 변경값 반영, 파일에도 저장 |
| 18 | 존재하지 않는 id | `IllegalArgumentException` |
| 19 | 존재하지 않는 fieldKey | `IllegalArgumentException` |
| 20 | 불변성 확인 | 기존 Record 객체 변경 없이 새 인스턴스 반환 |

### delete

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 21 | 정상 삭제 | 반환값 삭제된 Record, 이후 findAll에서 제거 확인 |
| 22 | 존재하지 않는 id | `IllegalArgumentException` |
| 23 | 삭제 후 id 공백 | 이후 create 시 삭제된 id 재사용 안 함 |

---

## 7. `MenuHandlerTest`

**대상**: `com.jsoncrud.menu.MenuHandler`

`System.setIn()`으로 입력을 주입하고 `System.setOut()`으로 출력을 캡처한다.
각 테스트 후 원래 스트림으로 복원한다.

| # | 테스트 케이스 | 검증 항목 |
|---|--------------|-----------|
| 1 | `0` 입력 | `"종료합니다."` 출력, 루프 종료 |
| 2 | 잘못된 메뉴 입력 | `"잘못된 입력입니다."` 출력 |
| 3 | Create 정상 흐름 | `"저장 완료. 부여된 ID: 1"` 출력 |
| 4 | Create 빈 필드 | `"필드를 하나 이상 입력해야 합니다."` 출력 |
| 5 | Read 전체 목록 (빈) | `"저장된 레코드가 없습니다."` 출력 |
| 6 | Read 전체 목록 (있음) | `"총 N건"` 출력 |
| 7 | Read ID 검색 존재 | ID 및 fields 출력 확인 |
| 8 | Read ID 검색 비존재 | `"ID N 를 찾을 수 없습니다."` 출력 |
| 9 | Read 필드 검색 결과 있음 | `"총 N건"` 출력 |
| 10 | Read 필드 검색 결과 없음 | `"검색 결과가 없습니다."` 출력 |
| 11 | Update 정상 흐름 | `"수정 완료."` 출력 |
| 12 | Update 없는 id | `"ID N 를 찾을 수 없습니다."` 출력 |
| 13 | Update 없는 필드명 | `"필드 'xxx' 가 존재하지 않습니다."` 출력 |
| 14 | Update 빈 필드명 | `"필드명은 비어있을 수 없습니다."` 출력 |
| 15 | Update id 숫자 아님 | `"숫자를 입력해 주세요."` 출력 |
| 16 | Delete Y 확인 | `"이(가) 삭제되었습니다."` 출력 |
| 17 | Delete N 취소 | `"삭제를 취소하였습니다."` 출력 |
| 18 | Delete 없는 id | `"ID N 를 찾을 수 없습니다."` 출력 |
| 19 | Delete id 숫자 아님 | `"숫자를 입력해 주세요."` 출력 |
| 20 | Read 서브메뉴 잘못된 입력 | `"잘못된 입력입니다."` 출력 |

---

## 8. `RunAllTests`

모든 테스트 클래스의 `main()` 을 순서대로 호출하고 전체 통과/실패 수를 집계한다.

```
실행 순서:
  1. JsonTokenTest
  2. JsonLexerTest
  3. JsonModelTest
  4. JsonParserTest (기존)
  5. RecordTest
  6. JsonFileRepositoryTest
  7. RecordServiceTest
  8. MenuHandlerTest

최종 출력:
  ===========================
  전체 결과: N 통과 / M 실패
  ===========================
```

---

## 9. `compile_test.bat`

```bat
javac -encoding UTF-8 -d out [라이브러리 소스] [앱 소스] [테스트 소스]
java -cp out RunAllTests
```

---

## 완료 기준

- [ ] 모든 테스트 클래스 컴파일 오류 없음
- [ ] `RunAllTests` 실행 시 전체 통과
- [ ] `JsonLexerTest`: Lexer 분기·오류 경로 전체 커버
- [ ] `JsonModelTest`: 모델 클래스 직렬화·캐스트 예외 전체 커버
- [ ] `RecordTest`: 직렬화 왕복 검증
- [ ] `JsonFileRepositoryTest`: 파일 I/O 격리 테스트 전체 통과
- [ ] `RecordServiceTest`: CRUD 23개 케이스 전체 통과
- [ ] `MenuHandlerTest`: UI 20개 케이스 전체 통과
