package fr.aliasource.webmail.troncation.test;

import java.io.InputStream;

import fr.aliasource.utils.FileUtils;
import fr.aliasource.webmail.truncation.TruncationService;
import junit.framework.TestCase;

public class HtmlTruncateTest  extends TestCase{
	
	public InputStream getFile(String filename) {
		InputStream in = HtmlTruncateTest.class.getClassLoader()
				.getResourceAsStream("html/" + filename);
		if (in == null) {
			fail("Cannot load " + filename);
		}
		return in;
	}
	
	public void testTruncate(){
		InputStream isHtml = getFile("test1.html");
		String html;
		try {
			html = FileUtils.streamString(isHtml, true);
			String truncated = TruncationService.getInstance().truncate("text/html", html, 61440);
			System.out.println(truncated);
			System.out.println("html["+html.length()+"] tronc["+truncated.length()+"]");
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
