# Phase 4 설계 — 빌드 스크립트 & 통합 검증

## 목적

단일 배치 명령으로 전체 소스를 컴파일하고 앱을 실행할 수 있게 한다.
이후 사전 정의된 통합 시나리오를 직접 실행하여 모든 CRUD 흐름이 정상 동작함을 확인한다.

---

## 1. `compile_crud.bat`

**위치**: 프로젝트 루트

### 동작 순서

```
1. out\ 디렉토리 초기화 (기존 .class 파일 제거)
2. 라이브러리 소스(com.jsonparser) + CRUD 소스(com.jsoncrud) 일괄 컴파일
3. 컴파일 성공 시 java -cp out com.jsoncrud.Main 실행
4. 컴파일 실패 시 오류 메시지 출력 후 중단
```

### 컴파일 대상 (순서 고정 — 의존성 순)

```
[라이브러리]
  exception\JsonParseException.java
  model\JsonValue.java
  model\JsonNull.java
  model\JsonBoolean.java
  model\JsonString.java
  model\JsonNumber.java
  model\JsonArray.java
  model\JsonObject.java
  JsonToken.java
  JsonLexer.java
  JsonParser.java

[CRUD 앱]
  model\Record.java
  repository\JsonFileRepository.java
  service\RecordService.java
  menu\MenuHandler.java
  Main.java
```

### 스크립트 형식

```bat
@echo off
setlocal

set SRC=src\main\java
set OUT=out

if exist %OUT% rmdir /s /q %OUT%
mkdir %OUT%

echo [1/2] 컴파일 중...
javac -encoding UTF-8 -d %OUT% ^
  [소스 목록]

if %ERRORLEVEL% neq 0 (
  echo 컴파일 실패.
  exit /b 1
)

echo [2/2] 실행 중...
java -cp %OUT% com.jsoncrud.Main

endlocal
```

- `-encoding UTF-8`: 한글 주석/문자열 인코딩 명시
- 컴파일과 실행을 `ERRORLEVEL` 체크로 분리하여 실패 시 즉시 중단

---

## 2. 통합 검증 시나리오

Java가 설치된 환경에서 `compile_crud.bat` 실행 후 아래 순서로 수동 검증한다.

### 시나리오 A — Create (3건)

| 입력 순서 | 필드명 | 값 |
|-----------|--------|----|
| 레코드 1 | name / email | Alice / alice@example.com |
| 레코드 2 | name / email | Bob / bob@example.com |
| 레코드 3 | name / city  | Carol / Seoul |

**확인 항목**
- 각 레코드에 ID 1, 2, 3 순서 부여
- `data/records.json` 파일에 3건 저장 확인

### 시나리오 B — Read 전체 목록

**확인 항목**
- 테이블에 3건 출력
- ID 오름차순 정렬 유지

### 시나리오 C — Read ID 검색

- ID `2` 조회 → Bob 레코드 단건 출력 확인
- ID `99` 조회 → `"ID 99 를 찾을 수 없습니다."` 출력 확인

### 시나리오 D — Read 필드 검색

- 필드명 `name`, 값 `li` 검색 → Alice 레코드 반환 확인 (대소문자 무시 부분 일치)
- 필드명 `city`, 값 `tokyo` 검색 → `"검색 결과가 없습니다."` 출력 확인

### 시나리오 E — Update

- ID `1` 선택 → `email` 필드를 `alice@new.com` 으로 수정
- **확인 항목**: 수정 후 출력에서 변경값 반영, `data/records.json` 파일 갱신 확인
- ID `99` 수정 시도 → `"ID 99 를 찾을 수 없습니다."` 출력 확인

### 시나리오 F — Delete

- ID `2` 삭제 → Y 확인 → `"ID 2 이(가) 삭제되었습니다."` 출력 확인
- 전체 목록 재조회 → 2건만 남음 확인
- ID `2` 재삭제 시도 → `"ID 2 를 찾을 수 없습니다."` 출력 확인
- ID `1` 삭제 → N 입력 → `"삭제를 취소하였습니다."` 출력 확인

### 시나리오 G — 예외 입력

| 입력 상황 | 기대 출력 |
|-----------|-----------|
| 메인 메뉴에서 `abc` 입력 | `"잘못된 입력입니다."` |
| Update ID 입력란에 `abc` | `"숫자를 입력해 주세요."` |
| Delete ID 입력란에 `abc` | `"숫자를 입력해 주세요."` |
| Create 필드 하나도 없이 빈 줄 | `"필드를 하나 이상 입력해야 합니다."` |

**모든 케이스에서 앱이 종료되지 않고 메인 메뉴로 복귀해야 한다.**

---

## 3. `data/records.json` 최종 상태 확인

시나리오 완료 후 파일 내용이 유효한 JSON이고 남은 레코드 수가 일치하는지 육안 확인.

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

---

## 4. 디렉토리 구조 (Phase 4 완료 시)

```
jsonParser/
├── compile_and_run.bat     ← 라이브러리 테스트용 (기존)
├── compile_crud.bat        ← CRUD 앱 빌드 & 실행 (Phase 4 신규)
├── data/
│   └── records.json        ← 앱 최초 실행 시 자동 생성
└── src/ ...
```

---

## 5. 완료 기준

- [ ] `compile_crud.bat` 실행 시 컴파일 오류 없음
- [ ] 시나리오 A–D (Create / Read) 정상 동작
- [ ] 시나리오 E (Update) 정상 동작 및 파일 갱신 확인
- [ ] 시나리오 F (Delete) Y/N 분기 및 재삭제 오류 확인
- [ ] 시나리오 G (예외 입력) 앱 종료 없이 메뉴 복귀 확인
- [ ] `data/records.json` 최종 상태 유효한 JSON 형식
