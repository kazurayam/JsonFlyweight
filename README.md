# JsonFlyweight

`com.kazurayam.jsonflyweight.JsonFlyweight` is a Java class with a static method `prettyPrint`.

- [`com.kazurayam.jsonflyweight.JsonFlyweight`]()

The `prettyPrint` method does pretty-print a JSON. The method has the following characteristics.

1. It works very fast. It is as fast as Gson and Jackson Databind.
2. It requires minimum memory runtime. It creates an internal buffer of 64 Kilobyte and no more.

There are many Pretty Printers for JSON in the world. Gson, Jackson Databind, Groovy's JsonOutput, and more. All of them are fine to process a small JSON. However, if you want to pretty-print a large JSON, you may encounter problems. Some of them are too slow. Some of them requires too much memory.

The `com.kazurayam.jsonflyweight.JsonFlyweight` class is designed to process a very large JSON file.

You can use it as follows:

```

```


