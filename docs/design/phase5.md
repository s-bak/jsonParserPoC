# Phase 5 설계 — GitHub Push & 문서화

## 목적

완성된 전체 코드가 이미 `master` 브랜치에 반영되어 있으므로,
`README.md`를 작성해 저장소를 처음 방문하는 사람이 프로젝트를 바로 이해하고 실행할 수 있도록 한다.

---

## 1. `README.md` 구성

**위치**: 프로젝트 루트

### 섹션 구성

| 섹션 | 내용 |
|------|------|
| 프로젝트 소개 | 목적, 사용 기술 (순수 Java SE, 외부 의존성 없음) |
| 아키텍처 | 라이브러리 계층 / 앱 계층 패키지 구조 다이어그램 |
| 빌드 & 실행 | JDK 요구사항, `compile_crud.bat` 실행 방법 |
| CRUD 사용 예시 | 각 기능별 콘솔 입출력 텍스트 스크린샷 |
| 데이터 파일 형식 | `data/records.json` 예시 |
| 프로젝트 구조 | 전체 파일 트리 |

### 각 섹션 상세

#### 프로젝트 소개
- 자체 구현 JSON 파싱 라이브러리(`com.jsonparser`) PoC 위에
  JSON 파일 기반 CRUD 콘솔 앱(`com.jsoncrud`)을 구현한 프로젝트임을 명시
- 외부 라이브러리 없이 순수 Java SE만 사용한다는 점 강조

#### 아키텍처
두 계층을 명확히 구분해서 표시

```
[JSON Parser Library]          [CRUD Console App]
com.jsonparser                 com.jsoncrud
├── JsonLexer                  ├── Main
├── JsonParser                 ├── menu/MenuHandler
├── JsonToken                  ├── service/RecordService
├── exception/                 ├── repository/JsonFileRepository
│   └── JsonParseException     └── model/Record
└── model/
    ├── JsonValue (abstract)
    ├── JsonObject
    ├── JsonArray
    ├── JsonString
    ├── JsonNumber
    ├── JsonBoolean
    └── JsonNull
```

#### 빌드 & 실행
- 요구 사항: JDK 11 이상
- 실행 명령: 프로젝트 루트에서 `compile_crud.bat`

#### CRUD 사용 예시
설계 문서(phase3.md)의 흐름 예시를 그대로 인용해 콘솔 블록으로 표시.
Create / Read / Update / Delete 각 1개씩.

#### 데이터 파일 형식
`CLAUDE.md`의 형식 예시 인용:
```json
{
  "records": [
    {
      "id": 1,
      "fields": {
        "name": "Alice",
        "email": "alice@example.com"
      }
    }
  ]
}
```

#### 프로젝트 구조
```
jsonParser/
├── compile_crud.bat
├── compile_and_run.bat
├── docs/
│   ├── PLAN.md
│   └── design/
│       ├── phase1.md ~ phase5.md
├── src/main/java/
│   ├── com/jsonparser/   ← JSON 파싱 라이브러리
│   └── com/jsoncrud/     ← CRUD 콘솔 앱
└── data/
    └── records.json      ← 앱 실행 시 자동 생성
```

---

## 2. 커밋 & Push 계획

Phase 5에서 새로 추가되는 파일은 `README.md` 하나이므로 단일 커밋으로 처리한다.

```
커밋 메시지:
docs: README.md 작성 (프로젝트 소개, 아키텍처, 빌드·실행, 사용 예시)
```

---

## 3. 완료 기준

- [ ] `README.md` 작성 및 `master` 브랜치에 push
- [ ] GitHub 저장소(`https://github.com/s-bak/jsonParserPoC`) 방문 시 README가 정상 렌더링
- [ ] 아키텍처 다이어그램, 빌드 방법, CRUD 예시가 모두 포함되어 있음
