package edu.wm.library.lyf;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class TranscribeUtil {
	
	private MysqlUtil mysqlUtil;
	private Connection conn = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	private List<Item> itemList;
	private List<File> fileList;
	private Item item;
	private File file;
	
	private String text;
	
	public TranscribeUtil () {
		this.mysqlUtil = new MysqlUtil();
	}
	
	public List<Item> getItemHavingFileEntry () {
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
	
	public List<Item> getItemEntry () {
		itemList = new ArrayList<Item>();
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select e.record_id as item_id, f.id as file_id, e.text as text " + 
									  "from element_texts e inner join files f on e.record_id = f.item_id " +
									  "where e.element_id = '86' and e.record_type = 'item' and e.record_id != '3'" + 
									  "and character_length(e.text) >= 500;");
			resultSet = preparedStatement.executeQuery();
			
			while (resultSet.next()) {
				item = new Item(resultSet.getInt(1));
				fileList = new ArrayList<File>();
				file = new File(resultSet.getInt(2));
				file.setText(resultSet.getString(3).trim());
				fileList.add(file);
				item.setFiles(fileList);
				itemList.add(item);
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		return itemList;
	}
	
	public String concatenateFileTranscription (List<File> fileEntry) {
		text = "";
		Collections.sort(fileEntry, new Comparator<File>() {
			public int compare (File f1, File f2) {
				return f1.getFileId() - f2.getFileId();
			}
		});
		for (int i = 0; i < fileEntry.size(); i++) {
			text = text + "\n" + fileEntry.get(i).getText();
		}
		return text.trim();
	}
	
	public String getItemTranscription (int itemId) {
		text = ""; 
		conn = mysqlUtil.getConnection("transcribe");
		try {
			preparedStatement = conn
					.prepareStatement("select text from element_texts where record_id = ? and element_id = '86' and record_type = 'Item';");
			preparedStatement.setInt(1, itemId);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next())
				text = resultSet.getString("text").trim();
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		return text;
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
	
	public List<Item> getItemHavingFileNumMatch (List<Item> fileEntry) {
		Iterator<Item> iterator = fileEntry.iterator();
		while (iterator.hasNext()) {
			item = iterator.next();
			List<Integer> fileIds = getFileIdsOfItem(item.getItemId());
			for (int i = 0; i < fileIds.size(); i++) {
				file = new File(fileIds.get(i));
				if (!item.getFiles().contains(file)) {
					iterator.remove();
					break;
				}
			}
		}
		return fileEntry;
	}
	
	public int insertItemTranscription (Item entry) {
		int elementId = 1;
		text = "";
		conn = mysqlUtil.getConnection("transcribe");
		try {
			fileList = entry.getFiles();
			text = concatenateFileTranscription(fileList);
			if (text.length() >= 500)
				elementId = 86;
			preparedStatement = conn.prepareStatement("insert into element_texts (record_id, record_type, element_id, html, text)" + 
													  "values (?, 'Item', ?, 0, ?);");
			preparedStatement.setInt(1, entry.getItemId());
			preparedStatement.setInt(2, elementId);
			preparedStatement.setString(3, text);
			preparedStatement.executeUpdate();
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
		return elementId;
	}
	
	public void insertFileTranscription (List<Item> itemEntry) {
		conn = mysqlUtil.getConnection("transcribe");
		try {
			for (int i = 0; i < itemEntry.size(); i++) {
				item = itemEntry.get(i);
				fileList = item.getFiles();
				for (int j = 0; j < fileList.size(); j++) {
					preparedStatement = conn.prepareStatement("insert into element_texts (record_id, record_type, element_id, html, text)" + 
															  "values (?, 'File', 86, 0, ?);");
					preparedStatement.setInt(1, fileList.get(j).getFileId());
					preparedStatement.setString(2, fileList.get(j).getText());
					preparedStatement.executeUpdate();
				}
			}			
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		} finally {
			mysqlUtil.close();
		}
	}
	
	public void updateFileTranscription (List<File> fileEntry, String itemText) {
		conn = mysqlUtil.getConnection("transcribe");
		for (int i = 0; i < fileEntry.size(); i++) {
			try {
				preparedStatement = conn
						.prepareStatement("update element_texts set text = ? where record_id = ? and record_type = 'File' and element_id = '86';");
				preparedStatement.setString(1, itemText);
				preparedStatement.setInt(2, fileEntry.get(i).getFileId());
				preparedStatement.executeUpdate();
			} catch (SQLException sqlException) {
				sqlException.printStackTrace();
			} finally {
				mysqlUtil.close();
			}
		}
	}
}
