package com.ytbot;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import static org.junit.Assert.*;

import org.openqa.jetty.html.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Like {
    public static WebDriver driver;
    public static StringBuffer verificationErrors = new StringBuffer();

    public static void like(String ip, int port, String url, String comment, String username, String password) throws Exception {
        setUp(ip, port);
        testLike(url, comment, username, password);
        tearDown();
    }

    @Before
    public static void setUp(String ip, int port) throws Exception {
        driver = Proxy.setProxy(ip, port);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public static void testLike(String url, String comment, String username, String password) throws Exception {
        int isPresent;
        JavascriptExecutor jse = (JavascriptExecutor) driver;

        driver.manage().window().maximize();
        driver.get(url);

        isPresent = driver.findElements(By.className("signin-container ")).size();

        if(isPresent > 0) {
            driver.findElement(By.className("signin-container ")).click();
            driver.findElement(By.id("Email")).clear();
            driver.findElement(By.id("Email")).sendKeys(username);
            driver.findElement(By.id("next")).click();
            driver.findElement(By.id("Passwd")).clear();
            driver.findElement(By.id("Passwd")).sendKeys(password);
            driver.findElement(By.id("PersistentCookie")).click();
            driver.findElement(By.id("signIn")).click();
        }

        int pos = driver.manage().window().getSize().getHeight() / 2;
        WebElement more;

        while(!textExists(comment)) {
            jse.executeScript("window.scrollTo(0 , " + pos + ")");
            pos += 250;

            System.out.println(textExists(comment));

            if(moreButtonSize() > 0) {
                more = driver.findElement(By.xpath("//*[@id=\"comment-section-renderer\"]/button"));
                more.click();
            }
        }

            if(textExists(comment)) {
                WebElement commentElement = driver.findElement(By.xpath("//*[contains(text(), '" + comment + "')]"));
                jse.executeScript("window.scrollTo(" + getX(commentElement) + ", " + getY(commentElement) + ")");
                WebElement commentFirstParent = commentElement.findElement(By.xpath(".."));
                WebElement commentSecondParent = commentFirstParent.findElement(By.xpath(".."));
                WebElement likeParent = commentSecondParent.findElement(By.className("comment-renderer-footer"));
                List<WebElement> childs = likeParent.findElements(By.xpath(".//*"));

                for(WebElement element : childs) {
                    jse.executeScript("window.scrollTo(" + getX(element) + ", " + getY(element) + ")");

                    if(element.getAttribute("data-action-type") != null
                            && !element.getAttribute("data-action-type").isEmpty()) {
                        if(element.getAttribute("data-action-type").equals("like")
                                && element.getCssValue("color").equals("rgba(51, 51, 51, 1)")) {
                            element.click();
                            System.out.print("success");
                            break;
                        }
                    }
                }
            }
    }

    public static boolean textExists(String text){
        boolean b = driver.getPageSource().contains(text);
        return b;
    }

    public static int moreButtonSize(){
        int s = driver.findElements(By.xpath("//*[@id=\"comment-section-renderer\"]/button")).size();
        return s;
    }

    public static int getX(WebElement element) {
        int x = element.getLocation().x - 100;
        return x;
    }

    public static int getY(WebElement element) {
        int y = element.getLocation().y - 100;
        return y;
    }

    @After
    public static void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }
}
