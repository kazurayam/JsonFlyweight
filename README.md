# JsonFlyweight

`com.kazurayam.jsonflyweight.JsonFlyweight` is a Java class with a static method `prettyPrint`.

- [`com.kazurayam.jsonflyweight.JsonFlyweight`](https://github.com/kazurayam/JsonFlyweight/blob/develop/lib/src/main/java/com/kazurayam/jsonflyweight/JsonFlyweight.java)

The `prettyPrint` method does pretty-print a JSON. The method has the following characteristics.

1. It works very fast. It runs as fast as Gson and Jackson Databind.
2. It requires minimum memory runtime. It creates an internal buffer of 32 Kilobyte and no more. So it can prettify a JSON-formatted text of some megabyte or gigabyte in size without any memory issue. 

There are many Pretty Printers for JSON in the world. For example, [Gson, Jackson Databind](https://www.baeldung.com/java-json-pretty-print) and [Groovy's JsonOutput](https://www.baeldung.com/groovy-json#2-formatting-the-json-output). All of them are fine to process a small JSON. However, if you want to pretty-print a large JSON, you may encounter problems. Some of them are too slow. Some of them requires too much memory. In such situation, the `com.kazurayam.jsonflyweight.JsonFlyweight` class shines.

You can use it as follows:

```
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
    public void testPrettyPrintSmallStrings() throws IOException {
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
```

When you execute it, you will see the following output in the console.

```
...
> Task :lib:testClasses
{"store": {"book": [{"author": "Nigel Rees","title": "Sayings of the Century","price": 8.95}, {"author": "J. R. R. Tolkien","title": "The Lord of the Rings","isbn": "0-395-19395-8","price": 22.99}],"bicycle": {"color": "red","price": 399}}}
=================================================
{
  "store": {
    "book": [
      {
        "author": "Nigel Rees",
        "title": "Sayings of the Century",
        "price": 8.95
      },
      {
        "author": "J. R. R. Tolkien",
        "title": "The Lord of the Rings",
        "isbn": "0-395-19395-8",
        "price": 22.99
      }
    ],
    "bicycle": {
      "color": "red",
      "price": 399
    }
  }
}
```

The `src/test/fixtures/sample.har` files in the above code was an HTTP Archive (HAR) of 1.3 MB in size with 6000 lines contained. The `JsonFlyweight.prettyPryt(InputStrea, OutputStream)` could prettify the input in less than 1 second, without any pressure to JVM head memory.
