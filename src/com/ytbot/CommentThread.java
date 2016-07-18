package com.ytbot;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CommentThread extends Thread {
    private final String proxy, url, comment, username, password;
    public boolean isRunning = true;
    private Comment com;
    public boolean done, errorFound;
    private final int pos;

    public CommentThread(int pos, String proxy, String url, String comment, String username, String password) {
        this.proxy = proxy;
        this.url = url;
        this.comment = comment;
        this.username = username;
        this.password = password;
        this.pos = pos;

        com = new Comment();
    }

    @Override
    public void run() {
        if(isRunning) {
            done = false;

            Monitor monitor = Main.monitor;

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

            int counter = monitor.getCommentCounter();
            int totalCounter = monitor.getTotalCommentCounter();

            String proxyText;

            if(!proxy.equals("0")) {
                proxyText = proxy;
            }else {
                proxyText = "No proxy";
            }

            if(pos >= Main.threads) {
                while(true) {//!Main.runningCommentThreads.get(pos - Main.threads).done) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            Calendar cal = Calendar.getInstance();

            monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxyText, "Comment", "Started", sdf.format(cal.getTime())});
            monitor.updateCounter(counter, totalCounter, "comment");

            try {
                com.comment(proxy, url, comment, username, password);
            }catch(Exception e) {
                e.printStackTrace();
            }

            cal = Calendar.getInstance();

            if(com.finished == 1) {
                counter++;

                monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxyText, "Comment", "Finished", sdf.format(cal.getTime())});
            }else {
                errorFound = true;

                monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxyText, "Comment", "Error", sdf.format(cal.getTime())});
            }

            monitor.updateCounter(counter, totalCounter, "comment");

            kill();
        }
    }

    public synchronized void kill() {
        done = true;
        isRunning = false;
        com.kill();
    }
}
