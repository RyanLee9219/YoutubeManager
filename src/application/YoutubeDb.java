package application;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class YoutubeDb {
	private PreparedStatement pst;
	private ResultSet rs;
	private Connection conn;
	
	public YoutubeDb(Connection conn) {
		this.conn = conn;
		
	}
	
	public ObservableList<YoutubeData> loadData(){
		
		ObservableList<YoutubeData> tmpOv = FXCollections.observableArrayList();
		String query = "select * from Youtube";
		
		try {
			pst = conn.prepareStatement(query);
			rs = pst.executeQuery();
			
			while(rs.next()) {
				String title = rs.getString("Title");
				String url = rs.getString("Url");
				
				YoutubeData data = new YoutubeData(title, url);
				tmpOv.add(data);
			}
			
			rs.close();
			pst.close();
		} catch (Exception e) {
			System.err.println("Failed to load data");
		}
		return tmpOv;
	}
	
	public void insertData(YoutubeData data) {
		String query = "Insert or Replace into Youtube (Title,Url) Values (?,?)";
		try {
			
			pst = conn.prepareStatement(query);
			pst.setString(1, data.getTitle());
			pst.setString(2, data.getUrl());
			pst.execute();
			pst.close();
			
		}catch (Exception e) {
			System.err.println("Failed to insert data");
		}
	}
	
	public void deleteData(YoutubeData data) {
		String query = "delete from Youtube where Url = ?";
		try {
			
			pst = conn.prepareStatement(query);
			pst.setString(1, data.getUrl());
			pst.execute();
			pst.close();
			
		}catch (Exception e) {
			System.err.println("Failed to delete data");
		}
	}

}
