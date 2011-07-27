package org.minig.imap;

import java.util.List;

public class NameSpaceInfo {
	
	private List<String> personal;
	private List<String> otherUsers;
	private List<String> mailShares;
	
	public List<String> getPersonal() {
		return personal;
	}
	public void setPersonal(List<String> personal) {
		this.personal = personal;
	}
	public List<String> getOtherUsers() {
		return otherUsers;
	}
	public void setOtherUsers(List<String> otherUsers) {
		this.otherUsers = otherUsers;
	}
	public List<String> getMailShares() {
		return mailShares;
	}
	public void setMailShares(List<String> mailShares) {
		this.mailShares = mailShares;
	}

	

}
