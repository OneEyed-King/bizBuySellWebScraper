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

    * **Returns:** A list of business listings.


* **GET /selenium/get-all-listings**

    Retrieves all business listings.

    * **Request Header:**
        * `headless` (optional, header): Whether to run Selenium in headless mode. Defaults to false.

    * **Returns:** A list of business listings.
