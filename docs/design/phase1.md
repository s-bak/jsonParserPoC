# Phase 1 설계 — 도메인 모델 & Repository 계층

## 목적

CRUD 앱의 데이터 구조(`Record`)와 JSON 파일 I/O(`JsonFileRepository`)를 정의한다.
모든 상위 계층(Service, UI)은 이 두 클래스에만 의존한다.

---

## 1. `Record.java`

**패키지**: `com.jsoncrud.model`

### 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | `int` | 자동 증가 고유 식별자 |
| `fields` | `Map<String, String>` | 사용자 정의 키-값 쌍 |

### 메서드

```java
// 생성자
Record(int id, Map<String, String> fields)

// 접근자
int getId()
Map<String, String> getFields()

// 직렬화: Record → JsonObject
JsonObject toJsonObject()

// 역직렬화: JsonObject → Record  (static factory)
static Record fromJsonObject(JsonObject obj)
```

### 직렬화 형식

```json
{
  "id": 1,
  "fields": {
    "name": "Alice",
    "email": "alice@example.com"
  }
}
```

- `id`는 JSON number로 저장 (따옴표 없음)
- `fields`는 항상 JsonObject (빈 객체 `{}` 허용)

---

## 2. `JsonFileRepository.java`

**패키지**: `com.jsoncrud.repository`

### 책임

- `data/records.json` 파일 한 곳만 읽고 쓴다.
- 파싱/직렬화는 `com.jsonparser.JsonParser`와 `JsonObject.toPrettyString()` 위임.
- 파일이 없으면 `{"records":[]}` 로 자동 초기화.

### 생성자

```java
JsonFileRepository(String filePath)
// filePath 기본값: "data/records.json"
```

- 생성 시점에 `data/` 디렉토리와 파일 존재 여부 확인 후 없으면 생성.

### 메서드

```java
List<Record> findAll()
// records.json 전체를 읽어 List<Record> 반환
// 파일이 비어있거나 records 배열이 없으면 빈 리스트 반환

void saveAll(List<Record> records)
// List<Record> 전체를 records.json에 덮어쓰기
// 내부적으로 JsonObject 트리를 직접 조립해 toPrettyString(0) 출력
```

### 파일 I/O 전략

```
findAll():
  파일 읽기 (java.nio.file.Files.readString)
    → JsonParser.parse(content).asObject()
    → root.get("records").asArray()
    → 각 element를 Record.fromJsonObject() 로 변환
    → List<Record> 반환

saveAll():
  List<Record> → 각 Record.toJsonObject()
    → JsonArray 조립
    → root JsonObject { "records": [...] }
    → toPrettyString(0)
    → Files.writeString(path, content, UTF_8)
```

---

## 3. 디렉토리 구조 (Phase 1 완료 시)

```
src/main/java/com/jsoncrud/
├── model/
│   └── Record.java
└── repository/
    └── JsonFileRepository.java

data/
└── records.json          ← 앱 최초 실행 시 자동 생성
```

---

## 4. 완료 기준

- [ ] `data/records.json` 파일이 없을 때 자동 생성되고 `{"records":[]}` 내용 확인
- [ ] `Record` 직렬화 → `saveAll` → `findAll` → 역직렬화 왕복 데이터 일치
- [ ] `id`, `fields` 값이 파일 저장 후에도 정확히 보존됨
