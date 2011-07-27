package fr.aliasource.webmail.common;

import fr.aliasource.webmail.common.message.SendParameters;

public class SendParametersFactory {

	public static SendParameters createFromParamsMap(IParameterSource req) {
		boolean hp = "true".equals(req.getParameter("hp"));
		boolean dispositionNotification = "true".equals(req.getParameter("disposition-notification"));
		boolean encrypt = "true".equals(req.getParameter("encrypt"));
		boolean sign = "true".equals(req.getParameter("sign"));
		SendParameters sp = new SendParameters();
		sp.setHighPriority(hp);
		sp.setDispositionNotification(dispositionNotification);
		sp.setEncrypt(encrypt);
		sp.setSign(sign);
		return sp;
	}
	
}
