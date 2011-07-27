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

package org.minig.settings.signature;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.settings.Signature;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class SaveSignaturesAction implements IControlledAction {

	@SuppressWarnings("unused")
	private Log logger;

	public SaveSignaturesAction() {
		logger = LogFactory.getLog(getClass());
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String xmlSignature = req.getParameter("xmlsignature");
		InputStream is = new ByteArrayInputStream(xmlSignature.getBytes());

		try {
			Document doc = DOMUtils.parse(is);
			List<Signature> signatures = getSignatureFromXml(doc);
			p.getSettingService().saveSignature(signatures);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<Signature> getSignatureFromXml(Document doc) {
		List<Signature> signatures = new ArrayList<Signature>();
		NodeList nodesSignature = doc.getElementsByTagName("signature");
		for (int i = 0; i < nodesSignature.getLength(); ++i) {
			Element ele = (Element) nodesSignature.item(i);
			String email = ele.getAttribute("email");
			String signature = ele.getTextContent();
			signatures.add(new Signature(email, signature));
		}

		return signatures;
	}

	@Override
	public String getUriMapping() {
		return "/saveSignature.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
