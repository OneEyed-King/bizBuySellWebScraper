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

    @GetMapping("/get-seller-details")
    public List<BusinessListing> getSelerDetails(@RequestParam String url, @RequestHeader(value = "headless", required = false, defaultValue = "false") String headless) throws InterruptedException {

        return seleniumService.scrape(Boolean.parseBoolean(headless));
    }

    @GetMapping("/get-all-listings")
    public List<BusinessListing> getAllListings(@RequestParam String url, @RequestHeader(value = "headless", required = false, defaultValue = "false") String headless) throws InterruptedException {

        return seleniumService.getWebListings(Boolean.parseBoolean(headless));
    }
}
