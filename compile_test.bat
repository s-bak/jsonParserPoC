@echo off
setlocal

set MSRC=src\main\java\com
set TSRC=src\test\java
set OUT=out_test

if exist %OUT% rmdir /s /q %OUT%
mkdir %OUT%

echo [1/3] 프로덕션 코드 컴파일 중...
javac -encoding UTF-8 -d %OUT% ^
  %MSRC%\jsonparser\exception\JsonParseException.java ^
  %MSRC%\jsonparser\model\JsonValue.java ^
  %MSRC%\jsonparser\model\JsonNull.java ^
  %MSRC%\jsonparser\model\JsonBoolean.java ^
  %MSRC%\jsonparser\model\JsonString.java ^
  %MSRC%\jsonparser\model\JsonNumber.java ^
  %MSRC%\jsonparser\model\JsonArray.java ^
  %MSRC%\jsonparser\model\JsonObject.java ^
  %MSRC%\jsonparser\JsonToken.java ^
  %MSRC%\jsonparser\JsonLexer.java ^
  %MSRC%\jsonparser\JsonParser.java ^
  %MSRC%\jsoncrud\model\Record.java ^
  %MSRC%\jsoncrud\repository\JsonFileRepository.java ^
  %MSRC%\jsoncrud\service\RecordService.java ^
  %MSRC%\jsoncrud\menu\MenuHandler.java ^
  %MSRC%\jsoncrud\Main.java

if %ERRORLEVEL% neq 0 (
  echo.
  echo 프로덕션 코드 컴파일 실패.
  exit /b 1
)
echo 프로덕션 코드 컴파일 성공.
echo.

echo [2/3] 테스트 코드 컴파일 중...
javac -encoding UTF-8 -cp %OUT% -d %OUT% ^
  %TSRC%\com\jsonparser\JsonTokenTest.java ^
  %TSRC%\com\jsonparser\JsonLexerTest.java ^
  %TSRC%\com\jsonparser\model\JsonModelTest.java ^
  %TSRC%\com\jsonparser\JsonParserTest.java ^
  %TSRC%\com\jsoncrud\model\RecordTest.java ^
  %TSRC%\com\jsoncrud\repository\JsonFileRepositoryTest.java ^
  %TSRC%\com\jsoncrud\service\RecordServiceTest.java ^
  %TSRC%\com\jsoncrud\menu\MenuHandlerTest.java ^
  %TSRC%\RunAllTests.java

if %ERRORLEVEL% neq 0 (
  echo.
  echo 테스트 코드 컴파일 실패.
  exit /b 1
)
echo 테스트 코드 컴파일 성공.
echo.

echo [3/3] 전체 테스트 실행 중...
echo.
java -cp %OUT% RunAllTests

set EXIT=%ERRORLEVEL%
endlocal
exit /b %EXIT%
