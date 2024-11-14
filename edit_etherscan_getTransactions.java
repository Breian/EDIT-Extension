import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.NoSuchElementException;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


/**
 * EDIT component that takes a list Ethereum addresses and retrives available transactions from Etherscan website
 */
public class edit_etherscan_getTransactions {
    /**
     * Given a set of addresses, the function retrieves available transactions in Etherscan by using automated browser
     * @param addresses The list of addresses
     * @param pathDestFolder The path folder where CSV transactions are stored. One CSV file for each address
     * @param defaultDownloadPath The path where browser downloads files
     */
    public static void getTransactions (ArrayList<String> addresses,String pathDestFolder,  String defaultDownloadPath){
        //Path to automated Browser chromedriver Selenium API
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\user\\Downloads\\SSIDeanonimization\\SSIDeanonimization\\lib\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-extensions");
        options.addArguments("--start-maximized");
        options.addArguments("--single-process");
        options.addArguments("--no-sandbox");

        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(4));
        
        for(String did:addresses) {
            File fDo=new File(pathDestFolder+"\\export-"+did+".csv");
            if(!fDo.exists()) {
                String url = "https://etherscan.io/address/" + did;
                System.out.println("go to " + url);
                driver.get(url);
                
                WebElement element = driver.findElement(By.xpath("//p[text()[contains(.,'from a total of')]]"));
                System.out.println(element.getAttribute("innerHTML"));
                String total = element.getText();
                String Ntransactions = total.substring(total.indexOf("from a total of") + "from a total of".length(), total.indexOf("transact")).trim();
                
                String nametag = "";
                try {
                    WebElement nametagElement = driver.findElement(By.xpath("//a[@data-bs-content-id='popover-ens-preview']//span[1]"));
                    nametag = nametagElement.getText().trim();
                    System.out.println("Nametag: " + nametag);
                } catch (NoSuchElementException e) {
                    System.out.println("Nametag not found for address: " + did);
                }

                String fundedBy = "";
                try {
                    // Check if the "Funded By" is human-readable
                    WebElement fundedByElement = driver.findElement(By.xpath("//h4[text()='Funded By']/following-sibling::div//span[@class='d-block text-truncate']"));
                    fundedBy = fundedByElement.getText().trim();
                    System.out.println("Funded by (human-readable): " + fundedBy);
                } catch (NoSuchElementException e1) {
                    try {
                        // Check if the "Funded By" is a non-human-readable address
                        WebElement fundedByElement = driver.findElement(By.xpath("//h4[text()='Funded By']/following-sibling::div//span[@data-highlight-target]"));
                        fundedBy = fundedByElement.getAttribute("data-highlight-target").trim();
                        System.out.println("Funded by (non-human-readable address): " + fundedBy);
                    } catch (NoSuchElementException e2) {
                        System.out.println("Funded by not found for address: " + did);
                    }
                }


                StringBuilder content = new StringBuilder();
                content.append("\"Funded by\" = ").append(fundedBy.isEmpty() ? "N/A" : fundedBy).append(System.lineSeparator());
                content.append("\"Nametag\" = ").append(nametag.isEmpty() ? "N/A" : nametag).append(System.lineSeparator());

                String txtFilePath = pathDestFolder + "\\export-" + did + ".txt";
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilePath))) {
                    writer.write(content.toString());
                    System.out.println("Saved info to " + txtFilePath);
                } catch (IOException e) {
                    System.out.println("Error writing to file: " + txtFilePath);
                    e.printStackTrace();
                }

                try {
                    int transactions = NumberFormat.getNumberInstance(java.util.Locale.US).parse(Ntransactions).intValue();
                    System.out.println("Numero transazioni: " + transactions);
                    if (transactions == 0) {

                    } else if (transactions <= 25) {
                          WebElement button = driver.findElement(By.xpath("//button[@id='btnExportQuickTableToCSV']"));
                        System.out.println(button.getAttribute("innerHTML"));
                        button.sendKeys(Keys.ENTER);
                        new Actions(driver)
                                .click(button)
                                .perform();
                        Thread.sleep(5000);

                        File f=new File(defaultDownloadPath+"\\export-"+did+".csv");
                        System.out.println("Move "+f.getPath()+ " in "+pathDestFolder);
                        f.renameTo(new File(pathDestFolder+"\\export-"+did+".csv"));
                    } else {
                        retrieveLargeDataset(driver,did,transactions,defaultDownloadPath,pathDestFolder);
                    }
                    Thread.sleep(ThreadLocalRandom.current().nextInt(2000, 5000 + 1));
                } catch (ParseException e) {
                    System.out.println("Errore di conversione: " + Ntransactions);
                } catch (InterruptedException e) {

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        driver.close();
        driver.quit();
    }


    /**
     * Manage the retrievial of large number of transaction from etherscan
     * @param driver The Chrome driver instance
     * @param address Address to retrieve
     * @param nTx Number of transactions of the address
     * @param defaultDownaloadPath
     * @param pathDestFolder
     */
    private static void retrieveLargeDataset(WebDriver driver, String address,int nTx, String defaultDownaloadPath,String pathDestFolder) throws IOException {
        //Numero records per pagina 50, 100
        String retrivialPath="https://etherscan.io/txs?";
        String queryParameterAddress="a=";
        String queryParameterPageSize="&ps=100";
        String queryParameterPage="&p=";
        int pages= (int) Math.ceil(nTx/100.0);
        System.out.println("Numero di txn "+nTx+" pages "+pages);
        for(int p=1;p<pages+1;p++){
            if(p==1) {
                System.out.println(retrivialPath + queryParameterAddress + address + queryParameterPageSize);
                driver.get(retrivialPath + queryParameterAddress + address + queryParameterPageSize);
            }else{
                System.out.println(retrivialPath + queryParameterAddress + address + queryParameterPageSize+queryParameterPage+p);
                driver.get(retrivialPath + queryParameterAddress + address + queryParameterPageSize+queryParameterPage+p);
            }
            WebElement button = driver.findElement(By.xpath("//button[text()[contains(.,'Download Page Data')]]"));
            System.out.println(button.getAttribute("innerHTML"));
            button.sendKeys(Keys.ENTER);
            new Actions(driver)
                    .click(button)
                    .perform();
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(4000, 7000 + 1));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        FilenameFilter beginswithm = new FilenameFilter()
        {
            public boolean accept(File directory, String filename) {
                return filename.startsWith("export-transaction-list-");
            }
        };
        File dir=new File(defaultDownaloadPath);
        File[] files = dir.listFiles(beginswithm);
        System.out.println("Merge "+files.length+" files...");
        CSVWriter w=new CSVWriter(new FileWriter(new File(pathDestFolder+"\\export-"+address+".csv")));
        String[] line;
        boolean header=false;
        for(File f:files){
            try {
                CSVReader r=new CSVReader(new FileReader(f));
                if(!header){
                    line=r.readNext();
                    w.writeNext(line);
                    header=true;
                }
                while((line=r.readNext())!=null){
                    w.writeNext(line);
                }
                r.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (CsvValidationException e) {
                throw new RuntimeException(e);
            }
            f.delete();
        }
        w.close();
    }

    public static void main(String[] args) {
        // Example Ethereum address. It is possible to pass one or more DIDs, depending on purposes.
        String address = "0x3f74f0af1FA2B2308dd157c7f163307e52e7fED4";
        // Create a list of addresses to process
        ArrayList<String> addresses = new ArrayList<>();
        addresses.add(address);
        
        // Set the destination folder where CSV files will be saved
        String pathDestFolder = "C:\\Users\\user\\Desktop\\TESI SSI\\DID_transactions";
        
        // Set the default download path of your browser (Chrome default download folder)
        String defaultDownloadPath = "C:\\Users\\user\\Downloads";
        
        // Call the getTransactions method
        getTransactions(addresses, pathDestFolder, defaultDownloadPath);
    }
    
}




