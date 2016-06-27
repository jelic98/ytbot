package com.ytbot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private static Calendar cal = Calendar.getInstance();
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

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
        Monitor.model.addRow(new Object[]{url + "~" + comment, username + "~" + password, "No proxy", "Comment", "Started", sdf.format(cal.getTime())});

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
                    Main.comment = 1;
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
                Long old = (Long) jse.executeScript("return window.scrollY;");
                pos -= 50;
                jse.executeScript("window.scrollTo(0 , " + pos + ")");
                Long current = (Long) jse.executeScript("return window.scrollY;");

                if(current > old) {
                    if(driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).getSize().height > 0) {
                        WebElement commentBox = driver.findElement(By.className("comment-simplebox-renderer-collapsed-content"));
                        commentBox.click();
                        driver.findElement(By.className("comment-simplebox-text")).sendKeys(comment);
                        driver.findElement(By.className("comment-simplebox-submit")).click();
                        Main.commented = 1;
                        timer.cancel();
                        break;
                    }
                }else {
                    break;
                }
            }
        }

        Monitor.model.addRow(new Object[]{url + "~" + comment, username + "~" + password, "No proxy", "Comment", "Finished", sdf.format(cal.getTime())});
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
