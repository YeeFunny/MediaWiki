package edu.wm.library.lyf;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DecodeUtil {
	
	private MysqlUtil mysqlUtil = new MysqlUtil();
	private Connection conn = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	public void decodeWindowsCode () {
		Map<Integer, String> textContent = getTextContent();
		for (Map.Entry<Integer, String> entry : textContent.entrySet()) {
			int id = entry.getKey();
			String text = entry.getValue();
			try {
				text = URLEncoder.encode(text, "cp1252");
			} catch (UnsupportedEncodingException encodingException) {
				System.out.println("Encoding Exception. ID: " + id);
				encodingException.printStackTrace();
			}
			try {
				text = URLDecoder.decode(text, "utf8");
			} catch (UnsupportedEncodingException decodingException) {
				System.out.println("Decoding Exception. ID: " + id);
				decodingException.printStackTrace();
			}
			textContent.replace(id, text);
		}
		updateTextContent(textContent);
	}
	
	private Map<Integer, String> getTextContent() {
		Map<Integer, String> textContent = new HashMap<Integer, String>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select id, text from element_texts where element_id in ('1','7','41','46','47','48','49','50','86');");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
				textContent.put(resultSet.getInt("id"), resultSet.getString("text").trim());
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		
		return textContent;
	}
	
	private void updateTextContent(Map<Integer, String> textContent) {
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("update element_texts set text = ? where id = ?;");
			for (Map.Entry<Integer, String> entry : textContent.entrySet()) {
				preparedStatement.setString(1, entry.getValue().trim());
				preparedStatement.setInt(2, entry.getKey());
				preparedStatement.execute();
			}
			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
	}
}
