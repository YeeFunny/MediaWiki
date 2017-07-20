package edu.wm.library.lyf;

public class File {
	private int fileId;
	private String text;
	private boolean exist;
	
	public File(int fileId) {
		this.fileId = fileId;
	}
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof File)
			exist = this.fileId == ((File)object).fileId;
		return exist;
	}
}
