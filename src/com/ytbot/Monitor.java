package com.ytbot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Monitor {
    public JPanel panel;
    public JLabel lCounterURL;
    public JLabel lCounterAccount;
    public JLabel lRateURL;
    public JLabel lRateAccount;
    private static JTable table;
    private static DefaultTableModel model;
    private static JFrame frame = new JFrame();
    public static int commentCounter = 0;
    public static int likeCounter = 0;
    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private static int width = (int) (screenSize.getWidth() * 0.5);
    private static int height = (int) (screenSize.getHeight() * 0.75);

    public Monitor() {
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

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane);
    }

    public static void updateCounter(int x, int y, String flag, JLabel label1, JLabel label2) {
        String s1 = "";
        String s2 = "";

        float z = 100 * x / y;

        if(flag.equals("comment")) {
            commentCounter = x;

            s1 = "Videos commented: ";
            s2 = "Comment success rate: ";
        }else if(flag.equals("like")) {
            likeCounter = x;

            s1 = "Comments liked: ";
            s2 = "Comments liked: ";
        }

        s1 += String.valueOf(x);
        s1 += "/";
        s1 += String.valueOf(y);

        s2 += String.valueOf(Math.round(z));
        s2 += "%";

        label1.setText(s1);
        label2.setText(s2);
    }

    public void main(String[] args) {
        newScreen();
    }

    public void newScreen() {
        frame.setTitle("YTBot Monitor");
        frame.setSize(new Dimension(width, height));
        frame.setLocation(screenSize.width / 2 - width / 2,screenSize.height / 2 - height / 2);
        frame.setContentPane(new Monitor().panel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public void addRow(Object[] row) {
        model.addRow(row);
        table.setModel(model);
    }
}
