# JSON Parser + CRUD Console Application

자체 구현 JSON 파싱 라이브러리를 기반으로, JSON 파일을 저장소로 사용하는 콘솔 CRUD 애플리케이션입니다.
순수 Java SE만 사용하며 외부 라이브러리 의존성이 없습니다.

---

## 아키텍처

두 계층이 명확히 분리되어 있습니다. CRUD 앱은 라이브러리를 호출하지만, 라이브러리는 앱을 전혀 알지 못합니다.

```
[JSON Parser Library]            [CRUD Console App]
com.jsonparser                   com.jsoncrud
├── JsonLexer                    ├── Main
├── JsonParser                   ├── menu/
├── JsonToken                    │   └── MenuHandler
├── exception/                   ├── service/
│   └── JsonParseException       │   └── RecordService
└── model/                       ├── repository/
    ├── JsonValue (abstract)     │   └── JsonFileRepository
    ├── JsonObject               └── model/
    ├── JsonArray                    └── Record
    ├── JsonString
    ├── JsonNumber
    ├── JsonBoolean
    └── JsonNull
```

### 레이어별 역할

| 레이어 | 클래스 | 역할 |
|--------|--------|------|
| Parser | `JsonLexer` → `JsonParser` | 문자열을 토큰화하고 JsonValue 트리로 변환 |
| Model | `JsonObject`, `JsonArray`, … | 파싱 결과를 타입 안전하게 표현 |
| Repository | `JsonFileRepository` | `data/records.json` 읽기·쓰기 |
| Service | `RecordService` | CRUD 비즈니스 로직, id 자동 증가 |
| UI | `MenuHandler` | 대화형 콘솔 메뉴, 입력 수집 및 결과 출력 |

---

## 빌드 & 실행

**요구 사항**: JDK 11 이상

프로젝트 루트에서 아래 명령을 실행합니다.

```bat
compile_crud.bat
```

스크립트가 전체 소스를 컴파일한 뒤 앱을 바로 실행합니다.
`data/records.json` 파일이 없으면 앱 실행 시 자동으로 생성됩니다.

---

## CRUD 사용 예시

### Create — 새 레코드 추가

```
========================================
        JSON CRUD Manager
========================================
 1. Create  — 새 레코드 추가
 ...
선택: 1

[새 레코드 추가]
필드명 입력 (완료 시 빈 줄): name
값 입력: Alice
필드명 입력 (완료 시 빈 줄): email
값 입력: alice@example.com
필드명 입력 (완료 시 빈 줄):

저장 완료. 부여된 ID: 1
```

### Read — 전체 목록 조회

```
선택: 2

[목록 보기 / 검색]
 1) 전체 목록
 2) ID 검색
 3) 필드 검색
선택: 1

----------------------------------------
 ID │ 필드
----------------------------------------
  1 │ name=Alice, email=alice@example.com
  2 │ name=Bob, email=bob@example.com
----------------------------------------
총 2건
```

### Update — 레코드 수정

```
선택: 3

[레코드 수정]
수정할 ID: 1

----------------------------------------
 ID: 1
 name  : Alice
 email : alice@example.com
----------------------------------------

수정할 필드명: email
새로운 값: alice@new.com

수정 완료.
----------------------------------------
 ID: 1
 name  : Alice
 email : alice@new.com
----------------------------------------
```

### Delete — 레코드 삭제

```
선택: 4

[레코드 삭제]
삭제할 ID: 2

----------------------------------------
 ID: 2
 name  : Bob
 email : bob@example.com
----------------------------------------

정말 삭제하시겠습니까? (Y/N): Y
ID 2 이(가) 삭제되었습니다.
```

---

## 데이터 파일 형식

`data/records.json`에 아래 형식으로 저장됩니다.

```json
{
  "records": [
    {
      "id": 1,
      "fields": {
        "name": "Alice",
        "email": "alice@new.com"
      }
    },
    {
      "id": 3,
      "fields": {
        "name": "Carol",
        "city": "Seoul"
      }
    }
  ]
}
```

- `id`: 자동 증가 정수 (삭제 후에도 재사용하지 않음)
- `fields`: 사용자가 자유롭게 정의하는 키-값 쌍

---

## 프로젝트 구조

```
jsonParser/
├── compile_crud.bat          ← CRUD 앱 빌드 & 실행
├── compile_and_run.bat       ← JSON 라이브러리 테스트
├── CLAUDE.md                 ← 요구사항 및 제약 명세
├── docs/
│   ├── PLAN.md               ← 전체 개발 플랜
│   └── design/
│       ├── phase1.md         ← 도메인 모델 & Repository 설계
│       ├── phase2.md         ← Service 계층 설계
│       ├── phase3.md         ← 콘솔 UI 설계
│       ├── phase4.md         ← 빌드 스크립트 & 통합 검증 설계
│       └── phase5.md         ← GitHub Push & 문서화 설계
├── src/main/java/
│   ├── com/jsonparser/       ← JSON 파싱 라이브러리 (변경 금지)
│   │   ├── JsonLexer.java
│   │   ├── JsonParser.java
│   │   ├── JsonToken.java
│   │   ├── exception/
│   │   └── model/
│   └── com/jsoncrud/         ← CRUD 콘솔 앱
│       ├── Main.java
│       ├── menu/
│       ├── service/
│       ├── repository/
│       └── model/
├── src/test/java/
│   └── com/jsonparser/
│       └── JsonParserTest.java
└── data/
    └── records.json          ← 앱 실행 시 자동 생성
```
