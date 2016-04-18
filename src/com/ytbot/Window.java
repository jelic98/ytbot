package com.ytbot;

        import javax.swing.*;
        import javax.swing.filechooser.FileFilter;
        import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.io.*;
        import java.nio.file.*;
        import java.util.*;
        import java.util.List;

public class Window {
    private JPanel panel;
    private JLabel lblKorisnickoIme, lblLozinka, lblLinkovi, lblNalozi, lblAdrese, lblPort;
    private JTextField tfURL, tfKorisnickoIme, tfLozinka, tfKomentar, tfPort;
    private JButton btnKomentarisi, btnUcitajNaloge, btnUcitajAdrese, btnUcitajLinkove;

    Map<String, String> accounts = new HashMap<String, String>();
    List<String> proxies = new ArrayList<String>();
    Map<String, String> urls = new HashMap<String, String>();
    int counter = 0;
    int counter2 = 0;
    int counter3 = 0;

    public static int session = 0;
    public static int comment = 0;
    public static int liked, commented;

    public Window() {
        btnKomentarisi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String korisnickoIme = tfKorisnickoIme.getText();
                String lozinka = tfLozinka.getText();
                int port = Integer.parseInt(tfPort.getText());

                if(korisnickoIme.isEmpty() || lozinka.isEmpty() || port == 0) {
                    Error.showError("Popuni sva polja");
                }else {
                for(String key : urls.keySet()) {
                    //initialize parameters
                    String url = key;
                    String komentar = urls.get(key);
                    boolean firstRun = true;

                    //execute comment action
                    while((comment == 1 || firstRun) && commented == 0) {
                        firstRun = false;

                        //execute like action
                        try {
                            Comment.comment(url, komentar, korisnickoIme, lozinka);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                    commented = 0;
                    comment = 1;

                    if (accounts.size() > 0 && proxies.size() > 0 && urls.size() > 0) {
                        int brojac = 0;
                        int indexP = 0;
                        int a = accounts.size();
                        int p = proxies.size();

                        for(String accKey : accounts.keySet()) {
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
                                firstRunLike = false;
                                //execute like action
                                try {
                                    Like.like(proxies.get(indexP), port, url, komentar, username, password);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    } else {
                        Error.showError("Ucitaj neophodne fajlove");
                    }
                }
                }

                Success.showMessage("Zavrseno komentarisanje i ljakovanje");
            }
        });

        btnUcitajAdrese.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    FileInputStream fstream = new FileInputStream(String.valueOf(Paths.get(getFiles())));
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line;
                    counter2 = 0;

                    while ((line = br.readLine()) != null) {
                        proxies.add(line);

                        counter2++;
                        updateLabel(counter2, lblAdrese);
                    }

                    updateLabel(counter2, lblAdrese);

                    in.close();
                }catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        });

        btnUcitajNaloge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateMap(accounts);
            }
        });

        btnUcitajLinkove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                populateMap(urls);
            }
        });
    }

    private void populateMap(Map<String, String> hashMap) {
        try{
            FileInputStream fstream = new FileInputStream(String.valueOf(Paths.get(getFiles())));
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            counter = 0;
            counter3 = 0;

            while ((line = br.readLine()) != null) {
                hashMap.put(line.substring(0, line.indexOf("~")), line.substring(line.indexOf("~") + 1));

                if(hashMap == accounts) {
                    counter++;
                    updateLabel(counter, lblNalozi);
                }else if(hashMap == urls) {
                    counter3++;
                    updateLabel(counter3, lblLinkovi);
                }
            }

            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateLabel(int counter, JLabel label) {
        String s = "";

        if(label == lblAdrese) {
            s = " adresa";
        }else if(label == lblNalozi) {
            s = " naloga";
        }else if(label == lblLinkovi) {
            s = " linkova";
        }

        label.setText(counter + s);
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
                Error.showError("Otvori .txt fajl");
            }

            return path;
        }

        return null;
    }

    public static void main(String[] args) {
        //get screen width and height
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getWidth();

        //set upapplication window
        JFrame frame = new JFrame();
        frame.setTitle("ytbot");
        frame.setSize(new Dimension(width, height));
        frame.setContentPane(new Window().panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
