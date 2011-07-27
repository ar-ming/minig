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

package org.minig.backend.xmpp;

import java.util.HashMap;
import java.util.Map;

import org.minig.backend.settings.ISettingsProvider;

import fr.aliasource.webmail.common.Credentials;

public class XMPPSettingsProvider implements ISettingsProvider {

	private Credentials creds;

	public XMPPSettingsProvider(Credentials credentials) {
		this.creds = credentials;
	}

	@Override
	public void destroy() {
	}

	@Override
	public String getCategory() {
		return "xmpp_configuration";
	}

	@Override
	public Map<String, String> getData() {
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("account_password", creds.getPassword());
		return ret;
	}

}
