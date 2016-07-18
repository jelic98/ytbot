package com.ytbot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

import java.util.HashMap;
import java.util.Map;

public class Proxy {
    public static WebDriver setProxy(String proxy) {
        FirefoxProfile profile = new FirefoxProfile();

        Map<String, String> details = getProxyDetails(proxy);

        profile.setPreference("network.proxy.type", 1);
        profile.setPreference("network.proxy.http", details.get("ip"));
        profile.setPreference("network.proxy.http_port", details.get("port"));

        return new FirefoxDriver(profile);
    }

    public static Map<String, String> getProxyDetails(String s) {
        Map<String, String> proxyDetails = new HashMap<String, String>();

        String ip = s.substring(0, s.indexOf(":"));
        String port = s.substring(s.indexOf(":") + 1);

        proxyDetails.put("ip", ip);
        proxyDetails.put("port", port);

        return proxyDetails;
    }
}
