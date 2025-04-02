# Web Scraper

This project is a web scraper built using Spring Boot and Selenium. It scrapes data from a website and provides several APIs to access the scraped data.

## APIs

Here are the available APIs:

### Get Seller Details

```
GET /selenium/get-seller-details
```

Retrieves seller details.

**Parameters:**

* `headless` (optional, default: `false`): Whether to run Selenium in headless mode.
* `count` (optional, default: `5`): The number of listings to retrieve.
* `skip` (optional, default: `0`): The number of listings to skip.
* `region` (optional, default: `0`): The region to filter by.


### Get All Listings

```
GET /selenium/get-all-listings
```

Retrieves all business listings.

**Parameters:**

* `headless` (optional, default: `false`): Whether to run Selenium in headless mode.



### Get All Regions

```
GET /selenium/get-all-regions
```

Retrieves all available regions.

**Parameters:**

* `headless` (optional, default: `false`): Whether to run Selenium in headless mode.



### Get All Industries

```
GET /selenium/get-all-industries
```

Retrieves all available industries.

**Parameters:**

* `headless` (optional, default: `false`): Whether to run Selenium in headless mode.


### Get By Region and Industry

```
GET /selenium/get-by-region-and-industry
```

Retrieves business listings filtered by region and industry.

**Parameters:**

* `headless` (optional, default: `false`): Whether to run Selenium in headless mode.
* `count` (optional, default: `5`): The number of listings to retrieve.
* `skip` (optional, default: `0`): The number of listings to skip.
* `region` (required, default: `california`): The region to filter by.
* `industry` (required, default: ``): The industry to filter by.
