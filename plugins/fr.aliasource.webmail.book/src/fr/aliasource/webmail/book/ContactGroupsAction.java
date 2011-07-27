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

package fr.aliasource.webmail.book;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.LoginUtils;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class ContactGroupsAction extends AbstractControlledAction {

	private Log logger;

	public ContactGroupsAction() {
		logger = LogFactory.getLog(getClass());
	}

	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://obm.aliasource.fr/xsd/contact_group",
					"contactGroups");
			Element root = doc.getDocumentElement();

			BookManager bm = BookActivator.getDefault().getBookManager();
			List<ContactGroup> cgl = bm.getGroups(LoginUtils
					.lat(p.getAccount()), p.getAccount().getUserPassword());
			for (ContactGroup cg : cgl) {
				Element cge = DOMUtils.createElement(root, "cg");
				cge.setAttribute("id", cg.getId());
				cge.setAttribute("d", cg.getDisplayName());
				cge
						.setAttribute("count", new Integer(cg.getCount())
								.toString());
			}

			responder.sendDom(doc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public String getUriMapping() {
		return "/contactGroups.do";
	}

}
