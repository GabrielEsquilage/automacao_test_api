package web;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class TesteGoogle {

    @Test
    public void pesquisarNoGoogle() {
        WebDriver driver = new FirefoxDriver();
        
        try {
            driver.get("https://www.google.com");
            
            WebElement campoBusca = driver.findElement(By.name("q"));
            campoBusca.sendKeys("Selenium WebDriver" + Keys.ENTER);
            
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}