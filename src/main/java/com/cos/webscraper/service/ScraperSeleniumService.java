package com.cos.webscraper.service;

import com.cos.webscraper.model.BusinessListing;
import com.cos.webscraper.model.dto.Regions;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScraperSeleniumService {

    CompletableFuture<List<BusinessListing>> scrapeAsync(boolean isHeadless, String count, String skip, String region) throws InterruptedException;

    List<BusinessListing> getWebListings(boolean b);

    List<Regions> getAllRegions(boolean b);

    List<String> getAllIndustries(boolean b);
}
