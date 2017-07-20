package edu.wm.library.lyf;
import java.util.List;


public class Item {
	private int itemId;
	private List<File> files;
	
	public Item(int itemId) {
		this.itemId = itemId;
	}
	public List<File> getFiles() {
		return files;
	}
	public void setFiles(List<File> files) {
		this.files = files;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
}
