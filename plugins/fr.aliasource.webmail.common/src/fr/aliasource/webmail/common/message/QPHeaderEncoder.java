package fr.aliasource.webmail.common.message;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class QPHeaderEncoder {

	public String encode(String s) {
		final String pf = "=?UTF-8?Q?";
		final String sf = "?=";
		StringBuilder q = new StringBuilder(2 * s.length());

		StringBuilder line = new StringBuilder(78);
		line.append(pf);
		int curLen = pf.length() + sf.length() + 2;
		char[] chars = s.toCharArray();
		for (Character c : chars) {
			String enc = encodeChar(c);
			if (curLen + enc.length() > 76) {
				line.append(sf);
				line.append("\r\n");
				q.append(line.toString());
				line = new StringBuilder(78);
				line.append(' ');
				line.append(pf);
				line.append(enc);
				curLen = pf.length() + enc.length()+ sf.length() + 2;
			} else {
				curLen += enc.length();
				line.append(enc);
			}
		}
		line.append(sf);
		q.append(line.toString());

		return q.toString();
	}

	private String encodeChar(Character c) {
		try {
			if (c == ' ') {
				return "=20";
			} else {
				return URLEncoder.encode("" + c, "utf-8").replace("%", "=");
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(); // will not happen
		}
	}
	
}
