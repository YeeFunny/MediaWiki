package edu.wm.library.lyf;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TranscribeUtil {
	
	private MysqlUtil mysqlUtil;
	private Connection conn = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	private List<Item> itemList;
	private List<File> fileList;
	private Item item;
	private File file;
	
	public TranscribeUtil () {
		this.mysqlUtil = new MysqlUtil();
	}
	
	private List<Item> getItemHavingFileEntry () {
		itemList = new ArrayList<Item>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select f.item_id, e.record_id, e.text from element_texts e inner join files f on e.record_id = f.id " + 
									  "where e.element_id = '86' and e.record_type = 'File' and f.item_id != '3' order by f.item_id;");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				file = new File(resultSet.getInt(2));
				file.setText(resultSet.getString(3).trim());
				if (item == null) {
					item = new Item(resultSet.getInt(1));
					fileList = new ArrayList<File>();
				} else if (resultSet.isLast()) {
					fileList.add(file);
					item.setFiles(fileList);
					itemList.add(item);
					break;
				}
				if (item.getItemId() != resultSet.getInt(1)) {
					item.setFiles(fileList);
					itemList.add(item);
					item = new Item(resultSet.getInt(1));
					fileList = new ArrayList<File>();;
				}
				
				fileList.add(file);
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		return itemList;
	}
	
	private List<Integer> getFileIdsOfItem (int itemId) {
		List<Integer> fileIds = new ArrayList<Integer>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn.prepareStatement("select id from files where item_id = ?;");
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
	
	public void justTest() {
		itemList = getItemHavingFileEntry();
		Iterator<Item> iterator = itemList.iterator();
		while (iterator.hasNext()) {
			item = iterator.next();
			System.out.print(item.getItemId() + "|");
			List<Integer> fileIds = getFileIdsOfItem(item.getItemId());
			for (int i = 0; i < fileIds.size(); i++) {
				file = new File(fileIds.get(i));
				if (!item.getFiles().contains(file)) {
					iterator.remove();
					break;
				}
			}
		}
		System.out.println("---------------------------------------------");
		for (int i = 0; i < itemList.size(); i++) {
			System.out.print(itemList.get(i).getItemId() + "|");
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
