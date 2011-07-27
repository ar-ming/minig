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

package org.minig.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.service.MinigForward;
import org.minig.obmsync.service.SettingService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class FetchForward implements IControlledAction {

	private static final Log logger = LogFactory.getLog(FetchForward.class);

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		logger.info("fetchVacation for user " + p.getAccount().getUserId());
		Document doc = null;
		try {
			doc = DOMUtils.createDoc("http://minig.org/xsd/forward.xsd",
					"forward");

			SettingService ss = new SettingService(p.getAccount());
			MinigForward mv = ss.getEmailForwarding();
			Element root = doc.getDocumentElement();
			root.setAttribute("enabled", "" + mv.isEnabled());
			root.setAttribute("localCopy", ""+mv.isLocalCopy());
			root.setAttribute("allowed", ""+mv.isAllowed());
			root.setTextContent(mv.getEmail());
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		try {
			responder.sendDom(doc);
		} catch (Exception e1) {
		}
	}

	@Override
	public String getUriMapping() {
		return "/fetchForward.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
