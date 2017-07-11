import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {
	
	public Map<String, String> addHtmlTag (Map<String, String> queryResult) {
		for (Map.Entry<String, String> entry: queryResult.entrySet()) {
			String id = entry.getKey();
			String text = entry.getValue();
			Pattern pattern = Pattern.compile("[^\\S\n]*\n");
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				text = matcher.replaceAll("<br />");
			}
			text = escapeQuote(text);
			queryResult.replace(id, text);
		}
		return queryResult;
	}
	
	private String escapeQuote (String entry) {
		String[] quotes = {"\"", "'"};
		for (String quote: quotes) {
			if (entry.contains(quote))
				entry = entry.replace(quote, "\\" + quote);
		}
		return entry;
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
}
