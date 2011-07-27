/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package fr.aliasource.webmail.quota;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.QuotaInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class ActionGetQuotaAction extends AbstractControlledAction {

	private Log logger;

	public ActionGetQuotaAction() {
		logger = LogFactory.getLog(getClass());
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String mailBox = req.getParameter("mailBox");

		IStoreConnection store = p.getAccount().getStoreProtocol();

		QuotaInfo qi = null;
		try {
			qi = store.getQuota(mailBox);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			store.destroy();
		}

		try {
			Document doc = DOMUtils.createDoc("http://minig.org/xsd/quotaInfo",
					"quotainfo");

			Element root = doc.getDocumentElement();

			Element att = DOMUtils.createElement(root, "enable");
			att.setTextContent(String.valueOf(qi.isEnable()));

			Element att1 = DOMUtils.createElement(root, "usage");
			att1.setTextContent(String.valueOf(qi.getUsage()));

			Element att2 = DOMUtils.createElement(root, "limit");
			att2.setTextContent(String.valueOf(qi.getLimit()));
			responder.sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public String getUriMapping() {
		return "/getQuota.do";
	}
}
