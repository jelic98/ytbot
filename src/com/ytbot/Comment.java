package com.ytbot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;

import org.junit.internal.runners.rules.RuleMemberValidator;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Comment {
    private WebDriver driver;
    private StringBuffer verificationErrors = new StringBuffer();

    private Calendar cal;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private int finished = 0;
    private int runs = 0;
    private final int RUN_LIMIT = 3;

    private int counter = 0;

    private static Monitor monitor;

    public boolean isRunning;

    @Before
    public void setUp() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(25, TimeUnit.SECONDS);
    }


    public void comment(String proxy, String url, String comment, String username, String password) throws Exception {
        setUp();

        if(!proxy.equals("0")) {
            driver = Proxy.setProxy(proxy);
        }else {
            proxy = "No proxy";
        }

        monitor = Main.monitor;

        cal = Calendar.getInstance();
        monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxy, "Comment", "Started", sdf.format(cal.getTime())});

        while(finished == 0) {
            runs++;

            if(runs < RUN_LIMIT) {
                testComment(url, comment, username, password);
            }else {
                break;
            }
        }

        cal = Calendar.getInstance();

        counter = monitor.getCommentCounter();

        if(finished == 1) {
            counter++;

            monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxy, "Comment", "Finished", sdf.format(cal.getTime())});
        }else {
            monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxy, "Comment", "Error", sdf.format(cal.getTime())});
        }

        monitor.updateCounter(counter, Main.urls.size(), "comment");

        tearDown();
    }

    @Test
    public void testComment(String url, String comment, String username, String password) throws Exception {
        driver.manage().window().maximize();
        driver.get(url);

        int isPresent = driver.findElements(By.className("signin-container ")).size();

        if(isPresent > 0) {
            zoomOut(3);

            driver.findElement(By.className("signin-container ")).click();
            driver.findElement(By.id("Email")).clear();
            driver.findElement(By.id("Email")).sendKeys(username);
            driver.findElement(By.id("next")).click();
            Thread.sleep(1000);
            driver.findElement(By.id("Passwd")).clear();
            driver.findElement(By.id("Passwd")).sendKeys(password);
            driver.findElement(By.id("PersistentCookie")).click();
            driver.findElement(By.id("signIn")).click();
        }

        JavascriptExecutor jse = (JavascriptExecutor)driver;
        int pos = driver.manage().window().getSize().height;

        Thread.sleep(1000);

        if(driver.toString() != null) {
            jse.executeScript("window.scrollTo(0 , " + driver.manage().window().getSize().height + ")");

            Thread.sleep(2500);

            zoomOut(3);

            while(driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).isDisplayed()) {
                if(!isRunning) {
                    finished = 0;
                    runs = RUN_LIMIT;
                    break;
                }

                Long old = (Long) jse.executeScript("return window.scrollY;");
                pos -= 50;
                jse.executeScript("window.scrollTo(0 , " + pos + ")");
                Long current = (Long) jse.executeScript("return window.scrollY;");

                if(current > old) {
                    if(driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).getSize().height > 0) {
                        driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).click();
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

    public void zoomOut(int level) {
        for(int i = 0; i < level; i++) {
            driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, Keys.SUBTRACT));
        }
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if(!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }
}
