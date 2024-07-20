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

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFlyweightPrettyPrinterTest {
    private static final Logger logger = LoggerFactory.getLogger(JsonFlyweightPrettyPrinterTest.class);

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(JsonFlyweightPrettyPrinterTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(JsonFlyweightPrettyPrinterTest.class).build();

    private Path sampleHAR;
    private Path storeJson;
    private Path commaJson;

    private static ObjectMapper mapper =
            new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);


    @BeforeClass
    public void beforeClass() {
        Path fixtures = too.getProjectDirectory().resolve("src/test/fixtures");
        sampleHAR = fixtures.resolve("sample.har");
        storeJson = fixtures.resolve("store.json");
        commaJson = fixtures.resolve("comma_in_escaped_quotes.json");
    }

    /**
     * test pretty-printing a small ugly JSON
     */
    @Test
    public void test_pp_small_json() throws IOException {
        String ugly = Files.readString(storeJson);
        StringReader sr = new StringReader(ugly);
        StringWriter sw = new StringWriter();
        JsonFlyweightPrettyPrinter.prettyPrint(sr,  sw);
        assertThat(sw.toString()).isNotEmpty();
        logger.debug(sw.toString());
        assertThat(isValid(sw.toString())).isTrue();
    }

    /**
     * test pretty-printing a large JSON
     */
    @Test
    public void test_pp_HAR() throws IOException {
        InputStream is = Files.newInputStream(sampleHAR);
        Path dir = too.cleanMethodOutputDirectory("test_pp_HAR");
        Path out = dir.resolve("out.json");
        OutputStream os = Files.newOutputStream(out);
        JsonFlyweightPrettyPrinter.prettyPrint(is, os);
        //
        assertThat(out).exists();
        assertThat(out.toFile().length()).isGreaterThan(0);
        assertThat(isValid(Files.readString(out))).isTrue();
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
        JsonFlyweightPrettyPrinter.prettyPrint(is, os);
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
