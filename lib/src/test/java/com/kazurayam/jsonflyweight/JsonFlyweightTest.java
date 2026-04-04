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

public class JsonFlyweightTest {
    private static final Logger logger = LoggerFactory.getLogger(JsonFlyweightTest.class);

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(JsonFlyweightTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(JsonFlyweightTest.class).build();

    private Path prettyHAR;
    private Path storeJson;
    private Path commaJson;

    private static final ObjectMapper mapper =
            new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);


    @BeforeClass
    public void beforeClass() {
        Path fixtures = too.getProjectDirectory().resolve("src/test/fixtures");
        storeJson = fixtures.resolve("store.json");
        commaJson = fixtures.resolve("comma_in_escaped_quotes.json");
        prettyHAR = fixtures.resolve("pretty.har");
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
        InputStream is = Files.newInputStream(prettyHAR);
        Path dir = too.cleanMethodOutputDirectory("test_pp_large_HAR");
        Path out = dir.resolve("out.json");
        OutputStream os = Files.newOutputStream(out);
        int numLines = JsonFlyweight.prettyPrint(is, os);
        assertThat(out).exists();
        assertThat(out.toFile().length()).isGreaterThan(11 * 1000 * 1000);
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

    @Test
    public void test_sanitizeNonPrintableChar() {
        assertThat(JsonFlyweight.sanitizeNonPrintableChar('A')).isEqualTo('A');
        assertThat(JsonFlyweight.sanitizeNonPrintableChar(Character.toChars(65)[0])).isEqualTo('A');
        assertThat(JsonFlyweight.sanitizeNonPrintableChar('0')).isEqualTo('0');
        assertThat(JsonFlyweight.sanitizeNonPrintableChar(Character.toChars(48)[0])).isEqualTo('0');
        // non printable character is replaced to a SPACE
        assertThat(JsonFlyweight.sanitizeNonPrintableChar(Character.toChars(1)[0])).isEqualTo('?');
        assertThat(JsonFlyweight.sanitizeNonPrintableChar(Character.toChars(31)[0])).isEqualTo('?');
        assertThat(JsonFlyweight.sanitizeNonPrintableChar(Character.toChars(127)[0])).isEqualTo('?');
    }

    @Test
    public void test_prettyPrint_json_with_non_printable_character() throws IOException {
        String sb = "{\"key\":\"value" +
                Character.toChars(31)[0] +
                "value\"}";
        StringReader sr = new StringReader(sb);
        StringWriter sw = new StringWriter();
        JsonFlyweight.prettyPrint(sr, sw);
        assertThat(sw.toString()).contains("\"value?value\"");
    }

    @Test
    public void test_prettyPrinting_an_already_pretty_json() throws IOException {
        String str = "{\n  \"key\": \"value\"\n}";
        StringReader sr = new StringReader(str);
        StringWriter sw = new StringWriter();
        JsonFlyweight.prettyPrint(sr, sw);
        assertThat(sw.toString()).contains(str);  // result the same
    }
}
