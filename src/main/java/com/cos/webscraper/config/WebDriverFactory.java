package com.cos.webscraper.config;

import com.google.gson.Gson;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WebDriverFactory {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();
    private WebDriver driver;
    private WebDriverWait wait;
    private Gson gson;

    public static WebDriver getDriver(boolean isHeadless) {
        if (driverThreadLocal.get() == null) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
//        options.enableBiDi();
            options.addPreference("permissions.default.image", 2);
            options.addPreference("dom.webnotifications.enabled", false);
            if (isHeadless) {
                options.addArguments("--headless");
            }

            WebDriver driver = new FirefoxDriver(options); // ✅ Store instance in a variable
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driverThreadLocal.set(driver);
            waitThreadLocal.set(new WebDriverWait(driver, Duration.ofSeconds(20)));
        }
        return driverThreadLocal.get();
    }

    public static WebDriverWait getWait() {
        return waitThreadLocal.get(); // ✅ Ensures each thread gets its own WebDriverWait
    }

    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            driverThreadLocal.get().quit();  // ✅ Only closes WebDriver for the current thread
            driverThreadLocal.remove();
            waitThreadLocal.remove();
        }
    }

}
