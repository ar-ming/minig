package org.minig.backend.xmpp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class SaveHistoryAction implements IControlledAction {

	private static final Log logger = LogFactory
			.getLog(SaveHistoryAction.class);

	public SaveHistoryAction() {
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String xmlString = req.getParameter("history");
		Document doc = null;
		try {
			doc = DOMUtils
					.parse(new ByteArrayInputStream(xmlString.getBytes()));
			String path = p.getAccount().getCache().getCachePath();
			File f = new File(path + "/chats");
			f.mkdirs();
			String uid = UUID.randomUUID().toString();
			DOMUtils.serialise(doc, new FileOutputStream(f.getAbsolutePath()
					+ "/" + uid + ".xml"));
			IAccount ac = p.getAccount();
			ac.getSearchDirector().crawlData(ac.getUserId() + "/chat", uid);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String getUriMapping() {
		return "/saveChatHistory.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
