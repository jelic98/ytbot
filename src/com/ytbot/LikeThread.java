package com.ytbot;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LikeThread extends Thread {
    private final String proxy, url, comment, username, password;
    private Like like;
    private static boolean isRunning = true;
    public boolean done;
    private final int pos, q;
    private int finishedLikes;

    public LikeThread(int pos, int q, String proxy, String url, String comment, String username, String password) {
        this.proxy = proxy;
        this.url = url;
        this.comment = comment;
        this.username = username;
        this.password = password;
        this.pos = pos;
        this.q = q;

        like = new Like();
    }

    @Override
    public void run() {
        if(isRunning) {
            Monitor monitor = Main.monitor;

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

            int counter = monitor.getLikeCounter();
            int totalCounter = monitor.getTotalLikeCounter();

            String proxyText;

            if(!proxy.equals("0")) {
                proxyText = proxy;
            }else {
                proxyText = "No proxy";
            }

            while(!Main.runningCommentThreads.get(pos).done) {
                //wait for comment thread to finish
                System.out.println(pos + " " + Main.runningCommentThreads.get(pos).done);
            }

            finishedLikes = 0;

            for(LikeThread likeThread : Main.runningLikeThreads) {
                if(likeThread.pos == pos && likeThread.q < q && likeThread.done) {
                    finishedLikes++;
                }
            }

            Calendar cal;

            if(Main.runningCommentThreads.get(pos).done && Main.runningCommentThreads.get(pos).errorFound) {
                cal = Calendar.getInstance();

                monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxyText, "Like", "Destroyed", sdf.format(cal.getTime())});
                monitor.updateCounter(counter, totalCounter - 1, "like");

                return;
            }

            cal = Calendar.getInstance();

            monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxyText, "Like", "Started", sdf.format(cal.getTime())});
            monitor.updateCounter(counter, totalCounter, "like");

            try {
                like.like(proxy, url, comment, username, password);
            }catch(Exception e) {
                e.printStackTrace();
            }

            cal = Calendar.getInstance();

            if(like.finished == 1) {
                counter++;

                monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxyText, "Like", "Finished", sdf.format(cal.getTime())});
            }else {
                monitor.addRow(new Object[]{url + ":" + comment, username + ":" + password, proxyText, "Like", "Error", sdf.format(cal.getTime())});
            }

            monitor.updateCounter(counter, totalCounter, "like");

            kill();
        }
    }

    public void kill() {
        done = true;
        isRunning = false;
        like.kill();
    }
}
