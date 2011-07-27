package fr.aliasource.webmail.common;


public final class LoginUtils {

	public static final String lat(IAccount ac) {
		String login = ac.getUserId();
		if (!login.contains("@")) {
			login = login + "@" + ac.getDomain();
		}
		return login;
	}

}
