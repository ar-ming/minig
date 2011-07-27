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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.message.AttachmentManager;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

/**
 * Fetches metadata for a given list of attachments id.
 * 
 * @author tom
 * 
 */
public class AttachmentsMetadataAction extends AbstractControlledAction {

	private Log logger;

	public AttachmentsMetadataAction() {
		logger = LogFactory.getLog(getClass());
	}

	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		Document doc = null;
		String[] ids = req.getParameter("ids").split(",");
		logger.info("fetching metadata for " + ids.length + " attachments. ("
				+ req.getParameter("ids") + ")");

		AttachmentManager mgr = p.getAccount().getAttachementManager();

		try {
			doc = DOMUtils.createDoc("http://minig.org/xsd/attachMetadatas",
					"attachmentId");
			Element root = doc.getDocumentElement();

			for (String id : ids) {
				logger.info("Going to load metadata for '" + id + "'");
				Map<String, String> meta = mgr.getMetadata(id);
				Element att = DOMUtils.createElement(root, "att");
				att.setAttribute(AttachmentManager.META_FILENAME, meta
						.get(AttachmentManager.META_FILENAME));
				att.setAttribute(AttachmentManager.META_SIZE, meta
						.get(AttachmentManager.META_SIZE));
				att.setAttribute(AttachmentManager.META_MIME, meta
						.get(AttachmentManager.META_MIME));
				att.setAttribute(AttachmentManager.META_PREVIEW, ""
						+ mgr.hasPreview(id));
				if (meta.containsKey(AttachmentManager.META_PREVIEW_MIME)) {
					att.setAttribute(AttachmentManager.META_PREVIEW_MIME, meta
							.get(AttachmentManager.META_PREVIEW_MIME));
				}
			}

			responder.sendDom(doc);
		} catch (Exception e) {
			logger.error("Error allocating attachment id", e);
		}
	}

	public String getUriMapping() {
		return "/attachmentsMetadata.do";
	}

}
