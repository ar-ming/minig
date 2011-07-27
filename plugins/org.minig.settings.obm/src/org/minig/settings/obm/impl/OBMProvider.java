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

package org.minig.settings.obm.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.backend.settings.ISettingsProvider;
import org.minig.obmsync.service.ISettingService;
import org.minig.obmsync.service.SettingService;
import org.minig.settings.obm.SettingsFactory;

import fr.aliasource.webmail.common.IAccount;

public class OBMProvider implements ISettingsProvider {

	private IAccount ac;

	private static final Log logger = LogFactory.getLog(OBMProvider.class);

	public OBMProvider(IAccount ac) {
		this.ac = ac;
	}

	@Override
	public String getCategory() {
		return "obm";
	}

	@Override
	public Map<String, String> getData() {
		Map<String, String> data = new HashMap<String, String>();
		try {
			ISettingService settingService = new SettingService(ac);
			data = settingService.getSettings();
			logger.info("return " + data.size() + " settings for userid: " + ac.getUserId());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (SettingsFactory.EXTERNAL_URL != null) {
			data.put("external_url", SettingsFactory.EXTERNAL_URL);
		}
		return data;
	}

	@Override
	public void destroy() {
	}

}
