package edu.wm.library.lyf;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {
	
	private Pattern pattern;
	private Matcher matcher;
	
	public Map<String, String> addHtmlTag (Map<String, String> queryResult) {
		for (Map.Entry<String, String> entry: queryResult.entrySet()) {
			String id = entry.getKey();
			String text = entry.getValue();
			pattern = Pattern.compile("[^\\S\n]*[\n]");
			matcher = pattern.matcher(text);
			while (matcher.find()) {
				text = matcher.replaceAll("<br />");
			}
			text = escapeQuoteSpace(text);
			queryResult.replace(id, text);
		}
		return queryResult;
	}
	
	public void exportHtmlFile (Map<String, String> queryResult, String targetDir) {
		for (Map.Entry<String, String> entry: queryResult.entrySet()) {
			Path file = Paths.get(targetDir + entry.getKey() + ".txt");
			try {
				Files.write(file, entry.getValue().getBytes());
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	
	private String escapeQuoteSpace (String entry) {
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
