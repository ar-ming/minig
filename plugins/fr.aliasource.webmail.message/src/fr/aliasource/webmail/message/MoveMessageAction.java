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
 *   minig.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package fr.aliasource.webmail.message;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.proxy.IControlledAction;
import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class MoveMessageAction implements IControlledAction {

	private Log logger;

	public MoveMessageAction() {
		this.logger = LogFactory.getLog(getClass());
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String move = req.getParameter("move");
		logger.info("move param value: " + move);
		String convId = req.getParameter("conversation");
		String ret = null;
		if (req.getParameter("message") != null) {
			String id = req.getParameter("message");
			IFolder dest = new IMAPFolder(req.getParameter("destination"));
			ret = p.moveMessage(dest, convId, Arrays.asList(Long.parseLong(id))).iterator().next();
		}
		responder.sendStream(new ByteArrayInputStream(ret.getBytes()));
	}

	@Override
	public String getUriMapping() {
		return "/moveMessage.do";
	}

	@Override
	public void init(ProxyConfiguration pcf) {
	}

}
