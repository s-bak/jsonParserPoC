@echo off
setlocal

set SRC=src\main\java\com\jsonparser
set TEST=src\test\java\com\jsonparser
set OUT=out

if exist %OUT% rmdir /s /q %OUT%
mkdir %OUT%

echo [1/2] 컴파일 중...
javac -d %OUT% ^
  %SRC%\exception\JsonParseException.java ^
  %SRC%\model\JsonValue.java ^
  %SRC%\model\JsonNull.java ^
  %SRC%\model\JsonBoolean.java ^
  %SRC%\model\JsonString.java ^
  %SRC%\model\JsonNumber.java ^
  %SRC%\model\JsonArray.java ^
  %SRC%\model\JsonObject.java ^
  %SRC%\JsonToken.java ^
  %SRC%\JsonLexer.java ^
  %SRC%\JsonParser.java ^
  %TEST%\JsonParserTest.java

if %ERRORLEVEL% neq 0 (
  echo 컴파일 실패!
  exit /b 1
)

echo [2/2] 테스트 실행 중...
java -cp %OUT% com.jsonparser.JsonParserTest

endlocal
