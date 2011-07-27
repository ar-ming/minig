package fr.aliasource.webmail.truncation;

/**
 * 
 * @author adrienp
 *
 */
public interface ITruncation {
	
	/**
	 * 
	 * @param text
	 * @param size
	 * @return
	 */
	String truncate(String text, int sizeMax) throws Exception;
}
