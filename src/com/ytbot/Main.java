package com.ytbot;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private JLabel lURL, lAccount, lProxy, lUsername, lPassword, lThreads, lEcloga;
    private JTextField tfURL, tfUsername, tfPassword, tfKomentar, tfThreads;
    private JList listURL, listAccount, listProxy;
    private JButton bSave, bStart, bLoadAccount, bLoadProxy, bLoadURL, bLoad, bInfo, bClearURL, bAddURL, bRemoveURL, bClearAccount, bAddAccount, bRemoveAccount, bClearProxy, bAddProxy, bRemoveProxy;
    private JCheckBox cProxy, cLike;
    private JScrollPane scrollProxy, scrollAccount, scrollURL;

    JTextField[] fields = {tfUsername, tfPassword, tfThreads};
    JCheckBox[] checks = {cLike, cProxy};

    public static List<String> proxies = new ArrayList<String>();
    public static Map<String, String> accounts = new LinkedHashMap<String, String>();
    public static Map<String, String> urls = new LinkedHashMap<String, String>();

    public static String dirName = System.getProperty("user.home") + "/ytbot";

    DefaultListModel listModel = new DefaultListModel();

    //account
    private static int counter = 0;
    //proxy
    private static int counter2 = 0;
    //url
    private static int counter3 = 0;

    private static int threads = 1;

    public static int session = 0;
    public static int liked = 0;
    public static int started = 0;

    private static boolean useProxy = true;
    private static boolean useLike = true;

    public Main() {
        Monitor monitor = new Monitor();

        createFolder();

        lEcloga.setForeground(Color.GRAY);

        listURL.setModel(new DefaultListModel());
        listAccount.setModel(new DefaultListModel());
        listProxy.setModel(new DefaultListModel());

        bStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(tfUsername.getText().isEmpty() || tfPassword.getText().isEmpty()) {
                    Error.showError("Username and password are required");
                    started = 0;
                }else {
                    //if first element if 0 then do not use proxy
                    if(useProxy && cProxy.isEnabled()) {
                        proxies.add("0");
                    }

                    if(urls.size() > 0) {
                        //check if user wants to like comment but have not loaded accounts
                        if(!(accounts.size() > 0) && useLike && cLike.isEnabled()) {
                            Error.showError("Accounts are required");
                            started = 0;

                            return;
                        }

                        //check if user wants to use proxies but have not loaded proxies
                        if(!(proxies.size() > 0) && useProxy && cProxy.isEnabled()) {
                            Error.showError("Proxies are required");
                            started = 0;

                            return;
                        }

                        //get values from text fields
                        String korisnickoIme = tfUsername.getText();
                        String lozinka = tfPassword.getText();

                        try {
                            threads = Math.abs(Integer.parseInt(tfThreads.getText()));
                        }
                        catch(Exception e1) {
                            Error.showError("Number of threads must be an integer");
                        }

                        //start process logging
                        monitor.newScreen();

                        //change process status
                        started = 1;
                        session = 0;
                        liked = 0;

                        int i = 0;
                        int j = 0;

                        for(String key : urls.keySet()) {
                            System.out.println(key);
                        }

                        while(i < urls.size() / threads) {
                            String key = urls.get(i);
                        //for(String key : urls.keySet()) {
                            //initialize parameters
                            String url = key;
                            String komentar = urls.get(key);

                            //execute comment action
                                int brojac = 0;
                                int indexP = 0;
                                int a = accounts.size();
                                int p = proxies.size();

                                //binding proxies with accounts
                                if(useProxy && cProxy.isEnabled()) {
                                    brojac++;

                                    if (brojac >= a / p) {
                                        counter = 0;
                                        indexP++;
                                    }

                                    if (indexP == p) {
                                        indexP = 0;
                                    }

                                }else {
                                    indexP = 0;
                                }

                                while(j < threads) {
                                    CommentThread thread = new CommentThread(proxies.get(indexP), url, komentar, korisnickoIme, lozinka);
                                    thread.start();
                                    j++;
                                }

                            //execute like action
                            if(useLike && cLike.isEnabled()) {
                                brojac = 0;
                                indexP = 0;
                                a = accounts.size();
                                p = proxies.size();

                                for (String accKey : accounts.keySet()) {
                                    boolean firstRunLike = true;
                                    liked = 0;

                                    //initialize parameters
                                    String username = accKey;
                                    String password = accounts.get(accKey);

                                    //binding proxies with accounts
                                    if(useProxy && cProxy.isEnabled()) {
                                        brojac++;

                                        if (brojac >= a / p) {
                                            counter = 0;
                                            indexP++;
                                        }

                                        if (indexP == p) {
                                            indexP = 0;
                                        }

                                    }else {
                                        indexP = 0;
                                    }

                                    //check if user liked video
                                    while((session == 1 || firstRunLike) && liked == 0) {
                                        firstRunLike = false;

                                        //execute like action
                                        i = 0;

                                        while(i < threads) {
                                            LikeThread thread = new LikeThread(proxies.get(indexP), url, komentar, username, password);
                                            thread.start();
                                            i++;
                                        }
                                    }
                                }

                                JOptionPane.showMessageDialog(null, "Process is successfully completed", "Success", JOptionPane.INFORMATION_MESSAGE);
                                started = 0;
                            }

                            i++;
                        }

                        started = 0;
                    }else {
                        Error.showError("URLs are required");
                        started = 0;
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
                    proxies.clear();

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
                loadMap(urls, "url.txt");
                loadMap(accounts, "account.txt");
                loadList();
                loadFields();
            }
        });

        bInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = "";
                s += "YTBot v2.0\n";
                s += "Mozilla Firefox(version<47) is required\n";
                s += "Visit ecloga.org/ytbot for more information";
                //todo write instructions
                JOptionPane.showMessageDialog(null, s, "Info", JOptionPane.INFORMATION_MESSAGE);
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

        bClearURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                listURL.setModel(listModel);
                lURL.setText("URL: 0");
                urls.clear();
            }
        });

        bClearAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                listAccount.setModel(listModel);
                lAccount.setText("Account: 0");
                accounts.clear();
            }
        });

        bClearProxy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.removeAllElements();
                listProxy.setModel(listModel);
                lProxy.setText("Proxy: 0");
                proxies.clear();
            }
        });

        bRemoveURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) listURL.getModel();
                int selectedIndex = listURL.getSelectedIndex();
                String line = listURL.getSelectedValue().toString();

                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                    urls.remove(line.substring(0, line.indexOf(":")));
                }
            }
        });

        bRemoveAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) listAccount.getModel();
                int selectedIndex = listAccount.getSelectedIndex();
                String line = listAccount.getSelectedValue().toString();

                if (selectedIndex != -1) {
                    model.remove(selectedIndex);
                    accounts.remove(line.substring(0, line.indexOf(":")));
                }
            }
        });

        bRemoveProxy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel model = (DefaultListModel) listProxy.getModel();
                int selectedIndex = listProxy.getSelectedIndex();
                String line = listProxy.getSelectedValue().toString();

                if(selectedIndex != -1) {
                    model.remove(selectedIndex);
                    proxies.remove(proxies.indexOf(line));
                }
            }
        });

        bAddURL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(null, "Add URL");

                if(name != null && !name.isEmpty()) {
                    if(!name.contains(":")) {
                        Error.showError("Format: URL:COMMENT");
                    }else {
                        name = name.replace("https://www.youtube.com/watch?v=", "");

                        DefaultListModel model = (DefaultListModel) listURL.getModel();
                        model.addElement(name);

                        urls.put(name.substring(0, name.indexOf(":")), name.substring(name.indexOf(":") + 1));
                    }
                }
            }
        });

        bAddAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(null, "Add account");

                if(name != null && !name.isEmpty()) {
                    if(!name.contains(":")) {
                        Error.showError("Format: USERNAME:PASSWORD");
                    }else {
                        DefaultListModel model = (DefaultListModel) listAccount.getModel();
                        model.addElement(name);

                        accounts.put(name.substring(0, name.indexOf(":")), name.substring(name.indexOf(":") + 1));
                    }
                }
            }
        });

        bAddProxy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(null, "Add proxy");

                if(name != null && !name.isEmpty()) {
                    if(!name.contains(":")) {
                        Error.showError("Format: IP:PORT@USERNAME:PASSWORD");
                    }else {
                        DefaultListModel model = (DefaultListModel) listProxy.getModel();
                        model.addElement(name);

                        proxies.add(name);
                    }
                }
            }
        });

        bSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFieldsToFile();
                saveListToFile("proxy.txt", proxies);
                saveMapToFile("url.txt", urls);
                saveMapToFile("account.txt", accounts);
            }
        });
    }

    private void loadMap(Map<String, String> hashMap, String fileName) {
        try{
            FileInputStream fs = new FileInputStream(dirName + "/" + fileName);
            DataInputStream in = new DataInputStream(fs);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            counter = 0;
            counter3 = 0;
            DefaultListModel listModel = new DefaultListModel();

            while((line = br.readLine()) != null) {
                if(hashMap == urls) {
                    line = line.replace("https://www.youtube.com/watch?v=", "");
                }

                hashMap.put(line.substring(0, line.indexOf(":")), line.substring(line.indexOf(":") + 1));

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

    private void loadList() {
        File f = new File(dirName + "/proxy.txt");

        try {
            FileInputStream fs = new FileInputStream(f);
            DataInputStream in = new DataInputStream(fs);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeProxy(boolean value) {
        bLoadProxy.setEnabled(value);
        bClearProxy.setEnabled(value);
        bAddProxy.setEnabled(value);
        bRemoveProxy.setEnabled(value);

        lProxy.setEnabled(value);
        scrollProxy.setEnabled(value);
        listProxy.setEnabled(value);
    }

    private void changeLike(boolean value) {
        bLoadAccount.setEnabled(value);
        bClearAccount.setEnabled(value);
        bAddAccount.setEnabled(value);
        bRemoveAccount.setEnabled(value);

        lAccount.setEnabled(value);
        scrollAccount.setEnabled(value);
        listAccount.setEnabled(value);
    }

    private void createFolder() {
        File theDir = new File(dirName);

        if(!theDir.exists()) {
            try{
                theDir.mkdir();
            }
            catch(SecurityException se){
                se.printStackTrace();
            }
        }
    }

    private void saveMapToFile(String fileName, Map<String, String> map) {
        try {
            PrintWriter w = new PrintWriter(dirName + "/" + fileName, "UTF-8");

            for(String line : map.keySet()) {
                w.println(line + ":" + map.get(line));
            }

            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void saveListToFile(String fileName, List<String> list) {
        try {
            PrintWriter w = new PrintWriter(dirName + "/" + fileName, "UTF-8");

            for(String line : list) {
                w.println(line);
            }

            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void saveFieldsToFile() {
        try {
            PrintWriter w = new PrintWriter(dirName + "/field.txt", "UTF-8");

            for(JTextField field : fields) {
                w.println(field.getText());
            }

            for(JCheckBox check : checks) {
                w.println(check.isSelected());
            }

            w.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void loadFields() {
        try{
            FileInputStream fs = new FileInputStream(dirName + "/field.txt");
            DataInputStream in = new DataInputStream(fs);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            int i = 0;

            while((line = br.readLine()) != null) {
                i++;

                if(i <= 3) {
                    fields[i - 1].setText(line);
                }else {
                    checks[i - 4].setSelected(Boolean.valueOf(line));
                }
            }

            in.close();
        }catch (Exception e){
            e.printStackTrace();
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

            hashMap.clear();

            while((line = br.readLine()) != null) {
                hashMap.put(line.substring(0, line.indexOf(":")), line.substring(line.indexOf(":") + 1));

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
