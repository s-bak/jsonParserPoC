@echo off
setlocal

set SRC=src\main\java\com
set OUT=out

if exist %OUT% rmdir /s /q %OUT%
mkdir %OUT%

echo [1/2] 컴파일 중...
javac -encoding UTF-8 -d %OUT% ^
  %SRC%\jsonparser\exception\JsonParseException.java ^
  %SRC%\jsonparser\model\JsonValue.java ^
  %SRC%\jsonparser\model\JsonNull.java ^
  %SRC%\jsonparser\model\JsonBoolean.java ^
  %SRC%\jsonparser\model\JsonString.java ^
  %SRC%\jsonparser\model\JsonNumber.java ^
  %SRC%\jsonparser\model\JsonArray.java ^
  %SRC%\jsonparser\model\JsonObject.java ^
  %SRC%\jsonparser\JsonToken.java ^
  %SRC%\jsonparser\JsonLexer.java ^
  %SRC%\jsonparser\JsonParser.java ^
  %SRC%\jsoncrud\model\Record.java ^
  %SRC%\jsoncrud\repository\JsonFileRepository.java ^
  %SRC%\jsoncrud\service\RecordService.java ^
  %SRC%\jsoncrud\menu\MenuHandler.java ^
  %SRC%\jsoncrud\Main.java

if %ERRORLEVEL% neq 0 (
  echo.
  echo 컴파일 실패. 위 오류를 확인하세요.
  exit /b 1
)

echo 컴파일 성공.
echo.
echo [2/2] 실행 중...
echo.
java -cp %OUT% com.jsoncrud.Main

endlocal
