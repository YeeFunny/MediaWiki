import java.util.Map;

public class ExportHtmlFile {

	public static void main(String[] args) {
		
		String targetDir = "/home/yliu/Documents/MediaWiki-Transcription/";
		
		TranscribeUtil transcribeUtil = new TranscribeUtil();
		HtmlUtil htmlUtil = new HtmlUtil();
		
		Map<String, String> queryResult = transcribeUtil.getTextContentOfItem();
		
		queryResult = htmlUtil.addHtmlTag(queryResult);
		
		htmlUtil.exportHtmlFile(queryResult, targetDir);
	}

}
