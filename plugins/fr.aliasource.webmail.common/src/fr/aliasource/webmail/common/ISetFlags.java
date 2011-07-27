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

package fr.aliasource.webmail.common;

import java.io.IOException;
import java.util.Set;

import fr.aliasource.webmail.common.imap.StoreException;


public interface ISetFlags {
	
	public static final String READ = "read";
	public static final String STAR = "star";
	public static final String ANSWERED = "answered";

	public void setFlags(Set<String> conversationIds, String flag) throws InterruptedException, IOException, StoreException;

	public void unsetFlags(Set<String> conversationIds, String flag) throws InterruptedException, IOException, StoreException;
	
	public void setFlags(String query, String flag) throws InterruptedException, IOException, StoreException;

	public void unsetFlags(String query, String flag) throws InterruptedException, IOException, StoreException;
	
}
