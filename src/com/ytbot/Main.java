package com.ytbot;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class Main {
    private JPanel panel;
    private JLabel lURL, lAccount, lProxy, lDelayAccount, lUsername, lPassword, lPort, lThreads, lEcloga;
    private JTextField tfURL, tfUsername, tfPassword, tfKomentar, tfPort, tfThreads, tfDelayAccount;
    private JList listURL, listAccount, listProxy;
    private JButton bStart, bLoadAccount, bLoadProxy, bLoadURL, bLoad, bAbort, bInfo, bClearURL, bAddURL, bRemoveURL, bClearAccount, bAddAccount, bRemoveAccount, bClearProxy, bAddProxy, bRemoveProxy;
    private JCheckBox cProxy;
    private JCheckBox cLike;
    private JButton bMonitor;
    private JOptionPane optionPane = new JOptionPane();

    List<String> proxies = new ArrayList<String>();
    Map<String, String> accounts = new HashMap<String, String>();
    Map<String, String> urls = new HashMap<String, String>();

    //account
    private static int counter = 0;
    //proxy
    private static int counter2 = 0;
    //url
    private static int counter3 = 0;

    private static int videosCommented = 0;
    private static int commentsLiked = 0;

    private static int delay = 0;
    private static int threads = 1;
    private static int port = 80;

    public static int session = 0;
    public static int comment = 0;
    public static int liked = 0;
    public static int commented = 0;
    public static int started = 0;
    public static int abort = 0;

    private static boolean monitorShown = false;
    private static boolean useProxy = true;
    private static boolean useLike = true;

    public Main() {
        Monitor monitor = new Monitor();

        lEcloga.setForeground(Color.GRAY);

        bStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(tfUsername.getText().isEmpty() || tfPassword.getText().isEmpty()) {
                    Error.showError("FIll out all required fields");
                    started = 0;
                }else {
                    //get values from text fields
                    String korisnickoIme = tfUsername.getText();
                    String lozinka = tfPassword.getText();

                    if(!tfPort.getText().isEmpty() && useProxy) {
                        port = Integer.parseInt(tfPort.getText());
                    }

                    if(!tfDelayAccount.getText().isEmpty() && useLike) {
                        delay = Integer.parseInt(tfDelayAccount.getText());
                    }

                    if(!tfThreads.getText().isEmpty()) {
                        threads = Integer.parseInt(tfThreads.getText());
                    }

                    //change process status
                    started = 1;

                for(String key : urls.keySet()) {
                    if(abort == 1) {
                        optionPane.showMessageDialog(null, "Process is successfully aborted", "Success", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }

                    //initialize parameters
                    String url = key;
                    String komentar = urls.get(key);
                    boolean firstRun = true;

                    //execute comment action
                    while((comment == 1 || firstRun) && commented == 0) {
                        if(abort == 1) {
                            optionPane.showMessageDialog(null, "Process is successfully aborted", "Success", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }

                        firstRun = false;

                        //execute like action
                        try {
                            Comment.comment(url, komentar, korisnickoIme, lozinka);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                        videosCommented++;
                        Monitor.updateCounter(videosCommented, counter3, "comment", monitor.lCounterURL, monitor.lRateURL);
                    }

                    commented = 0;
                    comment = 1;

                    if(useLike && accounts.size() > 0) {
                        delay = Integer.parseInt(tfDelayAccount.getText());

                        if(useProxy && proxies.size() > 0) {
                            int brojac = 0;
                            int indexP = 0;
                            int a = accounts.size();
                            int p = proxies.size();

                            for (String accKey : accounts.keySet()) {
                                if(abort == 1) {
                                    optionPane.showMessageDialog(null, "Process is successfully aborted", "Success", JOptionPane.INFORMATION_MESSAGE);
                                    break;
                                }

                                boolean firstRunLike = true;
                                liked = 0;

                                //initialize parameters
                                String username = accKey;
                                String password = accounts.get(accKey);

                                //syncing proxies with accounts
                                brojac++;

                                if (brojac >= a / p) {
                                    counter = 0;
                                    indexP++;
                                }

                                if (indexP == p) {
                                    indexP = 0;
                                }

                                //check if user liked video
                                while ((session == 1 || firstRunLike) && liked == 0) {
                                    if(abort == 1) {
                                        optionPane.showMessageDialog(null, "Process is successfully aborted", "Success", JOptionPane.INFORMATION_MESSAGE);
                                        break;
                                    }

                                    firstRunLike = false;
                                    //execute like action
                                    try {
                                        Like.like(proxies.get(indexP), port, url, komentar, username, password);
                                    } catch (Exception e1) {
                                        e1.printStackTrace();
                                    }

                                    commentsLiked++;
                                    Monitor.updateCounter(commentsLiked, videosCommented, "like", monitor.lCounterAccount, monitor.lRateAccount);
                                }

                                //pause process by provided time
                                try {
                                    Thread.sleep(delay);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }

                            optionPane.showMessageDialog(null, "Process is successfully completed", "Success", JOptionPane.INFORMATION_MESSAGE);
                            started = 0;
                        } else {
                            Error.showError("Load required files");
                            started = 0;
                        }
                    }
                }
                }
            }
        });

        bLoadProxy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    FileInputStream fstream = new FileInputStream(String.valueOf(Paths.get(getFiles())));
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line;
                    counter2 = 0;
                    DefaultListModel listModel = new DefaultListModel();

                    while((line = br.readLine()) != null) {
                        proxies.add(line);

                        listModel.addElement(line);

                        counter2++;
                        updateLabel(counter2, lProxy);
                    }

                    listProxy.setModel(listModel);

                    in.close();
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        });

        bLoadAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateMap(accounts);
            }
        });

        bLoadURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateMap(urls);
            }
        });

        bLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //todo load previous lists
            }
        });

        bAbort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(started == 1) {
                    abort = 1;
                    optionPane.showMessageDialog(null, "Process will be aborted", "Message", JOptionPane.INFORMATION_MESSAGE);
                }else {
                    Error.showError("Process is not running");
                }
            }
        });

        bInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = "";
                s += "Instructions\n";
                s += "\n";
                s += "1. Blah blah blah";
                //todo write instructions
                optionPane.showMessageDialog(null, s, "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        cProxy.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                useProxy = !useProxy;
                changeProxy(useProxy);
            }
        });

        cLike.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                useLike = !useLike;
                changeLike(useLike);
            }
        });

        bMonitor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!monitorShown) {
                    Monitor.newScreen();
                    monitorShown = true;
                }else {
                    optionPane.showMessageDialog(null, "Monitor is already open", "Message", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    private void changeProxy(boolean value) {
        bLoadProxy.setEnabled(value);
        bClearProxy.setEnabled(value);
        bAddProxy.setEnabled(value);
        bRemoveProxy.setEnabled(value);

        lProxy.setEnabled(value);

        tfPort.setEnabled(value);
        lPort.setEnabled(value);
    }

    private void changeLike(boolean value) {
        bLoadAccount.setEnabled(value);
        bClearAccount.setEnabled(value);
        bAddAccount.setEnabled(value);
        bRemoveAccount.setEnabled(value);

        lDelayAccount.setEnabled(value);
        tfDelayAccount.setEnabled(value);

        lAccount.setEnabled(value);

        cProxy.setEnabled(value);

        if(cProxy.isSelected()) {
            changeProxy(value);
        }
    }

    private void populateMap(Map<String, String> hashMap) {
        try{
            FileInputStream fstream = new FileInputStream(String.valueOf(Paths.get(getFiles())));
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            counter = 0;
            counter3 = 0;
            DefaultListModel listModel = new DefaultListModel();

            while((line = br.readLine()) != null) {
                hashMap.put(line.substring(0, line.indexOf("~")), line.substring(line.indexOf("~") + 1));

                if(hashMap == accounts) {
                    counter++;
                    updateLabel(counter, lAccount);
                }else if(hashMap == urls) {
                    counter3++;
                    updateLabel(counter3, lURL);
                }

                listModel.addElement(line);
            }

            if(hashMap == accounts) {
                listAccount.setModel(listModel);
            }else if(hashMap == urls) {
                listURL.setModel(listModel);
            }

            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateLabel(int counter, JLabel label) {
        String s = "";

        if(label == lProxy) {
            s = "Proxy: ";
        }else if(label == lAccount) {
            s = "Account: ";
        }else if(label == lURL) {
            s = "URL: ";
        }

        label.setText(s + counter);
    }

    private String getFiles() {
        //opening file chooser
        String userDir = System.getProperty("user.home");
        JFileChooser fileChooser = new JFileChooser(userDir + "/Desktop");

        //setting allowed file extension(s)
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "*.txt";
            }
        };

        fileChooser.addChoosableFileFilter(filter);

        int result = fileChooser.showOpenDialog(this.panel);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();

            //handle non-txt file upload
            if(!path.substring(path.length() - 3).equals("txt")) {
                Error.showError("Only .txt files are allowed");
            }

            return path;
        }

        return null;
    }

    public static void main(String[] args) {
        //get screen width and height
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.getWidth() * 0.75);
        int height = (int) (screenSize.getHeight() * 0.75);

        //set up application window
        JFrame frame = new JFrame();
        frame.setTitle("YTBot");
        frame.setSize(new Dimension(width, height));
        frame.setLocation(screenSize.width / 2 - width / 2,screenSize.height / 2 - height / 2);
        frame.setContentPane(new Main().panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
