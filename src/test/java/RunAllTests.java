import com.jsonparser.JsonLexerTest;
import com.jsonparser.JsonParserTest;
import com.jsonparser.JsonTokenTest;
import com.jsonparser.model.JsonModelTest;
import com.jsoncrud.menu.MenuHandlerTest;
import com.jsoncrud.model.RecordTest;
import com.jsoncrud.repository.JsonFileRepositoryTest;
import com.jsoncrud.service.RecordServiceTest;

public class RunAllTests {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  JSON Parser + CRUD 전체 테스트 실행");
        System.out.println("========================================");

        run("JsonTokenTest",           () -> JsonTokenTest.main(args),           () -> new int[]{JsonTokenTest.passed,           JsonTokenTest.failed});
        run("JsonLexerTest",           () -> JsonLexerTest.main(args),           () -> new int[]{JsonLexerTest.passed,           JsonLexerTest.failed});
        run("JsonModelTest",           () -> JsonModelTest.main(args),           () -> new int[]{JsonModelTest.passed,           JsonModelTest.failed});
        run("JsonParserTest",          () -> JsonParserTest.main(args),          () -> new int[]{JsonParserTest.passed,          JsonParserTest.failed});
        run("RecordTest",              () -> RecordTest.main(args),              () -> new int[]{RecordTest.passed,              RecordTest.failed});
        run("JsonFileRepositoryTest",  () -> JsonFileRepositoryTest.main(args),  () -> new int[]{JsonFileRepositoryTest.passed,  JsonFileRepositoryTest.failed});
        run("RecordServiceTest",       () -> RecordServiceTest.main(args),       () -> new int[]{RecordServiceTest.passed,       RecordServiceTest.failed});
        run("MenuHandlerTest",         () -> MenuHandlerTest.main(args),         () -> new int[]{MenuHandlerTest.passed,         MenuHandlerTest.failed});

        System.out.println("\n========================================");
        System.out.printf("  전체 결과: %d 통과 / %d 실패%n", totalPassed, totalFailed);
        System.out.println("========================================");
        if (totalFailed > 0) System.exit(1);
    }

    private static int totalPassed = 0;
    private static int totalFailed = 0;

    @FunctionalInterface interface ThrowingRunnable { void run() throws Exception; }
    @FunctionalInterface interface CountSupplier { int[] get(); }

    static void run(String name, ThrowingRunnable test, CountSupplier counts) {
        System.out.println("\n\n──────── " + name + " ────────");
        try {
            test.run();
        } catch (Exception e) {
            System.out.println("  [예외 발생] " + e.getMessage());
        }
        int[] c = counts.get();
        totalPassed += c[0];
        totalFailed += c[1];
        System.out.printf("  소계: %d 통과 / %d 실패%n", c[0], c[1]);
    }
}
