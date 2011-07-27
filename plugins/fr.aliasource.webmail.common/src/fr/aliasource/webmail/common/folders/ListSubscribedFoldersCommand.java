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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.NameSpaceInfo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.aliasource.webmail.common.AccountConfiguration;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.cache.IDirectCommand;
import fr.aliasource.webmail.common.cache.SubscribedFolderCache;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;

/**
 * List subscribed & other imap folders on server. Caching is handled at a
 * higher level : see {@link SubscribedFolderCache}
 * 
 * @author tom
 * 
 */
public class ListSubscribedFoldersCommand extends AbstractListFoldersCommand implements
		IDirectCommand<List<IFolder>> {

	private Log logger;

	public ListSubscribedFoldersCommand(IAccount account) {
		super(account);
		this.logger = LogFactory.getLog(getClass());
	}

	public List<IFolder> getData() throws IOException, StoreException,
			InterruptedException {
		LinkedList<IFolder> l = new LinkedList<IFolder>();

		ListResult infos = null;
		NameSpaceInfo namespaces = null;
		IStoreConnection con = account.getStoreProtocol();
		try {
			infos = con.lsub("", "*");
			namespaces = con.namespace();
		} finally {
			con.destroy();
		}

		if (infos != null && namespaces != null) {
			List<String> sharedNamespaces = Lists.newArrayList(
					Iterables.concat(namespaces.getOtherUsers(), namespaces.getMailShares()));
			
			for (ListInfo info : infos) {
				boolean shared = isShared(sharedNamespaces, info);
				l.add(new IMAPFolder(extractDisplayName(infos
						.getImapSeparator(), info), info.getName(), true, shared));
			}
		}
		return l;
	}

	public String initDefaultFolders(AccountConfiguration accountConf)
			throws IOException, StoreException, InterruptedException {
		String delim = null;
		IStoreConnection proto = account.getStoreProtocol();
		try {
			proto.subscribe("INBOX");
			ListResult infos = proto.list("", "*");
			Set<String> fNames = new HashSet<String>();
			if (infos != null) {
				delim = Character.toString(infos.getImapSeparator());
				for (ListInfo info : infos) {
					fNames.add(info.getName());
				}
				for (String folderName: AccountConfiguration.DEFAULT_FOLDERS) {
					createAndSubscribe(proto, fNames, accountConf.getSetting(
							account.getUserId(), folderName).replace(
							"%d", delim));
				}
			}
		} finally {
			proto.destroy();
		}
		return delim;
	}

	private void createAndSubscribe(IStoreConnection proto, Set<String> names,
			String folder) throws IOException, StoreException {
		if (!names.contains(folder)) {
			logger.info("Creating " + folder + " for user default setup.");
			proto.create(folder);
		}
		proto.subscribe(folder);

	}

}
