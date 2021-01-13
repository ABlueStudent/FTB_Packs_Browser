import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Vector;

public class MainFrame extends JFrame {
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu menu = new JMenu("Menu");
    private final JMenuItem exit = new JMenuItem("Exit");
    private final JMenu help = new JMenu("Help");
    private final JMenuItem about = new JMenuItem("About");
    private final JList packList = new JList<String>();
    private final JScrollPane packPane = new JScrollPane(packList);
    private final JPanel display = new JPanel();
    private final JLabel image = new JLabel();
    private final JLabel info = new JLabel();
    private JsonObject[] packInfo = null;
    private ImageIcon[] iconBuf = null;

    public MainFrame() {
        this.setTitle("Feed-the-Beast Modpack Browser");
        this.setSize(640, 480);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setJMenuBar(menuBar);
        this.setVisible(true);
    }

    public void build() {
        this.add(packPane, BorderLayout.WEST);
        this.menuBar.add(menu);
        this.menu.add(exit);
        this.menuBar.add(help);
        this.help.add(about);
        this.add(display, BorderLayout.CENTER);

        display.setLayout(new GridLayout(2, 1));
        display.add(image);
        display.add(info);

        exit.addActionListener(actionEvent -> System.exit(0));

        about.addActionListener(actionEvent -> {
            JDialog dialog = new JDialog();
            dialog.setTitle("About");
            dialog.setSize(360, 100);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setLayout(new BorderLayout());
            dialog.add(new JLabel("only for final homework. \nauthor: Min-Lin Yu"));
            dialog.setVisible(true);
        });

        //交作業用醜Code 開一個Worker Thread避免卡住UI Thread
        //Generate packList.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FetchFTB ftb = new FetchFTB();
                    JsonArray packList = ftb.packList(); //醜變數名，包列表 [JsonElement,JsonElement,JsonElement...]
                    JsonObject[] packInfo = new JsonObject[packList.size()]; //醜變數名，真的用來存包資料。
                    Vector nameList = new Vector<String>(); //生來給JList吃的東西
                    ImageIcon[] iconBuf = new ImageIcon[packList.size()];

                    int i = 0;
                    for (JsonElement packCode :
                            packList) {
                        JsonObject info = ftb.packInfo(packCode.getAsInt());
                        packInfo[i] = info;
                        iconBuf[i] = new ImageIcon(
                                new URL(
                                        info.get("art")
                                                .getAsJsonArray().get(0).getAsJsonObject()
                                                .get("url").getAsString()
                                ));
                        nameList.add(info.get("name").getAsString());
                        MainFrame.this.packList.setListData(nameList);
                        ++i;
                    }

                    SwingUtilities.invokeAndWait(new Runnable() { //對UI執行序 執行序安全
                        @Override
                        public void run() {
                            MainFrame.this.packInfo = packInfo;
                            MainFrame.this.iconBuf = iconBuf;
                        }
                    });
                } catch (IOException | InterruptedException | InvocationTargetException ignored) {
                }
            }
        }).run();

        // List Selection Listener
        packList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //右邊顯示的東西，各種拼裝。
                        JsonObject pack = packInfo[packList.getSelectedIndex()];

                        //文字介紹
                        String reducedInfo = "<html>";
                        reducedInfo += "<h1>" + pack.get("name").getAsString() + "</h1><br>";

                        reducedInfo += "<b>Tags:</b>";
                        JsonArray tags = pack.get("tags").getAsJsonArray();
                        for (JsonElement item : tags) {
                            if (item == tags.get(tags.size() - 1)) {
                                reducedInfo += "<b>" + item.getAsJsonObject().get("name").getAsString() + "</b>";
                            } else
                                reducedInfo += "<b>" + item.getAsJsonObject().get("name").getAsString() + ", " + "</b>";
                        }
                        reducedInfo += "<br>";

                        reducedInfo += "<b>" + "Authors:" + pack.get("authors").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString() + "</b>";
                        reducedInfo += "<p>" + pack.get("description").getAsString() + "</p>";
                        reducedInfo += "</html>";
                        info.setText(reducedInfo);

                        //圖片預覽
                        image.setIcon(new ImageIcon(iconBuf[packList.getSelectedIndex()].getImage().getScaledInstance(MainFrame.this.getHeight() / 2, MainFrame.this.getHeight() / 2, 0)));
                    }
                }).start();
            }
        });
    }
}