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

package org.minig.backend.settings.impl;

import java.util.Map;

import org.minig.backend.settings.ISettingsProvider;

import fr.aliasource.utils.IniFile;

public class IniProvider implements ISettingsProvider {

	private IniFile ini;

	public IniProvider(IniFile ini) {
		this.ini = ini;
	}
	
	@Override
	public String getCategory() {
		return ini.getCategory();
	}

	@Override
	public Map<String, String> getData() {
		return ini.getData();
	}

	@Override
	public void destroy() {
	}

}
