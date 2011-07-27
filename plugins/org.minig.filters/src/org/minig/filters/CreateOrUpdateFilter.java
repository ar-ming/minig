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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class CreateOrUpdateFilter implements IControlledAction {

	private static final Log logger = LogFactory
			.getLog(CreateOrUpdateFilter.class);

	public CreateOrUpdateFilter() {
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {

		FilterCache filterCache = new FilterCache(p.getAccount());
		List<FilterDefinition> fds = new ArrayList<FilterDefinition>();
		try {
			String filter = req.getParameter("filter");
			if (filter != null) {
				Document doc = DOMUtils.parse(new ByteArrayInputStream(filter
						.getBytes()));
				DOMUtils.logDom(doc);
				FilterDefinition fd = new FilterUtils().parseDefinition(doc
						.getDocumentElement());

				logger.info("should update sieve to add a new filter rule.");
				fds.add(fd);
			} else {
				logger.info("should regenerate sieve script");
			}

			filterCache.writeToCache(fds);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Override
	public String getUriMapping() {
		return "/updateFilter.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
