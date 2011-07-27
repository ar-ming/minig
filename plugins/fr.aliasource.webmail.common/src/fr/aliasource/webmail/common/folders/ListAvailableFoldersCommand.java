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
import java.util.LinkedList;
import java.util.List;

import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.NameSpaceInfo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.cache.IDirectCommand;
import fr.aliasource.webmail.common.cache.SubscribedFolderCache;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;

/**
 * List all imap folders on server. Caching is handled at a
 * higher level : see {@link SubscribedFolderCache}
 * 
 * @author tom
 * 
 */
public class ListAvailableFoldersCommand extends AbstractListFoldersCommand implements IDirectCommand<List<IFolder>> {

	public ListAvailableFoldersCommand(IAccount account) {
		super(account);
	}

	public List<IFolder> getData() throws IOException, StoreException, InterruptedException {
		LinkedList<IFolder> l = new LinkedList<IFolder>();
		
		ListResult infos = null;
		ListResult subs = null;
		NameSpaceInfo namespaces = null;
		IStoreConnection con = account.getStoreProtocol();
		try {
			subs = con.lsub("", "*");
			infos = con.list("", "*");
			namespaces = con.namespace();
		} finally {
			con.destroy();
		}

		if (infos != null && namespaces != null) {
			List<String> sharedNamespaces = Lists.newArrayList(
					Iterables.concat(namespaces.getOtherUsers(), namespaces.getMailShares()));
			
			for (ListInfo info : infos) {
				if(info.isSelectable()){
					boolean shared = isShared(sharedNamespaces, info);
					IFolder folder = new IMAPFolder(extractDisplayName(infos.getImapSeparator(), info), info.getName(),false, shared);
					if(subs!=null){
						for(ListInfo subInfo: subs){
							if(subInfo.getName().equals(info.getName())){
								folder.setSubscribed(true);
								break;
							}
						}
					}
					l.add(folder);
				}
			}
		}
		return l;
	}

}
