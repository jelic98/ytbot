package com.ytbot;

        import javax.swing.*;
        import javax.swing.filechooser.FileFilter;
        import java.awt.*;
        import java.awt.event.ActionEvent;
        import java.awt.event.ActionListener;
        import java.io.*;
        import java.nio.charset.Charset;
        import java.nio.file.*;
        import java.util.*;
        import java.util.List;

public class Window {
    private JPanel panel;
    private JLabel lblKorisnickoIme, lblLozinka, lblURL, lblKomentar, lblNalozi, lblAdrese, lblPort;
    private JTextField tfURL, tfKorisnickoIme, tfLozinka, tfKomentar, tfPort;
    private JButton btnKomentarisi, btnUcitajNaloge, btnLajkuj, btnUcitajAdrese;

    Map<String, String> accounts = new HashMap<String, String>();
    List<String> proxies = new ArrayList<String>();
    int counter = 0;
    int counter2 = 0;

    public static int session = 0;
    public static int liked;

    public Window() {
        btnKomentarisi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //initialize parameters
                String korisnickoIme = tfKorisnickoIme.getText();
                String lozinka = tfLozinka.getText();
                String url = tfURL.getText();
                String komentar = tfKomentar.getText();

                //handle inputs from necessary fields
                if(korisnickoIme.isEmpty() || lozinka.isEmpty() || url.isEmpty() || komentar.isEmpty()) {
                    Error.showError("Popuni sva polja");
                }else {
                    //execute comment action
                    try {
                        Comment.comment(url, komentar, korisnickoIme, lozinka);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        btnLajkuj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(counter > 0 && counter2 > 0) {
                    int brojac = 0;
                    int indexP = 0;
                    int a = accounts.size();
                    int p = proxies.size();

                    for(String key : accounts.keySet()) {
                        boolean firstRun = true;
                        liked = 0;

                        //initialize parameters
                        String username = key;
                        String password = accounts.get(key);
                        String url = tfURL.getText();
                        String komentar = tfKomentar.getText();
                        int port = Integer.parseInt(tfPort.getText());

                        brojac++;

                        if (brojac >= a / p) {
                            counter = 0;
                            indexP++;
                        }

                        if (indexP == p) {
                            indexP = 0;
                        }

                        while((session == 1 || firstRun) && liked == 0) {
                            firstRun = false;

                            //handle inputs from necessary fields
                            if (url.isEmpty() || komentar.isEmpty()) {
                                Error.showError("Popuni neophodna polja");
                            } else {
                                //execute like action
                                try {
                                    Like.like(proxies.get(indexP), port, url, komentar, username, password);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }

                    Success.showMessage("Lajkovanje je zavrseno");
                    }else {
                    Error.showError("Ucitaj neophodne fajlove");
                }
            }
        });

        btnUcitajAdrese.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get path of uploaded file
                Path path = Paths.get(getFiles());
                List<String> lines = null;

                //store lines as elements in List
                try {
                    lines = Files.readAllLines(path, Charset.defaultCharset());
                }catch (IOException e1) {
                    e1.printStackTrace();
                }

                //extract proxies and increase proxy counter
                for(String line : lines) {
                    proxies.add(line);
                    counter2++;
                }

                //update proxy counter label
                updateLabel(counter2, lblAdrese);
            }
        });

        btnUcitajNaloge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get path of uploaded file
                Path path = Paths.get(getFiles());
                List<String> lines = null;

                //store lines as elements in HashMap
                try {
                    lines = Files.readAllLines(path, Charset.defaultCharset());
                }catch (IOException e1) {
                    e1.printStackTrace();
                }

                //extract accounts and increase account counter
                for(String line : lines) {
                    accounts.put(line.substring(0, line.indexOf("~")), line.substring(line.indexOf("~") + 1));
                    counter++;
                }

                //update account counter label
                updateLabel(counter, lblNalozi);
            }
        });
    }

    private void updateLabel(int counter, JLabel label) {
        String s = "";

        if(label == lblAdrese) {
            s = " adresa";
        }else if(label == lblNalozi) {
            s = " naloga";
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
