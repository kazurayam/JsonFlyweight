import com.kazurayam.jsonflyweight.JsonFlyweight;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class SampleTest {

    @Test
    public void testPrettyPrint() throws IOException {
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
