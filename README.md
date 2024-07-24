# JsonFlyweight

`com.kazurayam.jsonflyweight.JsonFlyweight` is a Java class with a static method `prettyPrint`.

- [`com.kazurayam.jsonflyweight.JsonFlyweight`](https://github.com/kazurayam/JsonFlyweight/blob/develop/lib/src/main/java/com/kazurayam/jsonflyweight/JsonFlyweight.java)

The `prettyPrint` method does pretty-print a JSON. The method has the following characteristics.

1. It works very fast. It is as fast as Gson and Jackson Databind.
2. It requires minimum memory runtime. It creates an internal buffer of 64 Kilobyte and no more.

There are many Pretty Printers for JSON in the world. Gson, Jackson Databind, Groovy's JsonOutput, and more. All of them are fine to process a small JSON. However, if you want to pretty-print a large JSON, you may encounter problems. Some of them are too slow. Some of them requires too much memory.

The `com.kazurayam.jsonflyweight.JsonFlyweight` class is designed to process a very large JSON file.

You can use it as follows:

```
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
```


