package fr.aliasource.webmail.common.message;

import com.google.common.base.Objects;

public class SendParameters {

	private boolean highPriority;
	private boolean sign;
	private boolean encrypt;
	private boolean dispositionNotification;
	
	public SendParameters() {
	}

	public boolean isHighPriority() {
		return highPriority;
	}

	public boolean isSign() {
		return sign;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public boolean isDispositionNotification() {
		return dispositionNotification;
	}
	
	public void setHighPriority(boolean highPriority) {
		this.highPriority = highPriority;
	}

	public void setSign(boolean sign) {
		this.sign = sign;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public void setDispositionNotification(boolean dispositionNotification) {
		this.dispositionNotification = dispositionNotification;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("highPriority", highPriority)
			.add("encrypt", encrypt)
			.add("sign", sign)
			.add("disposition-notification", dispositionNotification)
			.toString();
	}
	
}
