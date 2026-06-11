# Phase 2 설계 — Service 계층 (CRUD 비즈니스 로직)

## 목적

`JsonFileRepository`를 감싸 CRUD 연산 단위로 추상화한다.
UI 계층(Phase 3)은 `RecordService`만 호출하고, Repository나 파일 경로를 직접 참조하지 않는다.

---

## 1. `RecordService.java`

**패키지**: `com.jsoncrud.service`

### 생성자

```java
RecordService(JsonFileRepository repository)
```

- `repository`는 외부에서 주입받는다 (`Main`에서 조립).

---

### 메서드 명세

#### 1-1. Create

```java
Record create(Map<String, String> fields)
```

| 항목 | 내용 |
|------|------|
| id 생성 | `findAll()`로 전체 로드 후 `max(id) + 1`. 레코드가 없으면 `1`부터 시작. |
| 저장 | 새 `Record`를 리스트 끝에 추가 후 `saveAll()` 호출. |
| 반환 | 저장된 `Record` (id 포함) |
| 예외 | `fields`가 null이거나 비어있으면 `IllegalArgumentException` |

#### 1-2. Read — 전체 조회

```java
List<Record> findAll()
```

- `repository.findAll()` 결과를 그대로 반환.
- 레코드가 없으면 빈 리스트 반환 (예외 없음).

#### 1-3. Read — ID 단건 조회

```java
Optional<Record> findById(int id)
```

- `findAll()` 순회 후 일치하는 첫 번째 레코드 반환.
- 없으면 `Optional.empty()`.

#### 1-4. Read — 필드 검색

```java
List<Record> findByField(String key, String value)
```

| 항목 | 내용 |
|------|------|
| 매칭 방식 | 대소문자 무시, 부분 일치 (`contains`) |
| 검색 대상 | `fields` 맵에서 해당 `key`의 값 |
| 결과 | 일치하는 레코드 전체 리스트 (없으면 빈 리스트) |
| 예외 | `key`가 null/blank면 `IllegalArgumentException` |

#### 1-5. Update

```java
Record update(int id, String fieldKey, String newValue)
```

| 항목 | 내용 |
|------|------|
| 동작 | 해당 id 레코드의 `fields`에서 `fieldKey` 값을 `newValue`로 교체. |
| 저장 | 수정된 전체 리스트를 `saveAll()` 호출. |
| 반환 | 수정 후 `Record` |
| 예외 | id가 존재하지 않으면 `IllegalArgumentException("ID {id} 를 찾을 수 없습니다.")` |
| 예외 | `fieldKey`가 레코드에 없으면 `IllegalArgumentException("필드 '{fieldKey}' 가 존재하지 않습니다.")` |

> **구현 노트**: `Record`는 불변이므로 수정 시 기존 fields를 복사한 뒤 변경 후 새 `Record` 인스턴스로 리스트를 교체한다.

#### 1-6. Delete

```java
Record delete(int id)
```

| 항목 | 내용 |
|------|------|
| 동작 | 해당 id 레코드를 리스트에서 제거 후 `saveAll()` 호출. |
| 반환 | 삭제된 `Record` (UI에서 삭제 확인 출력용) |
| 예외 | id가 존재하지 않으면 `IllegalArgumentException("ID {id} 를 찾을 수 없습니다.")` |

---

## 2. 파일 I/O 호출 흐름

```
create / update / delete
  └─ findAll()           (read-modify-write 패턴)
       ↓
  리스트 변경
       ↓
  saveAll()              (전체 덮어쓰기)

findAll / findById / findByField
  └─ findAll()           (읽기 전용)
```

> 동시성 제어는 이번 PoC 범위 밖이므로 고려하지 않는다.

---

## 3. 디렉토리 구조 (Phase 2 완료 시)

```
src/main/java/com/jsoncrud/
├── model/
│   └── Record.java               (Phase 1)
├── repository/
│   └── JsonFileRepository.java   (Phase 1)
└── service/
    └── RecordService.java        (Phase 2 신규)
```

---

## 4. 완료 기준

- [ ] `create` 호출 시 id가 1부터 순서대로 증가
- [ ] `findAll` 빈 파일에서 빈 리스트 반환
- [ ] `findById` 존재/비존재 양쪽 동작 확인
- [ ] `findByField` 대소문자 무시 부분 일치 동작 확인
- [ ] `update` 기존 필드 수정 동작 확인
- [ ] `update` 존재하지 않는 `fieldKey` 입력 시 `IllegalArgumentException` 발생 확인
- [ ] `update` / `delete` 없는 id 입력 시 `IllegalArgumentException` 발생 확인
- [ ] `delete` 후 파일에서 해당 레코드 제거 확인
