package com.cos.webscraper.service;

import com.cos.webscraper.model.BusinessListing;

import java.util.List;

public interface ScraperSeleniumService {

    List<BusinessListing> scrape(boolean isHeadless) throws InterruptedException;

    List<BusinessListing> getWebListings(boolean b);
}
