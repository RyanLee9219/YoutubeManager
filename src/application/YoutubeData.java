package application;

public class YoutubeData {
	private String title;
	private String url;
	
	public YoutubeData(String title, String url) {
		this.title = title;
		this.url = url;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	@Override
	public String toString() {
		return title;
		
	}

}
