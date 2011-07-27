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

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class UpdateForward implements IControlledAction {

	private static final Log logger = LogFactory.getLog(UpdateForward.class);

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		logger.info("updateVacation for user " + p.getAccount().getUserId());

		boolean enabled = "true".equals(req.getParameter("enabled"));
		boolean allowed = "true".equals(req.getParameter("allowed"));
		boolean localCopy = "true".equals(req.getParameter("localCopy"));
		String email = req.getParameter("email");
		
		try {
			SettingService ss = new SettingService(p.getAccount());
			MinigForward mf = new MinigForward();
			mf.setEnabled(enabled);
			mf.setAllowed(allowed);
			mf.setLocalCopy(localCopy);
			mf.setEmail(email);
			ss.setEmailForwarding(mf);
			logger.info("[" + p.getAccount().getUserId()
					+ "] forward setting updated.");
			responder.sendString("OK");
		} catch (Exception e) {
			logger.error("[" + p.getAccount().getUserId()
					+ "] error setting forward", e);
			responder.sendError(e.getMessage());
		}

	}

	@Override
	public String getUriMapping() {
		return "/updateForward.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
