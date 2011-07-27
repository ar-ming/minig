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

package fr.aliasource.webmail.common.conversation;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.webmail.common.folders.IFolder;

/**
 * Records pending notifications during the cache merge process, to emit them
 * after the cache is written to disk.
 * 
 * @author tom
 * 
 */
public class PendingNotifications {

	private List<ConversationReference> added;
	private List<ConversationReference> removed;
	private List<ConversationReference> updated;
	private List<IConversationListener> listeners;

	private Log logger = LogFactory.getLog(getClass());
	private IFolder folder;

	public PendingNotifications(IFolder folder) {
		this.folder = folder;
		added = new LinkedList<ConversationReference>();
		removed = new LinkedList<ConversationReference>();
		updated = new LinkedList<ConversationReference>();
	}

	void added(ConversationReference cr) {
		added.add(cr);
	}

	void updated(ConversationReference cr) {
		updated.add(cr);
	}

	void removed(ConversationReference cr) {
		removed.add(cr);
	}

	public void emitNotifications() {
		if (logger.isInfoEnabled()
				&& (added.size() > 0 || updated.size() > 0 || removed.size() > 0)) {
			logger.info("emitNotifications() a:" + added.size() + " / u:"
					+ updated.size() + " / r:" + removed.size());
		}

		for (ConversationReference cref : removed) {
			for (IConversationListener l : listeners) {
				l.conversationRemoved(folder, cref);
			}
		}

		for (ConversationReference cref : updated) {
			for (IConversationListener l : listeners) {
				l.conversationUpdated(folder, cref);
			}
		}

		for (ConversationReference cref : added) {
			for (IConversationListener l : listeners) {
				l.conversationCreated(folder, cref);
			}
		}
	}

	public void setListeners(List<IConversationListener> listeners) {
		this.listeners = listeners;
	}

}
