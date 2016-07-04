package com.ytbot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Like {
    private static WebDriver driver;
    private static StringBuffer verificationErrors = new StringBuffer();

    private static int finished = 0;
    private static int runs = 0;
    private static final int RUN_LIMIT = 3;

    private static Calendar cal;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static int counter = 0;

    public static void like(String proxy, String url, String comment, String username, String password) throws Exception {
        setUp();

        if(!proxy.equals("0")) {
            driver = Proxy.setProxy(proxy);
        }else {
            proxy = "No proxy";
        }

        counter = Monitor.likeCounter;

        Monitor monitor = new Monitor();

        cal = Calendar.getInstance();
        monitor.addRow(new Object[]{url + "~" + comment, username + "~" + password, proxy, "Like", "Started", sdf.format(cal.getTime())});

        while(finished == 0) {
            runs++;

            if(runs < RUN_LIMIT) {
                testLike(url, comment, username, password);
            }else {
                break;
            }
        }

        cal = Calendar.getInstance();

        if(finished == 1) {
            counter++;

            monitor.addRow(new Object[]{url + "~" + comment, username + "~" + password, proxy, "Like", "Finished", sdf.format(cal.getTime())});
            monitor.updateCounter(counter, Main.urls.size(), "comment", monitor.lCounterURL, monitor.lRateURL);
        }else {
            monitor.addRow(new Object[]{url + "~" + comment, username + "~" + password, proxy, "Like", "Error", sdf.format(cal.getTime())});
        }

        tearDown();
    }

    @Before
    public static void setUp() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(25, TimeUnit.SECONDS);
    }

    @Test
    public static void testLike(String url, String comment, String username, String password) throws Exception {
        int isPresent;
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        WebElement more;

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

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int i = 20;

            public void run() {
                if(i == 0) {
                    Main.session = 1;
                    timer.cancel();
                    driver.quit();
                    return;
                }

                i--;
            }
        }, 0, 1000);

        int pos = driver.manage().window().getSize().height;

        Thread.sleep(2500);

        if(driver.toString() != null) {
            jse.executeScript("window.scrollTo(0 , " + driver.manage().window().getSize().height + ")");

            while(driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).isDisplayed()) {
                pos -= 50;
                jse.executeScript("window.scrollTo(0 , " + pos + ")");

                if(driver.findElement(By.className("comment-simplebox-renderer-collapsed-content")).getSize().height > 0) {
                    break;
                }
            }

            while(!textExists(comment)) {
                Long old = (Long) jse.executeScript("return window.scrollY;");
                pos += 50;
                jse.executeScript("window.scrollTo(0 , " + pos + ")");
                Long current = (Long) jse.executeScript("return window.scrollY;");

                if(current > old) {
                    if(moreButtonSize() > 0) {
                        more = driver.findElement(By.xpath("//*[@id=\"comment-section-renderer\"]/button"));
                        more.click();
                        break;
                    }
                }else {
                    break;
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
                            Main.liked = 1;
                            timer.cancel();
                            break;
                        }
                    }
                }
            }
        }
    }

    private static boolean textExists(String text){
        boolean b = driver.getPageSource().contains(text);
        return b;
    }

    private static int moreButtonSize(){
        int s = driver.findElements(By.xpath("//*[@id=\"comment-section-renderer\"]/button")).size();
        return s;
    }

    private static int getX(WebElement element) {
        int x = element.getLocation().x - 100;
        return x;
    }

    private static int getY(WebElement element) {
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
