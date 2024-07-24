package io.github.bonigarcia.webdriver.testng.ch09.networktraffic;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

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
    static final Logger logger = getLogger(CaptureNetworkTrafficFirefoxNGTest.class);

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

    /**
     * Will create a JSON file of 1.37 MB.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testCaptureNetworkTrafficFirefox() throws IOException, InterruptedException {
        driver.get("https://bonigarcia.dev/selenium-webdriver-java/");
        assertThat(driver.getTitle()).contains("Selenium WebDriver");

        int times = 1;  // you can increase the times to make the HAR even more fat
        for (int i = 0; i < times; i++) {
            // a sequence of Chapter3-9 will result a HAR of
            //     length : 1,371,545 bytes
            //     #lines : 6,367
            navigateToChapter3(driver);
            navigateToChapter4(driver);
            navigateToChapter5(driver);
            navigateToChapter7(driver);
            navigateToChapter8(driver);
            navigateToChapter9(driver);
        }

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
        int numbLines = FlyPrettyPrinter.prettyPrint(
                Files.newInputStream(uglyHar),
                Files.newOutputStream(prettyHar));
        logger.info(String.format("Pretty HAR was written into %s", prettyHar));
        logger.info(String.format("    length : %,d bytes", prettyHar.toFile().length()));
        logger.info(String.format("    #lines : %,d", numbLines));
    }

    /**
     * visit more URLs in order to make the HAR file more fat.
     *
     * @param driver
     */
    private void navigateToChapter3(WebDriver driver) throws InterruptedException {
        // Chapter 3. WebDriver Fundamentals
        // length : 862,148 bytes
        // #lines : 2,781
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/web-form.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/navigation1.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/dropdown-menu.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/mouse-over.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/drag-and-drop.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/draw-in-canvas.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/loading-images.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/slow-calculator.html");
    }


    private void navigateToChapter4(WebDriver driver) throws InterruptedException {
        // Chapter 4. Browser-Agnostic Features
        // length : 230,927 bytes
        // #lines : 2,382
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/long-page.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/infinite-scroll.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/shadow-dom.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/cookies.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/frames.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/iframes.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/dialog-boxes.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/web-storage.html");
    }

    private void navigateToChapter5(WebDriver driver) throws InterruptedException {
        // Chapter 5. Browser-Specific Manipulation
        // length : 341,745 bytes
        // #lines : 2,150
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/geolocation.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/notifications.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/get-user-media.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/multilanguage.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/console-logs.html");
    }

    private void navigateToChapter7(WebDriver driver) throws InterruptedException {
        // Chapter 7. The Page Object Model (POM)
        // length : 186,984 bytes
        // #lines : 1,688
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/login-form.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/login-slow.html");
    }

    private void navigateToChapter8(WebDriver driver) throws InterruptedException {
        // Chapter 8. Testing Framework Specifics
        // length : 189,675 bytes
        // #lines : 1,802
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/random-calculator.html");
    }


    private void navigateToChapter9(WebDriver driver) throws InterruptedException {
        // Chapter 9. Third-Party Integrations
        // length : 196,962 bytes
        // #lines : 1,976
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/download.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/ab-testing.html");
        navigateToUrl(driver, "https://bonigarcia.dev/selenium-webdriver-java/data-types.html");
    }




    private void navigateToUrl(WebDriver driver, String URL) throws InterruptedException {
        driver.get(URL);
        assertThat(driver.getTitle()).contains("Selenium WebDriver");
        Thread.sleep(1500);
    }
}
