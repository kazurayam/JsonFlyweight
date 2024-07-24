import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kazurayam.jsonflyweight.JsonFlyweight;
import com.kazurayam.timekeeper.Measurement;
import com.kazurayam.timekeeper.ReportOptions;
import com.kazurayam.timekeeper.Table;
import com.kazurayam.timekeeper.Timekeeper;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import groovy.json.JsonOutput;

/**
 * This test examines 4 pretty-printers :
 * - Gson
 * - Jackson Databind
 * - FlyPrettyPrinter
 * - Groovy's JsonOutput
 *
 * This test measures how long each pretty-printers take to finish their job.
 */
class PerformanceMeasurement {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(PerformanceMeasurement.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(PerformanceMeasurement.class).build()

    private Path classOutputDir
    private Measurement m1

    private Path getFixtureHAR() {
        Path fixturesDir= too.getProjectDirectory().resolve("src/test/fixtures")
        Path har = fixturesDir.resolve("sample.har")
        return har
    }

    @BeforeClass
    void beforeTest() throws IOException {
        classOutputDir = too.cleanClassOutputDirectory()
        m1 = new Measurement.Builder("Pretty-Printing 1.3MB JSON",
                List.of("Case")).build()
    }

    @Test
    void testGson() {
        m1.before(Collections.singletonMap("Case", "Gson"))
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String source = Files.readString(getFixtureHAR())
        String pp = gson.toJson(source)                  // this runs fine
        Path outFile = too.cleanMethodOutputDirectory("testGson")
                .resolve("flyweight.json")
        Files.writeString(outFile, pp)
        m1.after()
    }

    @Test
    void testJackson() {
        m1.before(["Case": "Jackson Databind"])
        ObjectMapper mapper = new ObjectMapper()
        Object source = Files.readString(getFixtureHAR())
        String pp = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(source)
        Path outFile = too.cleanMethodOutputDirectory("testJackson")
                .resolve("flyweight.json")
        Files.writeString(outFile, pp)
        m1.after()
    }

    @Test
    void testJsonFlyweight() throws IOException {
        m1.before(Collections.singletonMap("Case", "JsonFlyweight"))
        InputStream is = Files.newInputStream(getFixtureHAR())
        Path outFile = too.cleanMethodOutputDirectory("testJsonFlyweight")
                .resolve("flyweight.json")
        OutputStream os = Files.newOutputStream(outFile)
        int lines = JsonFlyweight.prettyPrint(is, os)
        m1.after()
    }

    @Test
    void testGroovyJsonOutput() throws IOException {
        m1.before(Collections.singletonMap("Case", "Groovy JsonOutput"))
        String input = Files.readString(getFixtureHAR())
        String output = JsonOutput.prettyPrint(input);
        m1.after()
    }

    @AfterTest
    void afterTest() throws IOException {
        Path outDir = too.cleanClassOutputDirectory()
        Path report = outDir.resolve("PerformanceMeasurement.md")
        Timekeeper tk = new Timekeeper.Builder()
                .table(new Table.Builder(m1).build())
                .build()
        tk.report(report, ReportOptions.NODESCRIPTION_NOLEGEND)
    }
}
