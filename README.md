# BizBuySell Web Scraper

This Java application scrapes business listings from the BizBuySell website. It uses Selenium to automate browser interactions and extract data such as business name, price, revenue, cash flow, and other relevant details.

## Getting Started

### Prerequisites

* Docker

### Running the application

1. Pull the Docker image:

```bash
docker pull oneeyedking/bizbuysellscraper
```

2. Run the Docker container:

```bash
docker run -p 8080:8080 oneeyedking/bizbuysellscraper
```

The application will be accessible at `http://localhost:8080`.

## Building from source

Alternatively, you can build the application from source:

1. Clone the repository:

```bash
git clone https://github.com/your-username/bizbuysell-webscraper.git
```

2. Build the Docker image:

```bash
docker build -t bizbuysell-webscraper .
```

3. Run the Docker container:

```bash
docker run -p 8080:8080 bizbuysell-webscraper
```

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## API Documentation

The application exposes the following REST APIs:

* **GET /selenium/get-seller-details**

    Retrieves seller details.

    * **Request Header:**
        * `headless` (optional, header): Whether to run Selenium in headless mode. Defaults to false.
        * `count` (optional, request param): User can specify how many details it tobe fetched. Defaults value 5.

    * **Returns:** A list of business listings.


* **GET /selenium/get-all-listings**

    Retrieves all business listings.

    * **Request Header:**
        * `headless` (optional, header): Whether to run Selenium in headless mode. Defaults to false.

    * **Returns:** A list of business listings.

## Service Implementation Details

The `ScraperSeleniumServiceImpl` class implements the `ScraperSeleniumService` interface and provides the scraping logic.

Here's a breakdown of the key methods:

* **`initializeDriver(boolean isHeadless)`:** Initializes the WebDriver(we are using firefox webdriver), sets up browser options, and configures other properties like notification block and headless browsing using options. The `isHeadless` parameter controls whether the browser runs in headless mode.

* **`scrape(boolean isHeadless, String countStr)`:** This method scrapes business listings from the BizBuySell website. It takes two parameters: `isHeadless` (whether to run in headless mode) and `countStr` (the number of listings to fetch). It first initializes the WebDriver with initializeDriver(boolean isHeadless) method, navigates to the BizBuySell website, and retrieves a list of all business listings on the respective webpage . Then, it iterates through each listing, and calls a method to extracts seller details, and adds the listing to a result list. The method returns a list of `BusinessListing` objects.

* **`getWebListings(boolean isHeadless)`:** This method retrieves all business listings from the BizBuySell website on (www.bizbuysell.com/buy/ webpage). It takes one parameter: `isHeadless` (whether to run in headless mode). It initializes the WebDriver  with initializeDriver(boolean isHeadless) method, navigates to the BizBuySell website, and extracts listing details(a list of all elements) and passes it to another method to extract the href links. The method returns a list of `BusinessListing` objects.

* **`extractListingDetails(List<WebElement> listingElements)`:** This helper method extracts href links form list of WebElement elements. It iterates through each WebElement, extracts the business name, URL, listing ID(used to locate contact button), and contact button ID, and creates a `BusinessListing` object with these details. The method returns a list of `BusinessListing` objects.

* **`extractSellerDetails(BusinessListing listing)`:** This helper method extracts seller details(mainly seller contact number and listed by name) for a given business listing. It navigates to the listing URL, clicks the contact button, waits for the elements to appear and extracts the seller's contact  information and name if contact button is not available it only extracts the listed by name. It updates the provided `BusinessListing` object with the extracted details.
