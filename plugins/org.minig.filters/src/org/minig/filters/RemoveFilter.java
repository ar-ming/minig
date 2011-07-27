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

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class RemoveFilter implements IControlledAction {

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String filterId = req.getParameter("filterId");
		if (filterId != null) {
			FilterCache cache = new FilterCache(p.getAccount());
			cache.remove(filterId);
		}
	}

	@Override
	public String getUriMapping() {
		return "/deleteFilter.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
