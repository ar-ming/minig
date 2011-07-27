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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.service.MinigVacation;
import org.minig.obmsync.service.SettingService;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class UpdateVacation implements IControlledAction {

	private static final Log logger = LogFactory.getLog(UpdateVacation.class);

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		logger.info("updateVacation for user " + p.getAccount().getUserId());

		boolean enabled = "true".equals(req.getParameter("enabled"));
		Date start = null;
		Date end = null;

		String s = req.getParameter("start");
		if (s != null) {
			start = new Date(Long.parseLong(s));
		}
		s = req.getParameter("end");
		if (s != null) {
			end = new Date(Long.parseLong(s));
		}

		String text = req.getParameter("text");

		try {
			SettingService ss = new SettingService(p.getAccount());
			MinigVacation mv = new MinigVacation();
			mv.setEnabled(enabled);
			mv.setStart(start);
			mv.setEnd(end);
			mv.setText(text);
			ss.setVacationSettings(mv);
			flushSieveOnServer(p);
			logger.info("[" + p.getAccount().getUserId()
					+ "] vacation setting updated.");
			responder.sendString("OK");
		} catch (Exception e) {
			logger.error("[" + p.getAccount().getUserId()
					+ "] error setting vacation", e);
			responder.sendError(e.getMessage());
		}

	}

	private void flushSieveOnServer(IProxy p) {
		//update sieve script : filter cache is not a cache, and should provide an API 
		FilterCache filterCache = new FilterCache(p.getAccount());
		filterCache.writeToCache(filterCache.loadFromCache());
	}

	@Override
	public String getUriMapping() {
		return "/updateVacation.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
