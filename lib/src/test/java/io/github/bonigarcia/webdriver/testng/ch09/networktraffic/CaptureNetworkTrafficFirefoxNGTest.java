package io.github.bonigarcia.webdriver.testng.ch09.networktraffic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.kazurayam.jsonflyweight.FlyPrettyPrinter;
import com.kazurayam.unittest.TestOutputOrganizer;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import io.github.bonigarcia.wdm.WebDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;

/**
 * This class performs automated web-UI test using
 * - Selenium Java
 * - FirefoxDriver
 * - WebDriverManager, Boni Garcia
 * - BrowserMob Proxy, net.lightbody.bmp
 *
 * This code is meant to make a large JSON-formatted text file.
 * BrowserMob Proxy will privide us a HAR file.
 *
 * The original code is avaiable at
 * https://github.com/bonigarcia/selenium-webdriver-java/blob/master/selenium-webdriver-testng/src/test/java/io/github/bonigarcia/webdriver/testng/ch09/network_traffic/CaptureNetworkTrafficFirefoxNGTest.java
 */
public class CaptureNetworkTrafficFirefoxNGTest {
    static final Logger log = getLogger(CaptureNetworkTrafficFirefoxNGTest.class);

    static final TestOutputOrganizer too =
            new TestOutputOrganizer.Builder(CaptureNetworkTrafficFirefoxNGTest.class)
                    .outputDirectoryRelativeToProject("build/tmp/testOutput")
                    .subOutputDirectory(CaptureNetworkTrafficFirefoxNGTest.class).build();
    WebDriver driver;

    BrowserMobProxy proxy;

    @BeforeTest
    public void beforeTest() throws IOException {
        too.cleanClassOutputDirectory();
    }

    @BeforeMethod
    public void setup() {
        proxy = new BrowserMobProxyServer();
        proxy.start();
        proxy.newHar();
        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT,
                CaptureType.RESPONSE_CONTENT);

        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
        FirefoxOptions options = new FirefoxOptions();
        options.setProxy(seleniumProxy);
        options.setAcceptInsecureCerts(true);

        driver = WebDriverManager.firefoxdriver().capabilities(options)
                .create();
    }

    @AfterMethod
    public void teardown() {
        proxy.stop();
        driver.quit();
    }

    @Test
    public void testCaptureNetworkTrafficFirefox() throws IOException {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        assertThat(driver.getTitle()).contains("Selenium WebDriver");

        /*
        List<HarEntry> logEntries = proxy.getHar().getLog().getEntries();
        logEntries.forEach(logEntry -> {
            log.debug("Request: {} - Response: {}",
                    logEntry.getRequest().getUrl(),
                    logEntry.getResponse().getStatus());
        });
         */

        // write the original HAR created by BrowserMob Proxy into a local file
        Path classOutputDir = too.cleanClassOutputDirectory();
        Path uglyHar = classOutputDir.resolve("sample.har");
        Har har = proxy.getHar();
        har.writeTo(Files.newOutputStream(uglyHar));
        assertThat(uglyHar).exists();
        // the HAR file should be greater than 100K
        assertThat(uglyHar.toFile().length()).isGreaterThan(100 * 1000);

        // pretty-print the original HAR, write a pretty HAR into another file
        Path prettyHar = classOutputDir.resolve("sample.pp.har");
        FlyPrettyPrinter.prettyPrint(
                Files.newInputStream(uglyHar),
                Files.newOutputStream(prettyHar));
    }
}
