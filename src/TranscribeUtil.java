import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TranscribeUtil {
	
	private MysqlUtil mysqlUtil = new MysqlUtil();
	private Connection conn = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	public Map<String, String> getTextContent () {
		Map<String, String> recordText = new HashMap<String, String>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select a.record_id as item_id, b.id as file_id, a.text as text " + 
									  "from element_texts a inner join files b on a.record_id = b.item_id " +
									  "where a.element_id = '86' and a.record_type = 'item' and character_length(a.text) >= 500;");
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				int itemId = resultSet.getInt("item_id");
				int fileId = resultSet.getInt("file_id");
				String text = resultSet.getString("text");
				if (text.length() >= 500)
					recordText.put(itemId + "_" + fileId, text);
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		return recordText;
	}
	
	private boolean checkFileEntry (int fileId) {
		boolean fileEntryExist = false;
		try {
			preparedStatement = conn.
					prepareStatement("select record_id from element_texts where record_id = ? and element_id = '86' " +
									 "and record_type = 'File';");
			preparedStatement.setInt(1, fileId);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next())
				fileEntryExist = true;
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		
		return fileEntryExist;
	}
	
	public void updateFileTranscription (Map<String, String> queryResult) {
		conn = mysqlUtil.getConnection("transcribe");
		try {
			for (Map.Entry<String, String> entry : queryResult.entrySet()) {
				int fileId = Integer.valueOf(entry.getKey().substring(entry.getKey().indexOf("_")+1));
				String text = entry.getValue();
				if (checkFileEntry(fileId)) {
					preparedStatement = conn.prepareStatement("update element_texts set text = ? where record_id = ? and element_id = '86' " +
															  "and record_type = 'File';");
					preparedStatement.setString(1, text);
					preparedStatement.setInt(2, fileId);
				} else {
					preparedStatement = conn.prepareStatement("insert into element_texts (record_id, record_type, element_id, html, text) "+
							  "values (?, 'File', 86, 1, ?);");
					preparedStatement.setInt(1, fileId);
					preparedStatement.setString(2, text);
				}
				preparedStatement.execute();
			}			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
	}
}
