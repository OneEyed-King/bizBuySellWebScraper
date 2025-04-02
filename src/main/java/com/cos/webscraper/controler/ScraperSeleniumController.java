package com.cos.webscraper.controler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.cos.webscraper.model.BusinessListing;
import com.cos.webscraper.model.dto.Regions;
import com.cos.webscraper.service.ScraperSeleniumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selenium")
public class ScraperSeleniumController {

    @Autowired
    private ScraperSeleniumService seleniumService;

    /**
     * Retrieves seller details.
     *
     *
     * @param headless Whether to run Selenium in headless mode. Defaults to false.
     * @return A list of business listings.
     *
     */
    @GetMapping("/get-seller-details")
    public CompletableFuture<List<BusinessListing>> getSellerDetails(
            @RequestHeader(value = "headless", required = false, defaultValue = "false") String headless,
            @RequestParam(required = false, defaultValue = "5") String count,
            @RequestParam(required = false, defaultValue = "0") String skip,
            @RequestParam(required = false, defaultValue = "0") String region) throws InterruptedException {

        return seleniumService.scrapeAsync(Boolean.parseBoolean(headless), count, skip, region);
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

    @GetMapping("/get-all-regions")
    public List<Regions> getAllRegions(@RequestHeader(value = "headless", required = false, defaultValue = "false") String headless) throws InterruptedException {

        return seleniumService.getAllRegions(Boolean.parseBoolean(headless));
    }


    @GetMapping("/get-all-industries")
    public List<String> getAllIndustries(@RequestHeader(value = "headless", required = false, defaultValue = "false") String headless) throws InterruptedException {
        return seleniumService.getAllIndustries(Boolean.parseBoolean(headless));

    }

    @GetMapping("/get-by-region-and-industry")
    public CompletableFuture<List<BusinessListing>> getByRegionAndIndustry(@RequestHeader(value = "headless", required = false, defaultValue = "false") String headless,
                                                        @RequestParam(required = false, defaultValue = "5") String count,
                                                        @RequestParam(required = false, defaultValue = "0") String skip,
                                                        @RequestParam(value = "region", defaultValue = "california")  String region,
                                                        @RequestParam (value = "industry", defaultValue = "") String industry) throws IOException, InterruptedException, ExecutionException {
        return seleniumService.getBusinessesByRegionAndIndustry(region, industry, Boolean.parseBoolean(headless), count, skip);
    }

}
