package fr.aliasource.webmail.truncation.impl;


import java.io.StringReader;
import java.io.StringWriter;

//import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.cyberneko.html.HTMLConfiguration;
import org.cyberneko.html.filters.ElementRemover;
import org.cyberneko.html.filters.Writer;

import fr.aliasource.webmail.truncation.ITruncation;

public class HtmlTruncation implements ITruncation {

	private Log logger = LogFactory.getLog(getClass());
	
	@Override
	public String truncate(String html, int sizeMax) throws Exception {

		String retVal = "[html message]\n";

		String ret = html;
		ret = ret.replace("<br/>", "\n");
		ret = ret.replace("<BR/>", "\n");
		ret = ret.replace("<BR>", "\n");
		ret = ret.replace("<br>", "\n");

		ElementRemover er = new ElementRemover() {

			@Override
			public void comment(XMLString text, Augmentations augs)
					throws XNIException {
				// strip out comments, outlook loves comments in its html
			}

		};
		er.removeElement("script");
		er.removeElement("style");
		StringWriter sw = new StringWriter();

		XMLDocumentFilter[] filters = { er, new Writer(sw, "UTF-8") };

		XMLParserConfiguration xpc = new HTMLConfiguration();
		xpc
				.setProperty("http://cyberneko.org/html/properties/filters",
						filters);
		xpc.setProperty("http://cyberneko.org/html/properties/names/elems",
				"lower");

		XMLInputSource xis = new XMLInputSource(null, null, null,
				new StringReader(ret), null);

		try {
			xpc.parse(xis);
			retVal = sw.toString();
		} catch (Exception e) {
			logger.error(e.getMessage() + ". HTML was: \n" + html);
		}

		if(retVal != null && retVal.length()>sizeMax){
			retVal = retVal.substring(0, sizeMax);
		}
		
		return retVal;
	}
}
