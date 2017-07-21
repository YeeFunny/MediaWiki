package edu.wm.library.lyf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExportHtmlFile {

	public static void main(String[] args) {
		
		String targetDir = "/home/yliu/Documents/MediaWiki-Transcription/";
		
		TranscribeUtil transcribeUtil = new TranscribeUtil();
		HtmlUtil htmlUtil = new HtmlUtil();
		
		// List all items having file transcription entries
		List<Item> fileEntry = transcribeUtil.getItemHavingFileEntry();
		
		// List all item transcription entries
		List<Item> itemEntry = transcribeUtil.getItemEntry();
		// Remove all item having file transcription entries
		itemEntry.removeAll(fileEntry);
		transcribeUtil.insertFileTranscription(itemEntry);
		
		// List all items having file number matching, last should be logged
		fileEntry = transcribeUtil.getItemHavingFileNumMatch(fileEntry);
		
		String fileText;
		Iterator<Item> iterator = fileEntry.iterator();
		while (iterator.hasNext()) {
			Item item = iterator.next();
			int itemId = item.getItemId();
			String itemText = transcribeUtil.getItemTranscription(itemId);
			fileText = transcribeUtil.concatenateFileTranscription(item.getFiles());
			if (itemText.equals("")) {
				// Item does not have transcription
				int elementId = transcribeUtil.insertItemTranscription(item);
				if (elementId == 1)
					iterator.remove();
			} else if (itemText.length() > fileText.length()) {
				List<File> fileList = item.getFiles();
				List<File> newFileList = new ArrayList<File>();
				transcribeUtil.updateFileTranscription(fileList, itemText);
				for (int i = 0; i < fileList.size(); i++) {
					File file = fileList.get(i);
					file.setText(itemText);
					newFileList.add(file);
				}
				item.setFiles(newFileList);
				itemEntry.add(item);
				iterator.remove();
			}
		}
		
		itemEntry.addAll(fileEntry);
		// Export entry to front-end
		List<Item> escapsedItemEntry = new ArrayList<Item>();
		for (int i = 0; i < itemEntry.size(); i++) {
			Item item = itemEntry.get(i);
			List<File> fileList = item.getFiles();
			List<File> newFileList = new ArrayList<File>();
			for (int j = 0; j < fileList.size(); j++) {
				File file = fileList.get(j);
				String text = htmlUtil.addHtmlTag(file.getText());
				file.setText(text);
				newFileList.add(file);
			}
			item.setFiles(newFileList);
			escapsedItemEntry.add(item);
		}
		htmlUtil.exportHtmlFile(escapsedItemEntry, targetDir);
	}
}
