package fr.aliasource.webmail.truncation.impl;

import fr.aliasource.webmail.truncation.ITruncation;

public class PlainTruncation implements ITruncation{

	@Override
	public String truncate(String text, int size) {
		if(text != null && text.length()>size){
			text = text.substring(0, size);
		}
		return text;
	}

}
