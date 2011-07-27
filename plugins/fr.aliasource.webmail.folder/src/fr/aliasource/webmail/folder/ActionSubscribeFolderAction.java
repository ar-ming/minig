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

package fr.aliasource.webmail.folder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

/**
 * 
 * @author matthieu
 *
 */
public class ActionSubscribeFolderAction extends AbstractControlledAction {

	private Log logger;

	public ActionSubscribeFolderAction() {
		logger = LogFactory.getLog(getClass());
	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {

		IFolder folder = new IMAPFolder(req.getParameter("folder"));
		logger.info("Subscribe to folder:"+folder.getName());
		p.getFolderService().subscribeFolder(folder);
	}

	@Override
	public String getUriMapping() {
		return "/subscribe.do";
	}
}
