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

import java.util.List;

/**
 * Finds information in indexed data
 * 
 * @author tom
 *
 */
public interface ISearchable {

	/**
	 * @param type expected results type
	 * @param query in lucene like syntax
	 * @return
	 */
	List<Hit> findByType(String type, String query);
	
}
