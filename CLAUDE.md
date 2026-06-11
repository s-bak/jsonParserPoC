# JSON Parser + CRUD Console Application

## 프로젝트 개요

자체 구현 JSON 파싱 라이브러리(PoC)를 기반으로,
JSON 파일을 영속성 저장소로 사용하는 콘솔 CRUD 애플리케이션.

---

## 핵심 제약

- **라이브러리 코드 구조를 변경하지 않는다.**
  - `com.jsonparser` 패키지 하위의 `JsonLexer`, `JsonParser`, `JsonToken`,
    `model/*`, `exception/*` 파일은 기능 추가 없이 원형 유지.
  - CRUD 애플리케이션 코드는 별도 패키지(`com.jsoncrud`)에 작성한다.
- 외부 라이브러리 의존성을 추가하지 않는다 (순수 Java SE).
- 데이터 영속성은 오직 JSON 파일(`data/records.json`)로 관리한다.

---

## CRUD 기능 명세

### Create
- 사용자로부터 필드명/값 쌍을 대화형으로 입력받는다.
- 각 레코드에 고유 ID(자동 증가 정수: 현재 최대 ID + 1)를 자동 부여한다.
- 입력이 완료되면 JSON 파일에 즉시 저장한다.

### Read
- **전체 목록**: 저장된 모든 레코드를 테이블 형식으로 출력한다.
- **ID 검색**: 특정 ID로 단건 조회한다.
- **키-값 검색**: 특정 필드명과 값을 입력하면 일치하는 레코드를 모두 반환한다.

### Update
- ID를 입력받아 레코드를 특정한다.
- 수정할 필드명과 새 값을 입력받아 해당 필드만 교체한다.
- 존재하지 않는 ID 입력 시 오류 메시지를 출력하고 메뉴로 복귀한다.

### Delete
- ID를 입력받아 레코드를 특정한다.
- 삭제 전 레코드 내용을 출력하고 사용자에게 확인(`Y/N`)을 받는다.
- 확인 후에만 실제 삭제를 수행한다.
- 존재하지 않는 ID 입력 시 오류 메시지를 출력하고 메뉴로 복귀한다.

---

## 패키지 구조 (목표)

```
src/main/java/
├── com/jsonparser/          ← 기존 라이브러리 (변경 금지)
│   ├── JsonLexer.java
│   ├── JsonParser.java
│   ├── JsonToken.java
│   ├── exception/
│   └── model/
└── com/jsoncrud/            ← CRUD 애플리케이션 (신규)
    ├── Main.java            ← 진입점 & 메인 루프
    ├── menu/
    │   └── MenuHandler.java ← 콘솔 메뉴 렌더링
    ├── service/
    │   └── RecordService.java ← CRUD 비즈니스 로직
    ├── repository/
    │   └── JsonFileRepository.java ← 파일 I/O
    └── model/
        └── Record.java      ← 도메인 레코드 모델
```

---

## 데이터 파일 형식

```json
{
  "records": [
    {
      "id": "1",
      "fields": {
        "name": "Alice",
        "email": "alice@example.com"
      }
    }
  ]
}
```

---

## 빌드 & 실행

```bat
compile_and_run.bat          # 라이브러리 테스트
compile_crud.bat             # CRUD 앱 빌드 & 실행
```

---

## 개발 플랜 참조

- 전체 플랜: `docs/PLAN.md`
- Phase별 상세 설계:
  - Phase 1 — 도메인 모델 & Repository: `docs/design/phase1.md`
  - Phase 2 — Service 계층: `docs/design/phase2.md`
  - Phase 3 — 콘솔 UI: `docs/design/phase3.md`
  - Phase 4 — 빌드 & 통합 검증: `docs/design/phase4.md`
  - Phase 5 — GitHub Push & 문서화: `docs/design/phase5.md`

## 테스트 플랜 참조

- 전체 테스트 계획 및 케이스 명세: `docs/PLAN_TEST.md`
- 100% 커버리지 목표, 외부 프레임워크 없이 순수 Java SE로 작성
- 테스트 실행: `compile_test.bat` → `RunAllTests`
