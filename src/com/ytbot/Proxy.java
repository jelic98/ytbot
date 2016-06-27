package com.ytbot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class Proxy {
    public static WebDriver setProxy(String ip, int port) {
        FirefoxProfile profile = new FirefoxProfile();

        profile.setPreference("network.proxy.type", 1);
        profile.setPreference("network.proxy.http", ip);
        profile.setPreference("network.proxy.http_port", port);

        return new FirefoxDriver(profile);
    }
}
