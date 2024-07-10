module YouTubeManager {
	requires javafx.controls;
	requires javafx.graphics;
	requires java.desktop;
	requires org.jsoup;
	requires java.sql;
	
	opens application to javafx.graphics, javafx.fxml;
}
