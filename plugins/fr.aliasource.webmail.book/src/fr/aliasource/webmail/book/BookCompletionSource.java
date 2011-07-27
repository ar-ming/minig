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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LoginUtils;
import fr.aliasource.webmail.proxy.api.Completion;
import fr.aliasource.webmail.proxy.api.ICompletionSource;

public class BookCompletionSource implements ICompletionSource {

	private BookManager bm;

	public BookCompletionSource() {
		this.bm = BookActivator.getDefault().getBookManager();
	}

	public List<Completion> complete(IAccount account, String query, int limit) {
		List<MinigContact> cs = bm.find(LoginUtils.lat(account), account
				.getUserPassword(), BookManager.ALL_SOURCE_ID, query, limit);
		int len = Math.min(cs.size(), limit);
		ArrayList<Completion> ret = new ArrayList<Completion>(len);
		for (int i = 0; i < len; i++) {
			MinigContact c = cs.get(i);
			Map<String, MinigEmail> mails = c.getEmails();
			for (String lbl : mails.keySet())
				ret.add(new Completion(mails.get(lbl).getEmail(), c
						.getDisplayName()));
		}
		return ret;
	}

}
