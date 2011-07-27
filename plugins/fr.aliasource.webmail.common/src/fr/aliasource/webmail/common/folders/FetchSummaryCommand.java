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

package fr.aliasource.webmail.common.folders;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IFetchSummary;
import fr.aliasource.webmail.common.cache.ConversationCache;
import fr.aliasource.webmail.common.cache.IDirectCommand;
import fr.aliasource.webmail.common.imap.StoreException;

public class FetchSummaryCommand implements IFetchSummary,
		IDirectCommand<SortedMap<IFolder, Integer>> {

	private IAccount ac;

	public FetchSummaryCommand(IAccount ac) {
		this.ac = ac;
	}

	public SortedMap<IFolder, Integer> getData() throws IOException,
			StoreException, InterruptedException {
		return getSummary();
	}

	@Override
	public SortedMap<IFolder, Integer> getSummary() throws IOException,
			StoreException, InterruptedException {
		List<IFolder> folders = ac.getCache().getSubscribedFolderCache().getData();
		SortedMap<IFolder, Integer> ret = new TreeMap<IFolder, Integer>();

		ConversationCache cc = ac.getCache().getConversationCache();
		for (IFolder f : folders) {
			ret.put(f, cc.unreadCount(f));
		}
		return ret;
	}

}
