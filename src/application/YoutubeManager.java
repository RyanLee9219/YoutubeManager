/*
 * TODO : Need to fix duel process running alert issue.
 */

package application;

import org.jsoup.nodes.Document;
import java.awt.Desktop;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class YoutubeManager extends Application {

    private Connection conn;
    int port = 9900;

    @Override
    public void start(Stage primaryStage) {

//        checkIfRunning();

        ListView<YoutubeData> lv = new ListView<>();
        ObservableList<YoutubeData> ov = FXCollections.observableArrayList();
        lv.setItems(ov);

        FilteredList<YoutubeData> filteredList = new FilteredList<>(ov, item -> true);
        lv.setItems(filteredList);

        lv.setOnDragDetected(e -> {
            YoutubeData selected = lv.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dragboard db = lv.startDragAndDrop(TransferMode.ANY);
                ClipboardContent cc = new ClipboardContent();
                cc.putUrl(selected.getUrl());
                db.setContent(cc);
            }
        });

        lv.setOnDragOver(e -> {
            if (e.getGestureSource() != lv) {
                e.acceptTransferModes(TransferMode.ANY);
            }
        });

        lv.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (db.hasUrl()) {
                String url = db.getUrl();
                String title = getYoutubeTitle(url);
                YoutubeData data = new YoutubeData(title, url);
                ov.add(data);
                new YoutubeDb(conn).insertData(data);
            }
        });

        conn = dbConnector();
        ov.addAll(new YoutubeDb(conn).loadData());

        TextField addTf = new TextField();
        addTf.setPromptText("YouTube Address: ");
        addTf.setPrefWidth(248);

        TextField searchTf = new TextField();
        searchTf.setPromptText("Search");
        searchTf.setPrefWidth(330);

        CheckBox chromeCb = new CheckBox("Chrome");

        Button loadBt = new Button("Load");
        Button addBt = new Button("Add");
        Button removeBt = new Button("Remove");

        addBt.setOnAction(e -> {
            String url = addTf.getText().trim();
            if (!url.isEmpty()) {
                String title = getYoutubeTitle(url);
                YoutubeData ytData = new YoutubeData(title, url);
                if (!title.trim().isEmpty()) {
                    ov.add(ytData);
                    addTf.clear();
                    new YoutubeDb(conn).insertData(ytData);
                }
            }
        });

        removeBt.setOnAction(e -> {
            YoutubeData selected = lv.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ov.remove(selected);
                new YoutubeDb(conn).deleteData(selected);
            }
        });

        loadBt.setOnAction(e -> {
            YoutubeData selected = lv.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    if (chromeCb.isSelected()) {
                        String chromePath = "C:/Program Files/Google/Chrome/Application/chrome.exe";
                        new ProcessBuilder(chromePath, selected.getUrl()).start();
                    } else {
                        Desktop.getDesktop().browse(new URI(selected.getUrl()));
                    }
                } catch (Exception ex) {
                    System.out.println("Failed to load URL");
                }
            }
        });

        loadBt.setFocusTraversable(false);
        addBt.setFocusTraversable(false);
        removeBt.setFocusTraversable(false);
        chromeCb.setFocusTraversable(false);

        addTf.setOnAction(e -> addBt.fire());

        searchTf.textProperty().addListener((a, oldV, newV) -> {
            String search = searchTf.getText().toLowerCase().trim();
            filteredList.setPredicate(item -> item.getTitle().toLowerCase().contains(search));
        });

        HBox hBox = new HBox(addTf, loadBt, addBt, removeBt);
        hBox.setSpacing(2);

        HBox hBox2 = new HBox(searchTf, chromeCb);
        hBox2.setSpacing(2);
        hBox2.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(hBox, hBox2, lv);
        vBox.setSpacing(2);
        vBox.setPadding(new Insets(2));

        Scene scene = new Scene(vBox, 400, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("YouTube Manager");
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("assets/youtube.png")));
    }

    public static void main(String[] args) {
        launch(args);
    }
/*
 * Need to fix this function
    public void checkIfRunning() {
        try {
            ServerSocket socket = new ServerSocket(port, 0, InetAddress.getByAddress(new byte[] {127,0,0,1}));
            System.out.println("port9900");
            
        } catch (Exception e) {
            System.out.println("Process is running");
            Alert alert = new Alert(AlertType.INFORMATION);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            // stage.getIcons().add(new Image(getClass().getResourceAsStream("assets/warning.png")));

            alert.setTitle("Warning!");
            alert.setHeaderText("Youtube Manager is running already");

            alert.showAndWait();

            System.exit(1);
        }
    }
*/

    public Connection dbConnector() {
        try {
            if (conn == null) {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:sql/Database.sqlite");
                System.out.println("db connected");
                return conn;
            } else {
                return conn;
            }
        } catch (Exception e) {
            System.err.println("failed to connect to database");
            return null;
        }
    }

    public String getYoutubeTitle(String youtubeUrl) {
        youtubeUrl = "https://www.youtube.com/oembed?url=" + youtubeUrl + "&format=xml";
        try {
            Document document = Jsoup.connect(youtubeUrl).header("Accept-Charset", "UTF-8").get();
            Element titleElement = document.getElementsByTag("title").get(0);
            return titleElement.text();
        } catch (Exception e) {
            System.err.println("error - get youtube title");
            return "";
        }
    }
}
