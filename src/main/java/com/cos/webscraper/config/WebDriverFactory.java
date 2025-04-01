package com.cos.webscraper.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
@Component
@Slf4j
public class WebDriverFactory {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> userAgentThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> userProxy = new ThreadLocal<>();
    @Autowired private UserAgents userAgents;

    public WebDriver getFireFoxDriver(boolean isHeadless) {
        if (driverThreadLocal.get() == null) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
//        options.enableBiDi();

//            String randomUserAgent = getRandomUserAgent();
//            options.addPreference("general.useragent.override", randomUserAgent);
            options.addArguments("--start-maximized", "--start-fullscreen", "--incognito");
            options.addPreference("dom.webnotifications.enabled", false);
//            options.setAcceptInsecureCerts(true);
            options.addPreference("intl.accept_languages", "en-US,en;q=0.9");
            options.addPreference("network.http.accept-encoding", "gzip, deflate, br");
            options.addPreference("network.http.sendRefererHeader", 1);  // 1: Send the Referer header
            //            options.addPreference("general.useragent.override", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            options.addPreference("network.http.use-cache", false);
            options.addPreference("security.mixed_content.upgrade_display_content", true);
            options.setAcceptInsecureCerts(true);
            options.addArguments("--start-maximized");
            options.addArguments("--start-fullscreen");
            options.addArguments("--incognito");

            if (isHeadless) {
                options.addArguments("--headless");
            }


            WebDriver driver = new FirefoxDriver(options); // ✅ Store instance in a variable
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
            js.executeScript("Object.defineProperty(navigator, 'plugins', {get: () => [1,2,3]})");
            driverThreadLocal.set(driver);
            waitThreadLocal.set(new WebDriverWait(driver, Duration.ofSeconds(20)));

        }
        return driverThreadLocal.get();
    }


    public static File createChromeProxyExtension(String proxy) throws IOException {
        List<String> credentials = List.of(proxy.split(":"));
        String extensionPath = "proxy_auth_plugin.xpi";

        String manifestJson = """
                {
                  "manifest_version": 2,
                  "name": "Firefox Proxy Auth Extension",
                  "version": "1.0",
                  "permissions": [
                    "proxy",
                    "storage",
                    "webRequest",
                    "webRequestBlocking",
                    "<all_urls>"
                  ],
                  "host_permissions": ["<all_urls>"],
                  "background": {
                    "service_worker": "background.js"
                  }
                }
                """;

        String backgroundJs = String.format("""
                var config = {
                  mode: "fixed_servers",
                  rules: {
                    singleProxy: {
                      scheme: "http",
                      host: "%s",
                      port: %d
                    },
                    bypassList: ["localhost", "127.0.0.1"]
                  }
                };
                
                chrome.proxy.settings.set({ value: config, scope: "regular" }, function() {});
                
                chrome.webRequest.onAuthRequired.addListener(
                  function(details) {
                    return {
                      authCredentials: {
                        username: "%s",
                        password: "%s"
                      }
                    };
                  },
                  { urls: ["<all_urls>"] },
                  ["blocking"]
                );
                """, credentials.get(0), Integer.parseInt(credentials.get(1)), credentials.get(2), credentials.get(3));

        // Create the temporary files for the extension
        File tempFile = File.createTempFile("proxy_auth_plugin", ".zip");
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             ZipOutputStream zipOS = new ZipOutputStream(fos)) {

            // Create manifest.json and background.js
            createFile("manifest.json", manifestJson);
            createFile("background.js", backgroundJs);

            // Add files to the zip
            writeToZipFile("manifest.json", zipOS);
            writeToZipFile("background.js", zipOS);
        }

        return tempFile;
    }

    // Helper method to write files to the zip
    public static void writeToZipFile(String path, ZipOutputStream zipStream) throws IOException {
        System.out.println("Writing file : '" + path + "' to zip file");
        File aFile = new File(path);
        FileInputStream fis;
        try {
            fis = new FileInputStream(aFile);
            ZipEntry zipEntry = new ZipEntry(path);
            zipStream.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipStream.write(bytes, 0, length);
            }
            zipStream.closeEntry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to create files (manifest.json and background.js)
    public static void createFile(String filename, String text) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println(text);
        }
    }

    public WebDriver getChromeDriver(boolean isHeadless) {
        if (driverThreadLocal.get() == null) {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();

            // Assign a unique user agent per thread
//            String randomUserAgent = getRandomUserAgent();
//            options.addArguments("user-agent=" + randomUserAgent);

            options.addArguments("blink-settings=imagesEnabled=true");

            // Disable web notifications
//            options.addArguments("--disable-notifications");

            // Enable insecure certificates (for sites with self-signed certs)
//            options.setAcceptInsecureCerts(true);

            // Set Accept-Language preference
            options.addArguments("--lang=en-US");

            // Set Accept-Encoding header
            options.addArguments("--accept-encoding=gzip, deflate, br");

            // Set Referer header sending preference (through user-agent or custom headers)
//            options.addArguments("referer=https://example.com");

            // Disable cache to simulate no-cache behavior
            options.addArguments("--disable-cache");

            // Upgrade insecure requests to HTTPS
            options.addArguments("--enable-mixed-content");
            List<String> proxy = Collections.singletonList(getRandomUserProxy());
            if(!proxy.isEmpty()){
                List<String> randomProxy = List.of(proxy.get(0).split(":"));

                options.addArguments("--proxy-server=http://"+randomProxy.get(2)+":"+randomProxy.get(3)+"@"+randomProxy.get(0)+":"+ Integer.parseInt(randomProxy.get(1)));

                try {
                    File extension = createChromeProxyExtension(proxy.toString().replaceAll("[\\[\\]]", ""));
                    options.addExtensions(extension);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            // Exclude the "enable-automation" switch to prevent detection
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

            // Disable the automation extension
            options.setExperimentalOption("useAutomationExtension", false);

//            options.addArguments("--user-agent=" + getRandomUserAgent());


            // Enable headless mode if specified
            if (isHeadless) {
                options.addArguments("--headless");
            }

            // Initialize the ChromeDriver with the set options
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

            // Store the driver in ThreadLocal for thread safety
            driverThreadLocal.set(driver);
            waitThreadLocal.set(new WebDriverWait(driver, Duration.ofSeconds(20)));
        }
        return driverThreadLocal.get();
    }

    public String getRandomUserAgent() {
        if (userAgentThreadLocal.get() == null) {
            userAgentThreadLocal.set(userAgents.getRandomUserAgent());
        }
        return userAgentThreadLocal.get();
    }

    public String getRandomUserProxy() {
        if (userProxy.get() == null) {
            userProxy.set(userAgents.getRandomProxy());
        }
        return userProxy.get();
    }

    public static WebDriverWait getWait() {
        return waitThreadLocal.get(); // ✅ Ensures each thread gets its own WebDriverWait
    }

    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            driverThreadLocal.get().quit();  // ✅ Only closes WebDriver for the current thread
            driverThreadLocal.remove();
            waitThreadLocal.remove();
            userAgentThreadLocal.remove();
            userProxy.remove();
        }
    }

}
