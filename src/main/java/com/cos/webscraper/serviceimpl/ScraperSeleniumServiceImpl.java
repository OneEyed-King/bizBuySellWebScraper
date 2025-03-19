package com.cos.webscraper.serviceimpl;

import com.cos.webscraper.service.ScraperSeleniumService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.cos.webscraper.model.BusinessListing;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.TimeoutException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScraperSeleniumServiceImpl implements ScraperSeleniumService {

    private WebDriver driver;
    private WebDriverWait wait;
    private Gson gson;
    private int count;
    private final String url = "https://www.bizbuysell.com/";

    private void initializeDriver(boolean isHeadless) {

        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("permissions.default.image", 2);
        options.addPreference("dom.webnotifications.enabled", false);
        if (isHeadless) {
            options.addArguments("--headless");
        }
        this.driver = new FirefoxDriver(options);
        this.driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.gson = new GsonBuilder().setPrettyPrinting().create();

    }

    @Override
    public List<BusinessListing> scrape(boolean isHeadless, String countStr) throws InterruptedException {
        initializeDriver(isHeadless);
        List<BusinessListing> businessListings = new ArrayList<>();
        List<BusinessListing> fetchedListings = new ArrayList<>();
        int count = Integer.parseInt(countStr); // Convert once
        try {
            driver.get(url + "/buy/");

            List<WebElement> listingElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.diamond")));
            businessListings = extractListingDetails(listingElements);


            System.out.println("Found " + businessListings.size() + " listings.");

            for (BusinessListing listing : businessListings) {
//                if (count == 5) {
//                    driver.quit();
//                    initializeDriver(isHeadless);
//                    count = 0;
//                }
                extractSellerDetails(listing);
                fetchedListings.add(listing);
                if(fetchedListings.size() >= count){
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Unable to scrape web listing, reason :{} ", e.getMessage());
        } finally {
            driver.quit();
        }

        return fetchedListings;
    }

    @Override
    public List<BusinessListing> getWebListings(boolean isHeadless) {
        initializeDriver(isHeadless);
        List<BusinessListing> businessListings = new ArrayList<>();

        try {
            driver.get(url + "/buy/");

            List<WebElement> listingElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("a.diamond")));
            businessListings = extractListingDetails(listingElements);

        } catch (Exception e) {
            log.error("Unable to scrape web listing, reason :{} ", e.getMessage());
        } finally {
            driver.quit();
        }
        return businessListings;
    }


    private List<BusinessListing> extractListingDetails(List<WebElement> listingElements) {

        List<BusinessListing> businessListings = new ArrayList<>();

        for (WebElement listing : listingElements) {
            try {
                BusinessListing businessListing = new BusinessListing();
                businessListing.setName(listing.getText());

                String listingUrl = listing.getAttribute("href");
                listingUrl = listingUrl.substring(0, listingUrl.length() - 1);
                String listingId = listingUrl.substring(listingUrl.lastIndexOf("/") + 1).replaceAll("/", "");

                businessListing.setUrl(listingUrl);
                businessListing.setListingId(listingId);

                String contactButtonId = "hlViewTelephone_" + listingId;
                businessListing.setContactButtonId(contactButtonId);
                businessListings.add(businessListing);
            } catch (Exception e) {
                System.err.println("Error processing listing: " + e.getMessage());
            }
        }

        return businessListings;

    }

    private void extractSellerDetails(BusinessListing listing) {
        try {
            driver.get(listing.getUrl());

            JavascriptExecutor js = (JavascriptExecutor) driver;

            try {
                WebElement contactButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(listing.getContactButtonId())));
                js.executeScript("arguments[0].click();", contactButton);

                wait.until(ExpectedConditions.stalenessOf(contactButton));

                WebElement phoneElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.id("lblViewTpnTelephone_" + listing.getListingId())));

                listing.setSellerContact(phoneElement.getText());
            } catch (TimeoutException te) {
                System.err.println("Timeout waiting for contact details for: " + listing.getName());
            }


            try {
                WebElement sellerName = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".seller-name")));
                listing.setSellerName(sellerName.getText().replace("Listed By:", "").trim());
            } catch (TimeoutException te) {
                System.err.println("Timeout waiting for seller name for: " + listing.getName());
            }


            System.out.println("Extracted contact for: " + gson.toJson(listing));
        } catch (Exception e) {
            System.err.println("Failed to extract contact for: " + listing.getName() + ": " + e.getMessage());
        }
    }
}
