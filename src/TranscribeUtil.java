import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranscribeUtil {
	
	private MysqlUtil mysqlUtil = new MysqlUtil();
	private Connection conn = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	private Map<Integer, List<Integer>> getIdsOfFileEntry () {
		List<Integer> fileIds = new ArrayList<Integer>();
		Map<Integer, List<Integer>> fileEntryIds = new HashMap<Integer, List<Integer>>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select f.item_id, e.record_id from element_texts e inner join files f on e.record_id = f.id " + 
									  "where e.element_id = '86' and e.record_type = 'File' and f.item_id != '3' order by f.item_id;");
			resultSet = preparedStatement.executeQuery();
			int itemId = 0;
			while (resultSet.first()) {
				if (itemId == 0) {
					itemId = resultSet.getInt(1);
					fileIds.add(resultSet.getInt(2));
				}
				else if (itemId != resultSet.getInt(1)) {
					List<Integer> newFileIds = new ArrayList<Integer>(fileIds);
					fileEntryIds.put(itemId, newFileIds);
					itemId = resultSet.getInt(1);
					fileIds.clear();
					fileIds.add(resultSet.getInt(2));
				}
				else {
					fileIds.add(resultSet.getInt(2));
				}
				resultSet.deleteRow();
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		return fileEntryIds;
	}
	
	private List<Integer> getFileIdsOfItem (int itemId) {
		List<Integer> fileIds = new ArrayList<Integer>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select id from files where item_id = ?;");
			preparedStatement.setInt(1, itemId);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) 
				fileIds.add(resultSet.getInt("id"));
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		return fileIds;
	}
	
	private int getItemFileNum (int itemId) {
		int fileNum = 0;
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn.prepareStatement("select count(*) from files where item_id = ?;");
			preparedStatement.setInt(1, itemId);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
				fileNum = resultSet.getInt(1);
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		return fileNum;
	}
	
	public Map<String, String> getTextContentOfFile () {
		Map<String, String> recordText = new HashMap<String, String>();
		
		return recordText;
	}
	
	public void justTest () {
		Map<Integer, List<Integer>> fileEntryIds = getIdsOfFileEntry();
		for (Map.Entry<Integer, List<Integer>> entry : fileEntryIds.entrySet()) {
			int itemId = entry.getKey();
			List<Integer> fileIds = entry.getValue();
			int fileNum = getItemFileNum(itemId);
			if (fileIds.size() == fileNum)
				System.out.println(itemId);
		}
	}
	
	public Map<String, String> getTextContentOfItem () {
		Map<String, String> recordText = new HashMap<String, String>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select a.record_id as item_id, b.id as file_id, a.text as text " + 
									  "from element_texts a inner join files b on a.record_id = b.item_id " +
									  "where a.element_id = '86' and a.record_type = 'item' and a.record_id != '3'" + 
									  "and character_length(a.text) >= 500;");
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next())
				recordText.put(resultSet.getInt("item_id") + "_" + resultSet.getInt("file_id")
					, resultSet.getString("text").trim());
			
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
