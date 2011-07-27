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

import java.util.List;

import fr.aliasource.webmail.proxy.ProxyConfiguration;

public interface IBookSource {

	public enum SourceType {
		READ_ONLY, READ_WRITE
	}

	/**
	 * Called before any other method on the object
	 */
	void init(ProxyConfiguration pcf);

	ContactGroup getProvidedGroup();

	SourceType getType();

	/**
	 * Only init is allowed after this call.
	 */
	void shutdown();

	List<MinigContact> findAll(String userId, String userPassword);

	List<MinigContact> find(String userId, String userPassword, String query, int limit);
	
	int count(String userId, String userPassword);

	void insert(String userId, String userPassword, List<MinigContact> c);

}
