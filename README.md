# EDIT-Extension
An Extension of EDIT framework to perform web scraping on Etherscan, taking informations like transactions, nametags and funders.
# Overview
To enhance the capabilities of the EDIT framework, a web scraping module is incorporated to extract additional metadata for each DID. Specifically, a component that utilizes Java and Selenium WebDriver is added to automate the extraction of the Nametag and Funded By fields from the Etherscan website. The Funded By, in particular, enlight the first address that has sent some Ether in the account. This enables to profile every community by knowing the behavior and goals of each of its most central nodes. Selenium WebDriver is a web framework that permits you to execute cross-browser tests. This tool is used for automating web-based application testing to verify that it performs as expected. 

This web scraping technique allows to gather information that is not readily available through standard API calls or data exports. The Java program navigates to the Etherscan page corresponding to each DID and uses XPath expressions to locate and retrieve fields. XPath is an essential tool for web scraping, allowing to select specific elements from an HTML document for extraction. 
# Code Specification
In this part the most important parts of the code will be analysed.
## ChromeDriver
**WebDriver driver = new ChromeDriver();**


In this line it is initialized a new ChromeDriver. ChromeDriver is a standalone server that implements the W3C WebDriver standard, enabling automation of the Google Chrome browser. It is commonly used in conjunction with Selenium WebDriver to automate tests and interactions with web applications.
## XPath Expression
**WebElement element = driver.findElement(By.xpath(xPathEspression));**


To accurately extract the "Nametag" and "Funded by" fields from the Etherscan website, the Chrome browser's Developer Tools is used to explore the HTML structure of the web page. By right-clicking on the specific elements displaying the "Nametag" and "Funded by" information and selecting Inspect, it was possible to view and analyze the underlying HTML code corresponding to these components. This process allowed for the identification of the exact HTML tags, attributes, and hierarchical structure associated with this data. By copying the relevant HTML snippets from the inspection tool, the precise Xpath expressions needed to locate these elements programmatically using Selenium WebDriver in Java is determinated.
### Nametag Expression
* *//a[@data-bs-content-id='popover-ens-preview']//span[1]* 
### Funded By Expression
* *//h4[text()='Funded By']/following-sibling::div//span[@class='d-block text-truncate']* 


