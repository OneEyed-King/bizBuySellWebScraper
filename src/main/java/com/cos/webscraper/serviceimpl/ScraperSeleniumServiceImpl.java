package com.cos.webscraper.serviceimpl;

import com.cos.webscraper.config.WebDriverFactory;
import com.cos.webscraper.model.dto.Regions;
import com.cos.webscraper.model.dto.RegionsResponseDto;
import com.cos.webscraper.service.ScraperSeleniumService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.cos.webscraper.model.BusinessListing;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v134.network.Network;
import org.openqa.selenium.devtools.v134.network.model.RequestId;
import org.openqa.selenium.devtools.v134.network.model.Response;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.bidi.module.Network;

import java.lang.Thread;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ScraperSeleniumServiceImpl implements ScraperSeleniumService {


    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private int count;
    private final String url = "https://www.bizbuysell.com/";
    @Autowired private WebDriverFactory webDriverFactory;


    @Override
    @Async("asyncExecutor")  // Runs in a separate thread pool
    public CompletableFuture<List<BusinessListing>> scrapeAsync(boolean headless, String count, String skip) {
        List<BusinessListing> listings = scrape(headless, count, skip); // Your scraping logic
        return CompletableFuture.completedFuture(listings);
    }


    public List<BusinessListing> scrape(boolean isHeadless, String countStr, String skip) {
        WebDriver driver = webDriverFactory.getFireFoxDriver(isHeadless);
//        WebDriver driver = webDriverFactory.getChromeDriver(isHeadless);
        WebDriverWait wait = WebDriverFactory.getWait();
        List<BusinessListing> businessListings = new ArrayList<>();
        List<BusinessListing> fetchedListings = new ArrayList<>();
        List<WebElement> listingElements = new ArrayList<>();
        int count = Integer.parseInt(countStr); // Convert once
        int retry = 0;
        try {

            while (retry < 8) {
                try {
                    driver.get(url + "/buy/");
//                    driver.get("https://myexternalip.com/raw");
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    Thread.sleep(3000); // Let elements load
                    listingElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.diamond")));
                    break;
                } catch (Exception e) {
                    log.warn("retrying... count: {}", retry);
                    driver = rotateProxy(isHeadless);  // Rotate proxy and user agent
                    wait = WebDriverFactory.getWait();
                    retry++;
                }
            }
            if (retry >= 8) {
                log.error(" Max retries done returning");
                throw new TimeoutException();
            }
            businessListings = extractListingDetails(listingElements.subList(Integer.parseInt(skip), listingElements.size()));

            log.info("Found {} listings.", businessListings.size());
            JavascriptExecutor js = (JavascriptExecutor) driver;


            for (BusinessListing listing : businessListings) {

                extractSellerDetails(listing, driver, wait, isHeadless);
                if(listing.isBlocked()){
                    driver = rotateProxy(isHeadless);  // Rotate proxy and user agent
                    wait = WebDriverFactory.getWait();
                }
                fetchedListings.add(listing);
                if (fetchedListings.size() >= count || !businessListings.iterator().hasNext()) {
                    break;
                }

                driver.navigate().refresh();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            }
        } catch (Exception e) {
            log.error("Unable to scrape web listing details, reason :{} ", e.getMessage());
            return fetchedListings;
        } finally {
            WebDriverFactory.quitDriver();
        }

        return fetchedListings;
    }

    @Override
    public List<BusinessListing> getWebListings(boolean isHeadless) {
        WebDriver driver = webDriverFactory.getChromeDriver(isHeadless);
        WebDriverWait wait = WebDriverFactory.getWait();
        List<BusinessListing> businessListings = new ArrayList<>();

        try {
            driver.get(url + "/buy/");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            Thread.sleep(2000); // Wait for content to load

            List<WebElement> listingElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.diamond")));
            businessListings = extractListingDetails(listingElements);

        } catch (Exception e) {
            log.error("Unable to scrape web listing, reason :{} ", e.getMessage());
        } finally {
            WebDriverFactory.quitDriver();
        }
        return businessListings;
    }

    @Override
    public List<Regions> getAllRegions(boolean isHeadless) {
        WebDriver driver = webDriverFactory.getChromeDriver(isHeadless);
        WebDriverWait wait = WebDriverFactory.getWait();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300));
        AtomicReference<List<Regions>> regions = new AtomicReference<>();

        DevTools devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();

        // Enable network monitoring
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

        // Listen for network requests
        devTools.addListener(Network.requestWillBeSent(), request -> {
            if (request.getRequest().getUrl().contains("api/Resource/GetRegions")) {
                log.info("➡ API request detected: {}", request.getRequest().getUrl());
            }
        });

        // Listen for network responses
        devTools.addListener(Network.responseReceived(), response -> {
            Response res = response.getResponse();
            if (res.getUrl().contains("api/Resource/GetRegions")) {
                log.info("Api Found +++++++++++++++");
                RequestId requestId = response.getRequestId(); // Needed to fetch response body
                log.info("   Status Code: {}", res.getStatus());
                try {
                    Thread.sleep(5000);
                    Network.GetResponseBodyResponse bodyResponse = devTools.send(Network.getResponseBody(requestId));
                    String responseBody = bodyResponse.getBody();


                    log.info("Status Code: {}", res.getStatus());
                    ObjectMapper objectMapper = new ObjectMapper();
                    RegionsResponseDto apiResponse = objectMapper.readValue(responseBody, RegionsResponseDto.class);
                    regions.set(apiResponse.getRegions());
                } catch (Exception e) {
                    log.error("Error processing API response, reason: {}", e.getMessage());
                }
            }
        });

        // Visit a website
        driver.get(url + "/buy/");

        // Wait a few seconds to capture requests
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Close browser
        driver.quit();
        return regions.get();
    }

    private List<BusinessListing> extractListingDetails(List<WebElement> listingElements) {


        List<BusinessListing> businessListings = new ArrayList<>();

        for (WebElement listing : listingElements) {
            try {
                BusinessListing businessListing = new BusinessListing();

                String listingUrl = listing.getAttribute("href");
                listingUrl = listingUrl.substring(0, listingUrl.length() - 1);
                String listingId = listingUrl.substring(listingUrl.lastIndexOf("/") + 1).replaceAll("/", "");

                businessListing.setUrl(listingUrl);
                businessListing.setListingId(listingId);

                String contactButtonId = "hlViewTelephone_" + listingId;
                businessListing.setContactButtonId(contactButtonId);
                businessListings.add(businessListing);
            } catch (Exception e) {
                log.error("Error processing listing: {}", e.getMessage());
            }
        }

        return businessListings;

    }

    private void extractSellerDetails(BusinessListing listing, WebDriver driver, WebDriverWait wait, boolean isHeadless) {

        int retryCount = 0;

        try {
            Document doc = null;
            while (retryCount < 4) {
                try {
                    driver.get(listing.getUrl());
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
                    String pageSource = driver.getPageSource();
                    doc = Jsoup.parse(pageSource);
                    JavascriptExecutor js = (JavascriptExecutor) driver;

                    try {
                        WebElement contactButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(listing.getContactButtonId())));
                        js.executeScript("arguments[0].click();", contactButton);

                        wait.until(ExpectedConditions.stalenessOf(contactButton));

                        WebElement phoneElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.id("lblViewTpnTelephone_" + listing.getListingId())));

                        listing.setSellerContact(phoneElement.getText());
                    } catch (TimeoutException te) {
                        log.error("Timeout waiting for contact details for: {} reason: {}", listing.getName(), te.getMessage());

                    }

                    try {
                        WebElement sellerName = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#contactSellerForm > div:nth-child(9) > div:nth-child(1) > div:nth-child(1)")));
                        listing.setSellerName(sellerName.getText().replace("Listed By:", "").trim());
                    } catch (TimeoutException te) {
                        log.error("Timeout waiting for seller name for: {} reason: {}", listing.getName(), te.getMessage());
                    }

                    break;
                } catch (Exception e) {
                    log.warn("Blocked page detected for: {}, switching proxy.", listing.getName());
                    log.warn("retry count: {} ", retryCount);
//                    driver = rotateProxy(isHeadless);  // Rotate proxy and user agent
//                    wait = WebDriverFactory.getWait();
                    listing.setBlocked(true);
                    retryCount++;
                }

//                if (isBlocked(pageSource)) {
//                    log.warn("Blocked page detected for: {}, switching proxy.", listing.getName());
//                    driver = rotateProxy(isHeadless);  // Rotate proxy and user agent
//                    wait = WebDriverFactory.getWait();
//                    retryCount++;
//
//                } else {
//                    break;
//                }
            }

            if (retryCount > 8) {
                log.error(" Max retries done returning");
                return;
            }

            Element headingElement = doc.selectFirst("h1.bfsTitle"); // Adjusted to correct selector
            String listingTitle = headingElement != null ? headingElement.text().trim() : "N/A";
            listing.setName(listingTitle);

            StringBuilder financeString = new StringBuilder();
            Elements financials = doc.select(".financials .row p");  // Locate financial section
            for (Element item : financials) {
                String title = item.select(".title").text();
                String value = item.select(".normal").text();
                System.out.println(title + " " + value);
                financeString.append(String.format("%s %s\n", title, value));
            }
            listing.setFinancials(financeString.toString());


            Element descriptionElement = doc.selectFirst(".businessDescription");
            if (descriptionElement != null) {
                System.out.println(descriptionElement.text().trim());
                listing.setDescription(descriptionElement.text().trim());
            }


            StringBuilder detailedinfo = new StringBuilder();
            Elements details = doc.select("#ctl00_ctl00_Content_ContentPlaceHolder1_wideProfile_listingDetails_dlDetailedInformation dt");
            Elements values = doc.select("#ctl00_ctl00_Content_ContentPlaceHolder1_wideProfile_listingDetails_dlDetailedInformation dd");

            for (int i = 0; i < details.size(); i++) {
                String detailKey = details.get(i).text();
                String detailValue = values.get(i).text();
                System.out.println(detailKey + ": " + detailValue);
                detailedinfo.append(String.format("%s %s\n", detailKey, detailValue));
            }
            listing.setDetailedInfo(detailedinfo.toString());
//            JavascriptExecutor js = (JavascriptExecutor) driver;
//
//            try {
//                WebElement contactButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(listing.getContactButtonId())));
//                js.executeScript("arguments[0].click();", contactButton);
//
//                wait.until(ExpectedConditions.stalenessOf(contactButton));
//
//                WebElement phoneElement = wait.until(ExpectedConditions.presenceOfElementLocated(
//                        By.id("lblViewTpnTelephone_" + listing.getListingId())));
//
//                listing.setSellerContact(phoneElement.getText());
//            } catch (TimeoutException te) {
//                log.error("Timeout waiting for contact details for: {} reason: {}", listing.getName(), te.getMessage());
//            }
//
//            try {
//                WebElement sellerName = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#contactSellerForm > div:nth-child(9) > div:nth-child(1) > div:nth-child(1)")));
//                listing.setSellerName(sellerName.getText().replace("Listed By:", "").trim());
//            } catch (TimeoutException te) {
//                log.error("Timeout waiting for seller name for: {} reason: {}", listing.getName(), te.getMessage());
//            }

            listing.setBlocked(false);
            log.info("Extracted contact for: {}", gson.toJson(listing));

        } catch (Exception e) {
            log.error("Failed to extract contact for: {} : {}", listing.getName(), e.getMessage());
        }

    }

    private WebDriver rotateProxy(boolean isHeadless) {

        WebDriverFactory.quitDriver();
        return webDriverFactory.getChromeDriver(isHeadless);

    }

    private boolean isBlocked(String pageSource) {
        // Simple check for block page (can be extended to look for other block signs)
        return pageSource.contains("captcha") || pageSource.contains("Access Denied") || pageSource.contains("403 Forbidden") || pageSource.contains("connection failed");
    }

}
