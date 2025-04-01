package com.cos.webscraper.service;

import com.cos.webscraper.model.BusinessListing;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ScraperSeleniumService {

    CompletableFuture<List<BusinessListing>> scrapeAsync(boolean isHeadless, String count, String skip) throws InterruptedException;

    List<BusinessListing> getWebListings(boolean b);

    List<BusinessListing> getAllRegions(boolean b);
}
