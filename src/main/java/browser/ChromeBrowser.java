package browser;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import telegram.TelegramBot;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.Duration;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

@Slf4j
public class ChromeBrowser {

    private WebDriver driver;
    private ChromeDriverService service;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
    private String terminUrl;

    //should be generated manually
    private final String KEY_STRING = "aktuell keine Termine frei";
    public final String TERMIN_STRING = "Auswahl Termin";

    private final int BROWSER_TIMEOUT_SEC = 60;

    private String port;

    public ChromeBrowser(String port, String terminUrl) {
        this.port = port;
        this.terminUrl = terminUrl;
    }

    public void setupBrowser() throws IOException {

        WebDriverManager.chromedriver().setup();

        service = new ChromeDriverService.Builder()
                .usingPort(Integer.valueOf(port))
                .build();
        service.start();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless"); //silent
        options.addArguments("--no-sandbox"); // Bypass OS security model
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        // driver = new ChromeDriver(options);
        driver = new RemoteWebDriver(service.getUrl(), options);

    }

    public byte[] checkTermin() throws InterruptedException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(BROWSER_TIMEOUT_SEC));

        driver.get(terminUrl);

        String buttonId = "applicationForm:managedForm:proceed";
        By button = By.id(buttonId);
        wait.until(elementToBeClickable(button));
        driver.findElement(button).click();


        int secondsToWaitX2 = 60;
        int counter = 0;

        while (secondsToWaitX2 > counter) {
            Thread.sleep(2000);
            String html = driver.getPageSource();
            if (html.contains(TERMIN_STRING)) {
                log.info("TERMIN WAS FOUND");
                driver.quit();
                service.stop();
                return html.getBytes(Charset.forName("UTF-8"));
            }

            if (html.contains(KEY_STRING)) {
                log.info("NO TERMIN");
                break;
            } else {
                log.info("WAITING...REFRESH PAGE");
                counter++;
            }
        }

        driver.quit();
        service.stop();
        return null;
    }


}
