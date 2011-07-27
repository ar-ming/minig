package org.minig.obmsync.service;

public class MinigForward {
	private boolean enabled;
	private boolean localCopy;
	private boolean allowed;

	private String email;

	public MinigForward() {
		allowed = true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isLocalCopy() {
		return localCopy;
	}

	public void setLocalCopy(boolean localCopy) {
		this.localCopy = localCopy;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

}
