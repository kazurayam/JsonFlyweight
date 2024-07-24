package com.kazurayam.jsonflyweight;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFlyweightTest {
    private static final Logger logger = LoggerFactory.getLogger(JsonFlyweightTest.class);

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(JsonFlyweightTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(JsonFlyweightTest.class).build();

    private Path sampleHAR;
    private Path storeJson;
    private Path commaJson;

    private static ObjectMapper mapper =
            new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);


    @BeforeClass
    public void beforeClass() {
        Path fixtures = too.getProjectDirectory().resolve("src/test/fixtures");
        storeJson = fixtures.resolve("store.json");
        commaJson = fixtures.resolve("comma_in_escaped_quotes.json");
        //
        Path userHome = Paths.get(System.getProperty("user.home"));
        sampleHAR = userHome
                .resolve("katalon-workspace/BrowserMobProxyInKatalonStudio")
                .resolve("work/sample.har");
    }

    /**
     * test pretty-printing a small ugly JSON
     */
    @Test
    public void test_pp_small_json() throws IOException {
        String ugly = Files.readString(storeJson);
        StringReader sr = new StringReader(ugly);
        StringWriter sw = new StringWriter();
        int numLines = JsonFlyweight.prettyPrint(sr,  sw);
        assertThat(sw.toString()).isNotEmpty();
        logger.debug(sw.toString());
        assertThat(numLines).isEqualTo(20);
        assertThat(isValid(sw.toString())).isTrue();
    }

    /**
     * this testcase requires a very large sample HAR file.
     * The file can be found only on the kazurayam's Mac.
     * In other environment, this will certainly fail due to a FileNotFoundException.
     *
     * @throws IOException
     */
    @Test
    public void test_pp_large_HAR() throws IOException {
        Path har180mb = locateLargeHAR();
        InputStream is = Files.newInputStream(har180mb);
        Path dir = too.cleanMethodOutputDirectory("test_pp_large_HAR");
        Path out = dir.resolve("out.json");
        OutputStream os = Files.newOutputStream(out);
        int numLines = JsonFlyweight.prettyPrint(is, os);
        assertThat(out).exists();
        assertThat(out.toFile().length()).isGreaterThan(11 * 1000 * 1000);
    }

    private Path locateLargeHAR() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path hostProject = userHome.resolve("katalon-workspace/BrowserMobProxyInKatalonStudio");
        Path harFile = hostProject.resolve("work/TS3_process_large_HAR.har");
        return harFile;
    }


    /**
     * comma character inside a pair of escaped quotes is problematic
     * e.g,
     * {"key":"<link href=\"http://hoo.bar/?300,400,700\">"}
     *                                         ^   ^
     */
    @Test
    public void test_comma_in_escaped_quotes() throws IOException {
        InputStream is = Files.newInputStream(commaJson);
        Path dir = too.cleanMethodOutputDirectory("test_comma_in_escaped_quotes");
        Path out = dir.resolve("out.json");
        OutputStream os = Files.newOutputStream(out);
        int numLines = JsonFlyweight.prettyPrint(is, os);
        //
        assertThat(out).exists();
        assertThat(out.toFile().length()).isGreaterThan(0);
        assertThat(isValid(Files.readString(out))).isTrue();
    }


    private Boolean isValid(String json) {
        try {
            mapper.readTree(json);
        } catch (JacksonException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
