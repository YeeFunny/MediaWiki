package edu.wm.library.lyf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {
	
	private Pattern pattern;
	private Matcher matcher;
	
	public String addHtmlTag (String text) {
		pattern = Pattern.compile("[^\\S\n]*[\n]");
		matcher = pattern.matcher(text);
		while (matcher.find()) {
			text = matcher.replaceAll("<br />");
		}
		text = escapeSpace(text);
		return text;
	}
	
	public void exportHtmlFile (List<Item> itemList, String targetDir) {
		for (int i = 0; i < itemList.size(); i++) {
			Item item = itemList.get(i);
			List<File> fileList = item.getFiles();
			(new java.io.File(targetDir + "/" + item.getItemId())).mkdir();
			for (int j = 0; j < fileList.size(); j++) {
				Path file = Paths.get(targetDir + "/" + item.getItemId() + "/" + fileList.get(j).getFileId() + ".txt");
				try {
					Files.write(file, fileList.get(j).getText().getBytes());
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
	}
	
	private String escapeSpace (String entry) {
		String[] quotes = {"\"", "'"};
		for (String quote: quotes) {
			if (entry.contains(quote))
				entry = entry.replace(quote, "\\" + quote);
		}
		entry = entry.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		pattern = Pattern.compile(" (?= |&nbsp;)|(?<= |&nbsp;) ");
		matcher = pattern.matcher(entry);
		entry = matcher.replaceAll("&nbsp;");
		return entry;
	}
}
