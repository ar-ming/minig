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

import java.util.Comparator;

/**
 * Sorting of email conversation.
 * 
 * @author tom
 * 
 */
public class ConversationComparator implements
		Comparator<ConversationReference> {

	private static int FIRST_BEFORE = -1;
	private static int FIRST_AFTER = 1;

	private boolean unreadFirst = false;

	public int compare(ConversationReference cr1, ConversationReference cr2) {
		if (unreadFirst) {
			if (cr1.isRead() && !cr2.isRead()) {
				return FIRST_AFTER;
			}
			if (!cr1.isRead() && cr2.isRead()) {
				return FIRST_BEFORE;
			}
		}
		return new Long(cr2.getLastMessageDate()).compareTo(cr1
				.getLastMessageDate());
	}

	public boolean equals(Object o) {
		return this == o;
	}

}
