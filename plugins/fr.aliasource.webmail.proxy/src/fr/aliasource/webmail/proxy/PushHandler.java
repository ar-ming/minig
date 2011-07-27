package fr.aliasource.webmail.proxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IClientChannel;
import fr.aliasource.webmail.common.ServerEventKind;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class PushHandler implements IClientChannel {

	private Log logger = LogFactory.getLog(getClass());

	private Map<IAccount, Continuation> conts;

	public PushHandler() {
		conts = Collections
				.synchronizedMap(new HashMap<IAccount, Continuation>());
	}

	void handlePush(IProxy p, HttpServletRequest req, IResponder resp) {

		IAccount ac = p.getAccount();
		Continuation cont = ContinuationSupport.getContinuation(req, ac);

		logger.info("handlePush(" + p.getAccount().getUserId() + ") cont: "
				+ cont);

		if (isNewCont(ac, cont)) {
			ac.setClientChannel(this);
			synchronized (conts) {
				conts.put(ac, cont);
			}
			logger.info("push suspend");
			cont.suspend(10000);
		} else if (cont.isPending() && cont.isResumed()) {
			logger.info("push triggered event p:" + cont.isPending() + " r: "
					+ cont.isResumed());
			endPush(ac);
			ServerEventKind evKind = (ServerEventKind) cont.getObject();
			if (evKind == null) {
				evKind = ServerEventKind.NONE;
			}
			resp.sendString(evKind.toString());
		} else { // (expiration)
			logger.info("push expired p:" + cont.isPending() + " r: "
					+ cont.isResumed());
			endPush(ac);
			resp.sendString(ServerEventKind.NONE.toString());
		}
	}

	private boolean isNewCont(IAccount ac, Continuation cont) {
		if (cont.isNew()) {
			return true;
		}
		synchronized (conts) {
			return !conts.containsKey(ac);
		}
	}

	private void endPush(IAccount ac) {
		ac.setClientChannel(null);
		synchronized (conts) {
			conts.remove(ac);
		}
	}

	@Override
	public void triggerEvent(IAccount ac, ServerEventKind eventKind) {
		logger.info("trigger event for " + ac.getUserId() + ": " + eventKind);
		Continuation cont = null;
		synchronized (conts) {
			cont = conts.get(ac);
		}
		if (cont != null) {
			cont.setObject(eventKind);
			cont.resume();
		}
	}

}
