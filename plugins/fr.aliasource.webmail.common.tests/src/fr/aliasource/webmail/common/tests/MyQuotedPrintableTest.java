package fr.aliasource.webmail.common.tests;

import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.minig.imap.impl.QuotedPrintable;

import fr.aliasource.webmail.common.message.QPHeaderEncoder;

public class MyQuotedPrintableTest extends TestCase {

	public void testQPEncode() {
		String str = "Mail avec accents dans le sujé qui est plutot long et va faire chier thunderbird.......... €éè";
		QPHeaderEncoder hc = new QPHeaderEncoder();
		String ret = hc.encode(str);
		System.out.println(ret.replace("\r", "\\R").replace("\n", "\\N")
				.replace(" ", "\\S"));
	}
	
	public void testDecode() {
		String s = "=?utf-8?q?Joyeux_No=C3=AAl?=";
		CharSequence dec = QuotedPrintable.decode(s, Charset.forName("utf-8"));
		System.out.println("dec: "+dec);
		
	}

}
