import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String a = "Yi    \n  fang 	\n		Liu		";
		System.out.println(a);
		Pattern pattern = Pattern.compile("[^\\S\n]+\n");
		Matcher matcher = pattern.matcher(a);
		while (matcher.find())
			a = matcher.replaceAll("<br />");
		System.out.println(a);
	}

}
