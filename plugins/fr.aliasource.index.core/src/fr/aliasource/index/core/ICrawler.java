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

package fr.aliasource.index.core;


/**
 * Fetches & parses data for indexation purpose
 * 
 * @author tom
 *
 */
public interface ICrawler {
	
	/**
	 * eg. obm_contact
	 * 
	 * @return
	 */
	String getType();
	
	/**
	 * Register a listener, that will be notified when this crawler finds indexable data
	 * 
	 * @param listener
	 */
	void registerListener(ICrawlerListener listener);

	/**
	 * Performs queued fetches, and notifies the listener for each found data
	 */
	void startFetch();
	
	void queueFetch(String id);

}
