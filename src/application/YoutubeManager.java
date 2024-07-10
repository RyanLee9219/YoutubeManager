package application;
	
import org.jsoup.nodes.Document;

import java.awt.Desktop;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class YoutubeManager extends Application {
	
	private Connection conn;
	@Override
	public void start(Stage primaryStage) {
		
		ListView<YoutubeData> lv = new ListView<>();
		ObservableList<YoutubeData> ov = FXCollections.observableArrayList();
		lv.setItems(ov);
		
		//db connection
		conn = dbConnector();
		ov.addAll(new YoutubeDb(conn).loadData());
		
		//text field
		TextField addTf = new TextField();
		addTf.setPromptText("YouTube Address: ");
		addTf.setPrefWidth(248);
		
		//Button Declaration
		Button loadBt = new Button("Load");
		Button addBt = new Button("Add");
		Button removeBt = new Button("Remove");
		
		
		//Button Event 
		addBt.setOnAction(e-> {
			String url = addTf.getText().trim();
			if(!url.isEmpty()) {
				
				String title = getYoutubeTitle(url);
				YoutubeData ytData = new YoutubeData(title, url);
				
				if(!title.trim().isEmpty()) {
					ov.add(ytData);
					addTf.clear();
					
					//db
					new YoutubeDb(conn).insertData(ytData);
				}
					
			}
		});
		
		removeBt.setOnAction(e-> {
			YoutubeData selected = lv.getSelectionModel().getSelectedItem();
			if(selected !=null) {
				ov.remove(selected);
				//db
				new YoutubeDb(conn).deleteData(selected);
			}
			
		});

		loadBt.setOnAction(e-> {
			YoutubeData selected = lv.getSelectionModel().getSelectedItem();
			if(selected !=null) {
				try {
				Desktop.getDesktop().browse(new URI(selected.getUrl()));
				}catch (Exception ex) {
					System.out.println("Failed to load URL");
				}
			}
	
		});
		//TextField Event
		addTf.setOnAction(e ->{
			addBt.fire();
		});
		
		//Hbox
		HBox hBox = new HBox(addTf,loadBt,addBt,removeBt);
		hBox.setSpacing(2);
		
		//VBox
		VBox vBox = new VBox(hBox,lv);
		vBox.setSpacing(2);
		vBox.setPadding(new Insets(2));
		
	
		
		
		//Scene
		Scene scene = new Scene(vBox,400,400);
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setTitle("YouTube Manager");
		primaryStage.setResizable(false);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("assets/youtube.png"))); //icon
		
		
	
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	//database 
	public Connection dbConnector() {
		try {
			
			if(conn == null) {
				Class.forName("org.sqlite.JDBC");
				conn= DriverManager.getConnection("jdbc:sqlite:sql/Database.sqlite");
				System.out.println("db connected");
				return conn;
			} else {
				return conn;
			}
		}catch (Exception e) {
			System.err.println("failed to connect to database");
			return null;
		}
	}
	
	public String getYoutubeTitle(String youtubeUrl) {
		youtubeUrl = "https://www.youtube.com/oembed?url=" + youtubeUrl +"&format=xml";
		try {
		Document document = Jsoup.connect(youtubeUrl).header("Accept-Charset", "UTF-8").get();
		Element titleElement = document.getElementsByTag("title").get(0);

		String title = titleElement.text();
		
		return title;
		}catch (Exception e) {
			System.err.println("error - get youtube title");
			return "";
		
		}
	}
}
