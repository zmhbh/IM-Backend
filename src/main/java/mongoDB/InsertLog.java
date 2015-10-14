package mongoDB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import comm.PropertiesUtils;

public class InsertLog {
	public static void logHistory(String history){
		Properties p = new Properties();
		p.setProperty("lastTimestamp", history);
		OutputStream output=null;
		URL url = InsertLog.class.getClassLoader().getResource("MongoInsertLog.properties");
		try {
			output=new FileOutputStream(new File(url.toURI()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			p.store(output, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String readHistory(){
		Properties p = new Properties();

		try {
			InputStream inputStream = InsertLog.class.getClassLoader().getResourceAsStream(
					"MongoInsertLog.properties");
			p.load(inputStream);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return p.getProperty("lastTimestamp");

	}
	
}
