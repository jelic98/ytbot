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
        String s1 = s.substring(0, s.indexOf("@"));
        String s2 = s.substring(s.indexOf("@") + 1, s.length());

        Map<String, String> proxyDetails = new HashMap<String, String>();

        String ip = s1.substring(0, s1.indexOf(":"));
        String port = s1.substring(s1.indexOf(":") + 1);
        String username = s2.substring(0, s2.indexOf(":"));
        String password = s2.substring(s2.indexOf(":") + 1);

        proxyDetails.put("ip", ip);
        proxyDetails.put("port", port);
        proxyDetails.put("username", username);
        proxyDetails.put("password", password);

        return proxyDetails;
    }
}
