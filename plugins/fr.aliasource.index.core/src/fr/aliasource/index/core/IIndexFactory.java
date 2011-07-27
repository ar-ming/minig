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
 * Factory used to obtain {@link Index} implementations. One implementation of
 * this interface must be registered from the extension point system.
 * 
 * @author tom
 * 
 */
public interface IIndexFactory {

	void init(IIndexingParameters params);
	
	/**
	 * Returns an initialized index, suitable for receiving data of the given
	 * type.
	 * 
	 * @param type
	 * @return
	 */
	Index getIndex(String type);

}
