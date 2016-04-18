package com.ytbot;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Comment {
    public static WebDriver driver;
    public static StringBuffer verificationErrors = new StringBuffer();

    public static void comment(String url, String comment, String username, String password) throws Exception {
        setUp();
        testComment(url, comment, username, password);
        tearDown();
    }

    @Before
    public static void setUp() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public static void testComment(String url, String comment, String username, String password) throws Exception {
        driver.manage().window().maximize();
        driver.get(url);

        int isPresent = driver.findElements(By.className("signin-container ")).size();

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

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int i = 10;

            public void run() {
                if(i == 0) {
                    Window.comment = 1;
                    timer.cancel();
                    driver.quit();
                    return;
                }

                i--;
            }
        }, 0, 1000);

        JavascriptExecutor jse = (JavascriptExecutor)driver;
        int pos = driver.manage().window().getSize().height;

        Thread.sleep(2500);

        if(driver.toString() != null) {
            jse.executeScript("window.scrollTo(0 , " + driver.manage().window().getSize().height + ")");

            while(driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).isDisplayed()) {
                pos -= 50;
                jse.executeScript("window.scrollTo(0 , " + pos + ")");

                if(driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).getSize().height > 0) {
                    WebElement commentBox = driver.findElement(By.className("comment-simplebox-renderer-collapsed-content"));
                    commentBox.click();
                    driver.findElement(By.className("comment-simplebox-text")).sendKeys(comment);
                    driver.findElement(By.className("comment-simplebox-submit")).click();
                    Window.commented = 1;
                    timer.cancel();
                    break;
                }
            }
        }
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
