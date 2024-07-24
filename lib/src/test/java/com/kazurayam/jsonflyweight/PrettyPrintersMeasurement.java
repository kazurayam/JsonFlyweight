package com.kazurayam.jsonflyweight;

import com.kazurayam.timekeeper.ReportOptions;
import com.kazurayam.timekeeper.Timekeeper;
import com.kazurayam.timekeeper.Measurement;
import com.kazurayam.timekeeper.Table;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test examines 4 pretty-printers :
 * - Gson
 * - Jackson Databind
 * - FlyPrettyPrinter
 * - Groovy's JsonOutput
 *
 * This test measures how long each pretty-printers take to finish their job.
 */
public class PrettyPrintersMeasurement {

    private static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(FlyPrettyPrinterTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(FlyPrettyPrinterTest.class).build();

    private Path getFixtureHAR() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path dataSourceDir = userHome.resolve("katalon-workspace")
                .resolve("BrowserMobProxyInKatalonStudio").resolve("work");
        Path har = dataSourceDir.resolve("TS1_demoaut.katalon.com.har");
        //Path har = dataSourceDir.resolve("TS3_process_large_HAR.har");
        return har;
    }

    private Timekeeper tk;

    @BeforeClass
    public void beforeTest() {

    }

    @AfterClass
    public void afterClass() throws IOException {
        Path outDir = too.cleanClassOutputDirectory();
        Path report = outDir.resolve("PrettyPrintersMeasurement.md");
        tk.report(report, ReportOptions.DEFAULT);
    }
}
