package com.cos.webscraper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BusinessListing {
    private String name;
    private String url;
    private String contactButtonId;
    private String sellerName;
    private String sellerContact;
    private String listingId;
    private String description;
    private String financials;
    private String detailedInfo;

    public BusinessListing(String name, String listingUrl, String contactButtonId, String listingId) {
    }
}
