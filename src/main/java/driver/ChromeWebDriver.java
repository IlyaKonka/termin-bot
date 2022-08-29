package driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static utils.BotDefaults.BROWSER_TIMEOUT_SEC;
import static utils.BotDefaults.RESPONSE_TIMEOUT_SEC;

/**
 * Class starts web driver and analyses web page to find new free termin.
 */

@Slf4j
public class ChromeWebDriver {

    private WebDriver driver;
    private ChromeDriverService service;

    private String terminUrl;
    private String terminFound;
    private String noTerminFound;
    private String buttonId;
    private String port;

    @Builder
    public ChromeWebDriver(String port, String terminUrl,
                           String terminFound, String noTerminFound, String buttonId) {
        this.port = port;
        this.terminUrl = terminUrl;
        this.terminFound = terminFound;
        this.noTerminFound = noTerminFound;
        this.buttonId = buttonId;
    }

    public void setupBrowser() throws IOException {

        WebDriverManager.chromedriver().setup();

        service = new ChromeDriverService.Builder()
                .usingPort(Integer.parseInt(port))
                .build();
        service.start();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless"); //silent
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        driver = new RemoteWebDriver(service.getUrl(), options);

    }

    /**
     * This method opens web page [terminUrl].
     * Then clicks on a button [buttonId].
     * Waiting for new response page with one of keywords.
     * There are two variants [terminFound, noTerminFound]
     *
     * @return html page with free termin
     * @throws InterruptedException
     */
    public byte[] findFreeTermin() throws InterruptedException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(BROWSER_TIMEOUT_SEC));

        driver.get(terminUrl);

        By button = By.id(buttonId);
        wait.until(elementToBeClickable(button));
        driver.findElement(button).click();

        int counter = 0;
        while (RESPONSE_TIMEOUT_SEC > counter) {

            //delay before check the page (without delay could be used too much RAM)
            Thread.sleep(2000);

            String html = driver.getPageSource();
            if (html.contains(terminFound)) {
                log.info("###Termin was found###");
                driver.quit();
                service.stop();
                return html.getBytes(StandardCharsets.UTF_8);
            }

            if (html.contains(noTerminFound)) {
                log.info("No termin found.");
                break;
            } else {
                log.info("Waiting...refresh page...");
                counter = counter + 2;
            }
        }

        driver.quit();
        service.stop();
        return null;
    }


}
