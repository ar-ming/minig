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
import java.util.SortedMap;

import fr.aliasource.webmail.common.folders.IFolder;

/**
 * 
 * @author matthieu
 *
 */
public interface IFolderService {

	void createFolder(IFolder f);

	void subscribeFolder(IFolder f);

	void unSubscribeFolder(IFolder f);

	List<IFolder> listSubscribedFolders();
	
	List<IFolder> listAvailableFolders();

	SortedMap<IFolder, Integer> getSummary();

	void deleteFolder(IFolder folder);
	
	void moveFolder(IFolder src, IFolder dest);

	void renameFolder(IFolder folder, String newName);


}
