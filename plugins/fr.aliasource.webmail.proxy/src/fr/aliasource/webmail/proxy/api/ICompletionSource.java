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

package fr.aliasource.webmail.proxy.api;

import java.util.List;

import fr.aliasource.webmail.common.IAccount;

/**
 * A source of completion
 * 
 * @author tom
 *
 */
public interface ICompletionSource {

	/**
	 * returns limit (or less) possible completions for the given query.
	 * 
	 * Implementation MUST be thread-safe.
	 * 
	 * @param query
	 *            what to complete
	 * @param limit
	 * @return
	 */
	List<Completion> complete(IAccount account, String query, int limit);

}
