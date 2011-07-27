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
import org.minig.obmsync.service.MinigVacation;
import org.minig.obmsync.service.SettingService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class FetchVacation implements IControlledAction {

	private static final Log logger = LogFactory.getLog(FetchVacation.class);

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		logger.info("fetchVacation for user " + p.getAccount().getUserId());
		Document doc = null;
		try {
			doc = DOMUtils.createDoc("http://minig.org/xsd/vacation.xsd",
					"vacation");

			SettingService ss = new SettingService(p.getAccount());
			MinigVacation mv = ss.getVacationSettings();
			Element root = doc.getDocumentElement();
			root.setAttribute("enabled", "" + mv.isEnabled());
			if (mv.getStart() != null) {
				root.setAttribute("start", "" + mv.getStart().getTime());
			}
			if (mv.getEnd() != null) {
				root.setAttribute("end", "" + mv.getEnd().getTime());
			}
			root.setTextContent(mv.getText());
			
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
		return "/fetchVacation.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
