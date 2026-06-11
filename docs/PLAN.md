# 개발 플랜 — JSON CRUD Console Application

## 목표

`com.jsonparser` 라이브러리(변경 금지)를 직접 활용해
JSON 파일 기반 CRUD 콘솔 애플리케이션을 단계적으로 구현한다.

---

## Phase 1 — 도메인 모델 & Repository 계층

**목적**: 데이터 구조와 파일 I/O를 정의한다.

### 산출물

| 파일 | 역할 |
|------|------|
| `com/jsoncrud/model/Record.java` | id, fields(Map) 보유 |
| `com/jsoncrud/repository/JsonFileRepository.java` | JSON 파일 읽기·쓰기 전담 |

### 세부 작업

1. **`Record`** 클래스
   - 필드: `int id`, `Map<String, String> fields`
   - `JsonObject`로 직렬화 / `JsonObject`에서 역직렬화하는 메서드 포함

2. **`JsonFileRepository`**
   - `data/records.json` 파일이 없으면 `{"records":[]}` 로 자동 생성
   - `List<Record> findAll()` — 전체 레코드 로드
   - `void saveAll(List<Record> records)` — 전체 레코드를 파일에 덮어쓰기
   - 내부적으로 `JsonParser.parse()` 로 읽고, `JsonObject.toPrettyString()` 으로 쓴다

### 완료 기준

- `data/records.json` 파일 자동 생성 확인
- 빈 목록 직렬화 → 역직렬화 왕복 테스트 통과

---

## Phase 2 — Service 계층 (CRUD 비즈니스 로직)

**목적**: Repository를 감싸 CRUD 연산 단위로 추상화한다.

### 산출물

| 파일 | 역할 |
|------|------|
| `com/jsoncrud/service/RecordService.java` | CRUD 메서드 구현 |

### 세부 작업

1. **Create**
   - `Record create(Map<String, String> fields)`
   - id: 현재 최대 id + 1 (정수 자동 증가)

2. **Read**
   - `List<Record> findAll()`
   - `Optional<Record> findById(String id)`
   - `List<Record> findByField(String key, String value)` — 대소문자 무시 부분 일치

3. **Update**
   - `Record update(int id, String fieldKey, String newValue)`
   - 대상 ID 없으면 `IllegalArgumentException` 발생
   - `fieldKey`가 레코드에 존재하지 않으면 `IllegalArgumentException` 발생

4. **Delete**
   - `Record delete(int id)` — 삭제된 레코드 반환 (확인 출력용)
   - 대상 없으면 `IllegalArgumentException` 발생

### 완료 기준

- 각 메서드 단위 시나리오 수동 검증 (콘솔 출력으로 확인)

---

## Phase 3 — 콘솔 UI (Menu & Main)

**목적**: 사용자와 상호작용하는 메뉴 루프를 구현한다.

### 산출물

| 파일 | 역할 |
|------|------|
| `com/jsoncrud/menu/MenuHandler.java` | 메뉴 출력, 입력 수집, Service 호출 |
| `com/jsoncrud/Main.java` | 진입점, 의존성 조립, 메인 루프 |

### 세부 작업

1. **메인 메뉴**
   ```
   ========== JSON CRUD Manager ==========
   1. Create  — 새 레코드 추가
   2. Read    — 목록 보기 / 검색
   3. Update  — 레코드 수정
   4. Delete  — 레코드 삭제
   0. Exit
   =======================================
   ```

2. **Create 흐름**
   - 필드명 입력 → 값 입력 → 반복 (빈 입력 시 종료)
   - 저장 완료 메시지 + 부여된 ID 출력

3. **Read 흐름**
   - 서브메뉴: `1) 전체 목록` / `2) ID 검색` / `3) 필드 검색`
   - 전체 목록: id, fields 요약을 테이블로 출력
   - 검색 결과 없으면 안내 메시지 출력

4. **Update 흐름**
   - ID 입력 → 현재 레코드 출력 → 수정할 필드명 입력
   - 필드명이 존재하지 않으면 즉시 안내 메시지 출력 후 메뉴 복귀
   - 필드명이 존재하면 새 값 입력 → 저장

5. **Delete 흐름**
   - ID 입력 → 레코드 내용 출력 → `정말 삭제하시겠습니까? (Y/N)` → Y면 삭제

6. **공통 UX**
   - 잘못된 입력 시 재입력 요청 (루프 종료 없음)
   - 모든 작업 후 메인 메뉴로 복귀

### 완료 기준

- 메인 메뉴 → 각 기능 → 메인 복귀 흐름 정상 동작
- 잘못된 ID, 빈 필드 등 예외 상황에서 앱이 종료되지 않음

---

## Phase 4 — 빌드 스크립트 & 통합 검증

**목적**: 단일 명령으로 빌드·실행이 가능하게 하고, 전체 시나리오를 검증한다.

### 산출물

| 파일 | 역할 |
|------|------|
| `compile_crud.bat` | CRUD 앱 컴파일 & 실행 스크립트 |

### 세부 작업

1. **`compile_crud.bat`** 작성
   - 라이브러리 소스 + CRUD 소스를 함께 컴파일
   - `java -cp out com.jsoncrud.Main` 으로 실행

2. **통합 시나리오 검증**
   - 레코드 3건 생성 → 전체 목록 확인
   - ID 검색 / 필드 검색 각 1회
   - 1건 수정 → 목록에서 변경 확인
   - 1건 삭제 (Y 확인) → 목록에서 제거 확인
   - 존재하지 않는 ID로 Update/Delete 시도 → 오류 메시지 확인

3. **`data/records.json`** 최종 상태 육안 확인

### 완료 기준

- 모든 통합 시나리오 오류 없이 통과
- `data/records.json` 이 유효한 JSON 형식 유지

---

## Phase 5 — GitHub Push & 문서화

**목적**: 완성된 코드를 원격 저장소에 반영하고 README를 작성한다.

### 세부 작업

1. **`README.md`** 작성
   - 프로젝트 소개, 구조 다이어그램, 빌드·실행 방법, 사용 예시 스크린샷(텍스트)

2. **커밋 & Push**
   - Phase별 커밋 단위로 정리 후 `git push origin master`

3. **GitHub 확인**
   - Repository 파일 구조, README 렌더링 확인

### 완료 기준

- `https://github.com/s-bak/jsonParserPoC` 에 전체 코드 반영
- README가 프로젝트를 충분히 설명

---

## 일정 요약

| Phase | 내용 | 의존성 |
|-------|------|--------|
| Phase 1 | 도메인 모델 & Repository | 없음 |
| Phase 2 | Service 계층 | Phase 1 완료 후 |
| Phase 3 | 콘솔 UI | Phase 2 완료 후 |
| Phase 4 | 빌드 스크립트 & 통합 검증 | Phase 3 완료 후 |
| Phase 5 | GitHub Push & 문서화 | Phase 4 완료 후 |
