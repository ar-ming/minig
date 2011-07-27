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

package fr.aliasource.webmail.attachments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class AllocateIdAction extends AbstractControlledAction {

	private Log logger;

	public AllocateIdAction() {
		logger = LogFactory.getLog(getClass());
		if (logger.isDebugEnabled()) {
			logger.debug("AllocateIdAction created.");
		}
	}

	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String attId = p.allocateAttachmentId();
		Document doc = null;
		try {
			doc = DOMUtils.createDoc("http://minig.org/xsd/attachId",
					"attachmentId");
			DOMUtils.createElementAndText(doc.getDocumentElement(), "value",
					attId);
			responder.sendDom(doc);
		} catch (Exception e) {
			logger.error("Error allocating attachment id", e);
		}
	}

	public String getUriMapping() {
		return "/allocateAttachmentId.do";
	}

}
