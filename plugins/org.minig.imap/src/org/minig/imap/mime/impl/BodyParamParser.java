package org.minig.imap.mime.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import org.minig.imap.EncodedWord;
import org.minig.imap.mime.BodyParam;


public class BodyParamParser {
	
	private final String key;
	private final String value;
	private String decodedKey;
	private String decodedValue;
	
	public BodyParamParser(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public BodyParam parse() {
		if (key.endsWith("*")) {
			decodedKey = key.substring(0, key.length() - 1);
			decodedValue = decodeAsterixEncodedValue();
		} else {
			decodedKey = key;
			decodedValue = decodeQuotedPrintable();
		}
		return new BodyParam(rewritedKey(decodedKey), decodedValue);
	}
	
	
	private String decodeAsterixEncodedValue() {
		final int firstQuote = value.indexOf("'");
		final int secondQuote = value.indexOf("'", firstQuote + 1);
		final String charsetName = value.substring(0, firstQuote);
		final String text = value.substring(secondQuote + 1);
		try {
			Charset charset = Charset.forName(charsetName);
			return URLDecoder.decode(text, charset.displayName());
		} catch (UnsupportedEncodingException e) {
		} catch (IllegalCharsetNameException e) {
		} catch (IllegalArgumentException e) {
		}
		return text;
	}

	private String decodeQuotedPrintable() {
		return EncodedWord.decode(value).toString();
	}

	private static String rewritedKey(String decodedKey) {
		if ("filename".equalsIgnoreCase(decodedKey)) {
			return "name";
		}
		return decodedKey.toLowerCase();
	}

}
