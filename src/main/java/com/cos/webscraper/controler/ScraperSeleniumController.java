package com.cos.webscraper.controler;

import java.util.List;

import com.cos.webscraper.model.BusinessListing;
import com.cos.webscraper.serviceimpl.ScraperSeleniumServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selenium")
public class ScraperSeleniumController {

    @Autowired
    private ScraperSeleniumServiceImpl seleniumService;

    /**
     * Retrieves seller details.
     *
     *
     * @param headless Whether to run Selenium in headless mode. Defaults to false.
     * @return A list of business listings.
     *
     */
    @GetMapping("/get-seller-details")
    public List<BusinessListing> getSelerDetails(@RequestHeader(value = "headless", required = false, defaultValue = "false") String headless) throws InterruptedException {

        return seleniumService.scrape(Boolean.parseBoolean(headless));
    }

    /**
     * Retrieves all business listings.
     *
     *
     * @param headless Whether to run Selenium in headless mode. Defaults to false.
     * @return A list of business listings.
     *
     */
    @GetMapping("/get-all-listings")
    public List<BusinessListing> getAllListings(@RequestHeader(value = "headless", required = false, defaultValue = "false") String headless) throws InterruptedException {

        return seleniumService.getWebListings(Boolean.parseBoolean(headless));
    }
}
