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

package org.minig.settings.obm;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.backend.settings.ISettingsProvider;
import org.minig.backend.settings.ISettingsProviderFactory;
import org.minig.settings.obm.impl.OBMProvider;

import fr.aliasource.utils.IniFile;
import fr.aliasource.webmail.common.IAccount;

/**
 * Pushes obm user settings to minig
 * 
 * @author tom
 * 
 */
public class SettingsFactory implements ISettingsProviderFactory {

	private static final Log logger = LogFactory.getLog(SettingsFactory.class);
	public static String EXTERNAL_URL;

	static {
		// external-url=obm23.buffy.kvm
		// external-protocol=https
		// obm-prefix=/
		IniFile ini = new IniFile("/etc/obm/obm_conf.ini") {

			@Override
			public String getCategory() {
				return "obm_conf_ini";
			}
		};
		Map<String, String> d = ini.getData();
		if (d != null && !d.isEmpty()) {
			StringBuilder ext = new StringBuilder(100);
			ext.append(d.get("external-protocol").trim());
			ext.append("://");
			ext.append(d.get("external-url").trim());
			ext.append(d.get("obm-prefix").trim());
			EXTERNAL_URL = ext.toString();
		}
	}
	
	public SettingsFactory() {
		logger.info("OBM settings factory created");
	}

	@Override
	public ISettingsProvider getProvider(IAccount ac) {
		return new OBMProvider(ac);
	}

}
