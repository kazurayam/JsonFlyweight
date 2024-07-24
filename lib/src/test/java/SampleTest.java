import com.kazurayam.jsonflyweight.JsonFlyweight;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class SampleTest {

    @Test
    public void testPrettyPrintStrings() throws IOException {
        Path projectDir = Paths.get(".");
        Path input = projectDir.resolve("src/test/fixtures/store.json");
        String uglyJson = Files.readString(input);
        System.out.println(uglyJson);
        System.out.println("=================================================");
        StringReader sr = new StringReader(uglyJson);
        StringWriter sw = new StringWriter();
        // now prettify it
        int lines = JsonFlyweight.prettyPrint(sr, sw);

        System.out.println(sw.toString());
    }

    @Test
    public void testPrettyPrintStreams() throws IOException {
        Path projectDir = Paths.get(".");
        Path inputHAR = projectDir.resolve("src/test/fixtures/sample.har");
        Path prettyJson = projectDir.resolve("build/tmp/testOutput/Sample/sample.pp.json");
        Files.createDirectories(prettyJson.getParent());
        // now prettify it
        int lines = JsonFlyweight.prettyPrint(Files.newInputStream(inputHAR), Files.newOutputStream(prettyJson));

        assertThat(prettyJson).exists();
        assertThat(lines).isGreaterThan(6_000);
        assertThat(prettyJson.toFile().length()).isGreaterThan(1_300_000);
    }

}
