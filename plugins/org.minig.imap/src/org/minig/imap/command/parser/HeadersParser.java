package org.minig.imap.command.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import fr.aliasource.utils.DOMUtils;

public class HeadersParser {

	public static Map<String, String> parseRawHeaders(Reader reader) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(reader);
		String line = null;
		StringBuilder curHead = null;
		String lastKey = null;
		while ((line = br.readLine()) != null) {
			// collapse rfc822 headers into one line
			if (!(line.length() > 1)) {
				continue;
			}
			char first = line.charAt(0);
			if (Character.isWhitespace(first)) {
				int nbSpaces = 1;
				while (Character.isWhitespace(line.charAt(nbSpaces))) {
					nbSpaces += 1;
				}
				curHead.append(' ').append(line.substring(nbSpaces));
			} else {
				if (lastKey != null) {
					headers.put(lastKey, DOMUtils
							.stripNonValidXMLCharacters(curHead.toString()));
				}
				curHead = new StringBuilder();
				lastKey = null;

				int split = line.indexOf(':');
				if (split > 0) {
					lastKey = line.substring(0, split).toLowerCase();
					String value = line.substring(split + 1).trim();
					curHead.append(value);
				}

			}
		}
		if (lastKey != null) {
			headers.put(lastKey, DOMUtils.stripNonValidXMLCharacters(curHead
					.toString()));
		}
		return headers;
	}
	
}
