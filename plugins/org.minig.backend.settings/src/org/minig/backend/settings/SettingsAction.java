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

package org.minig.backend.settings;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class SettingsAction extends AbstractControlledAction {

	private Log logger = LogFactory.getLog(getClass());

	public SettingsAction() {
	}

	private List<ISettingsProviderFactory> getFactories() {
		return Activator.getDefault().getFactories();
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {

		try {
			Document ret = DOMUtils.createDoc(
					"http://obm.aliasource.org/xsd/server_settings",
					"serverSettings");

			Element root = ret.getDocumentElement();
			Element imap = DOMUtils.createElement(root, "imap");
			Element delim = DOMUtils.createElement(imap, "delimiter");
			delim.setAttribute("value", p.getAccount().getMailboxDelimiter());

			for (ISettingsProviderFactory spf : getFactories()) {
				ISettingsProvider sp = spf.getProvider(p.getAccount());
				appendSettings(sp, root);
				sp.destroy();
			}
			responder.sendDom(ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void appendSettings(ISettingsProvider sp, Element parent) {
		Element cat = DOMUtils.createElement(parent, sp.getCategory());

		Map<String, String> values = sp.getData();
		for (String key : values.keySet()) {
			Element e = DOMUtils.createElement(cat, key.replace(".", "_"));
			e.setAttribute("value", values.get(key));
		}
	}

	@Override
	public String getUriMapping() {
		return "/settings.do";
	}

}
