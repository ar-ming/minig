package fr.aliasource.webmail.truncation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.truncation.impl.HtmlTruncation;
import fr.aliasource.webmail.truncation.impl.PlainTruncation;

/**
 * 
 * @author adrienp
 * 
 */
public class TruncationService {
	
	private static final Log logger = LogFactory
			.getLog(TruncationService.class);
	private Map<String, ITruncation> truncates;

	private static TruncationService instance;

	private TruncationService() {
		truncates = new HashMap<String, ITruncation>();
		truncates.put("text/plain", new PlainTruncation());
		truncates.put("text/html", new HtmlTruncation());
		truncates.put("text/cleanHtml", new HtmlTruncation());
		truncates.put("text/partialCleanHtml", new HtmlTruncation());
		
	}

	public static TruncationService getInstance() {
		if (instance == null) {
			instance = new TruncationService();
		}
		return instance;
	}

	/**
	 * 
	 * @param mime 
	 * @param text
	 * @param size
	 * @return the truncated value in plain/text
	 * @throws Exception 
	 */
	public String truncate(String mime, String text, int size) throws Exception{
		ITruncation tr = truncates.get(mime);
		if(tr != null){
			text = tr.truncate(text, size);
		} else {
			logger.error("Can't truncate text with "+mime+" mime");
		}
		return text;
	}

}
