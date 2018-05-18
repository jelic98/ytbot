package com.ytbot;

import java.security.Key;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Like {
    private WebDriver driver;
    public int finished = 0;
    private int runs = 0;
    private static final int RUN_LIMIT = 3;
    private boolean isRunning = true;

    public void like(String proxy, String url, String comment, String username, String password) throws Exception {
        setUp();

        if(!proxy.equals("0")) {
            driver = Proxy.setProxy(proxy);
        }

        while(finished == 0) {
            runs++;

            if(runs <= RUN_LIMIT) {
                testLike(url, comment, username, password);
            }else {
                break;
            }
        }

        tearDown();
    }

    @Before
    public void setUp() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test
    public void testLike(String url, String comment, String username, String password) throws Exception {
        int isPresent;
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        WebElement more;

        driver.manage().window().maximize();
        driver.get(url);

        isPresent = driver.findElements(By.className("signin-container ")).size();

        if(isPresent > 0) {
            driver.findElement(By.className("signin-container ")).click();
            Thread.sleep(2500);
            driver.findElement(By.id("Email")).clear();
            driver.findElement(By.id("Email")).sendKeys(username);
            driver.findElement(By.id("next")).click();
            Thread.sleep(2500);
            driver.findElement(By.id("Passwd")).clear();
            driver.findElement(By.id("Passwd")).sendKeys(password);
            driver.findElement(By.id("signIn")).click();
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int i = 20;

            public void run() {
                if(i == 0) {
                    Main.session = 1;
                    timer.cancel();
                    return;
                }

                i--;
            }
        }, 0, 1000);

        if(driver.toString() != null) {
            Thread.sleep(2500);

            Long old = (Long) jse.executeScript("return window.scrollY;");
            driver.findElement(By.xpath("//body")).sendKeys(Keys.SPACE);
            Long current = (Long) jse.executeScript("return window.scrollY;");

            Thread.sleep(2500);

            while (old < current) {
                if (!isRunning) {
                    finished = 0;
                    runs = RUN_LIMIT;
                    break;
                }

                if(driver.getPageSource().contains(comment)) {
                    try {
                        if(driver.findElement(By.xpath("//*[contains(text(), '" + comment + "')]")).isDisplayed()) {
                            WebElement commentElement = driver.findElement(By.xpath("//*[contains(text(), '" + comment + "')]"));
                            jse.executeScript("window.scrollTo(" + getX(commentElement) + ", " + getY(commentElement) + ")");
                            WebElement commentFirstParent = commentElement.findElement(By.xpath(".."));
                            WebElement commentSecondParent = commentFirstParent.findElement(By.xpath(".."));
                            WebElement likeParent = commentSecondParent.findElement(By.className("comment-renderer-footer"));
                            List<WebElement> childs = likeParent.findElements(By.xpath(".//*"));

                            for (WebElement element : childs) {
                                if (!isRunning) {
                                    finished = 0;
                                    runs = RUN_LIMIT;
                                    break;
                                }

                                jse.executeScript("window.scrollTo(" + getX(element) + ", " + getY(element) + ")");

                                if (element.getAttribute("data-action-type") != null
                                        && !element.getAttribute("data-action-type").isEmpty()) {
                                    if (element.getAttribute("data-action-type").equals("like")
                                            && element.getCssValue("color").equals("rgba(51, 51, 51, 1)")) {
                                        element.click();
                                        Main.liked = 1;
                                        timer.cancel();
                                        break;
                                    }
                                }
                            }

                            break;
                        }
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                boolean bottomReached = (Boolean) jse.executeScript("if((window.innerHeight+window.scrollY)>=document.body.offsetHeight){return true;}");

                if (bottomReached) {
                    if (driver.findElements(By.xpath("//*[@id=\"comment-section-renderer\"]/button")).size() > 0) {
                        more = driver.findElement(By.xpath("//*[@id=\"comment-section-renderer\"]/button"));
                        more.click();
                    }else {
                        break;
                    }
                }

                old = (Long) jse.executeScript("return window.scrollY;");
                driver.findElement(By.xpath("//body")).sendKeys(Keys.SPACE);
                current = (Long) jse.executeScript("return window.scrollY;");
            }
        }else {
            finished = 1;
            runs = RUN_LIMIT;
        }
    }

    private int getX(WebElement element) {
        int x = element.getLocation().x - 100;
        return x;
    }

    private int getY(WebElement element) {
        int y = element.getLocation().y - 100;
        return y;
    }

    public synchronized void kill() {
        isRunning = false;
    }

    @After
    public synchronized void tearDown() throws Exception {
        driver.quit();
    }
}
