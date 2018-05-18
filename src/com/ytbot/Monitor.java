package com.ytbot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Monitor {
    public JPanel panel;
    private static JLabel lCounterURL, lCounterAccount;
    private static JTable table;
    private static DefaultTableModel model;
    private static JFrame frame = new JFrame();
    private static int commentCounter;
    private static int likeCounter;
    private static int totalCommentCounter;
    private static int totalLikeCounter;
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static int width = (int) (screenSize.getWidth() * 0.5);
    private static int height = (int) (screenSize.getHeight() * 0.75);
    private static JScrollPane scrollPane;

    public Monitor() {
        lCounterURL = new JLabel();
        lCounterAccount = new JLabel();

        totalCommentCounter = 0;
        totalLikeCounter = 0;

        commentCounter = 0;
        likeCounter = 0;

        model = new DefaultTableModel();

        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String[] columns = {"URL", "Account", "Proxy", "Action", "Status", "Time"};

        for(String value : columns) {
            model.addColumn(value);
        }

        table.setPreferredScrollableViewportSize(new Dimension(width, height));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        scrollPane = new JScrollPane(table);
    }

    public synchronized int getCommentCounter() {
       return commentCounter;
    }

    public synchronized int getLikeCounter() {
        return likeCounter;
    }

    public synchronized int getTotalCommentCounter() {
        totalCommentCounter++;
        return totalCommentCounter;
    }

    public synchronized int getTotalLikeCounter() {
        totalLikeCounter++;
        return totalLikeCounter;
    }

    public synchronized void updateCounter(int x, int y, String flag) {
        String s1 = "";

        if(flag.equals("comment")) {
            commentCounter = x;
            s1 = "Comments: ";
        }else if(flag.equals("like")) {
            likeCounter = x;
            s1 = "Likes: ";
        }

        s1 += String.valueOf(x);
        s1 += "/";
        s1 += String.valueOf(y);


        if(flag.equals("comment")) {
            lCounterURL.setText(s1);
        }else if(flag.equals("lsike")) {
            lCounterAccount.setText(s1);
        }
    }

    public void newScreen(boolean useLike, boolean cLikeEnabled) {
        lCounterURL.setText("Comments: 0/0");
        lCounterAccount.setText("Likes: 0/0");

        panel.add(lCounterURL);

        if(useLike && cLikeEnabled) {
            panel.add(lCounterAccount);
        }

        panel.add(scrollPane);

        frame.setTitle("YTBot Monitor");
        frame.setSize(new Dimension(width, height));
        frame.setLocation(screenSize.width / 2 - width / 2,screenSize.height / 2 - height / 2);
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public void addRow(Object[] row) {
        model.addRow(row);
        table.setModel(model);
    }
}
