package com.ytbot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
public class Comment {
    private static WebDriver driver;
    private static StringBuffer verificationErrors = new StringBuffer();

    private static Calendar cal;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static int finished = 0;
    private static int runs = 0;
    private static final int RUN_LIMIT = 3;

    private static int counter = 0;

    @Before
    public static void setUp(String proxy) throws Exception {
        driver = new FirefoxDriver();

        if(!proxy.equals("0")) {
            driver = Proxy.setProxy(proxy);
        }

        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }


    public static void comment(String proxy, String url, String comment, String username, String password) throws Exception {
        setUp(proxy);

        counter = Monitor.commentCounter;

        Monitor monitor = new Monitor();

        cal = Calendar.getInstance();
        monitor.addRow(new Object[]{url + "~" + comment, username + "~" + password, "No proxy", "Comment", "Started", sdf.format(cal.getTime())});

        while(finished == 0) {
            runs++;

            if(runs < RUN_LIMIT) {
                testComment(url, comment, username, password);
            }else {
                break;
            }
        }

        cal = Calendar.getInstance();

        if(finished == 1) {
            counter++;

            monitor.addRow(new Object[]{url + "~" + comment, username + "~" + password, "No proxy", "Comment", "Finished", sdf.format(cal.getTime())});
            monitor.updateCounter(counter, Main.urls.size(), "comment", monitor.lCounterURL, monitor.lRateURL);
        }else {
            monitor.addRow(new Object[]{url + "~" + comment, username + "~" + password, "No proxy", "Comment", "Error", sdf.format(cal.getTime())});
        }

        tearDown();
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

        JavascriptExecutor jse = (JavascriptExecutor)driver;
        int pos = driver.manage().window().getSize().height;

        Thread.sleep(2500);

        if(driver.toString() != null) {
            jse.executeScript("window.scrollTo(0 , " + driver.manage().window().getSize().height  / 2 + ")");

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
                        finished = 1;
                        break;
                    }
                }else {
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
