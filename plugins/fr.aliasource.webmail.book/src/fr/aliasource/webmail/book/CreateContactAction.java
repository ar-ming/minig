package fr.aliasource.webmail.book;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.book.impl.ContactSerialiser;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class CreateContactAction extends AbstractControlledAction {

	private Log logger = LogFactory.getLog(getClass());
	
	public CreateContactAction() {
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		IAccount account = p.getAccount();
		try {
			Document doc = DOMUtils.parse(new ByteArrayInputStream(req
					.getParameter("contact").getBytes()));
			Element root = doc.getDocumentElement();
			MinigContact mc = new ContactSerialiser().parse(root);
			LinkedList<MinigContact> lc = new LinkedList<MinigContact>();
			lc.add(mc);
			BookActivator.getDefault().getBookManager().insert(account, lc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String getUriMapping() {
		return "/createContact.do";
	}

}
