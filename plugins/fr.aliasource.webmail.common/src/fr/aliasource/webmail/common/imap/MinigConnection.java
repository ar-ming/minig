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

package fr.aliasource.webmail.common.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.minig.imap.Envelope;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.ListResult;
import org.minig.imap.NameSpaceInfo;
import org.minig.imap.QuotaInfo;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.minig.imap.impl.MailThread;

public class MinigConnection implements IStoreConnection {

	private StoreClient sc;

	public MinigConnection(String host, int port, String login, String password)
			throws StoreException {
		this.sc = new StoreClient(host, port, login, password);
		boolean ok = false;
		try {
			ok = sc.login();
		} catch (org.minig.imap.IMAPException e) {
			throw new StoreException(e.getMessage(), e);
		}
		if (!ok) {
			throw new StoreException("Login refused for " + login + " / "
					+ password);
		}
	}

	@Override
	public long append(String name, InputStream formattedMessage,
			FlagsList flags) throws IOException, StoreException {
		return sc.append(name, formattedMessage, flags);
	}

	@Override
	public void create(String mailbox) throws IOException, StoreException {
		try {
			sc.create(mailbox);
		} catch (IMAPException e) {
			throw new StoreException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String mailbox) throws IOException, StoreException {
		try {
			sc.delete(mailbox);
		} catch (IMAPException e) {
			throw new StoreException(e.getMessage(), e);
		}
	}

	@Override
	public void expunge() throws IOException, StoreException {
		sc.expunge();
	}

	@Override
	public ListResult list(String reference, String mailbox)
			throws IOException, StoreException {
		return sc.listAll(reference, mailbox);
	}

	@Override
	public ListResult lsub(String reference, String mailbox)
			throws IOException, StoreException {
		return sc.listSubscribed(reference, mailbox);
	}

	@Override
	public boolean select(String mailbox) throws IOException, StoreException {
		boolean ret = false;

		try {
			ret = sc.select(mailbox);
		} catch (IMAPException e) {
			throw new StoreException(e.getMessage(), e);
		}
		return ret;
	}

	@Override
	public void subscribe(String mailbox) throws IOException, StoreException {
		try {
			sc.subscribe(mailbox);
		} catch (IMAPException e) {
			throw new StoreException(e.getMessage(), e);
		}
	}

	@Override
	public void unsubscribe(String mailbox) throws IOException, StoreException {
		try {
			sc.unsubscribe(mailbox);
		} catch (IMAPException e) {
			throw new StoreException(e.getMessage(), e);
		}
	}

	@Override
	public QuotaInfo getQuota(String mailbox) {
		return sc.quota(mailbox);
	}

	@Override
	public Collection<Long> uidCopy(Collection<Long> set, String mailbox) throws IOException,
			StoreException {
		return sc.uidCopy(set, mailbox);
	}

	@Override
	public InputStream uidFetchPart(long uid, String address)
			throws IOException, StoreException {
		return sc.uidFetchPart(uid, address);
	}

	@Override
	public Collection<org.minig.imap.mime.MimeTree> uidFetchBodystructure(Collection<Long> set)
			throws IOException, StoreException {
		return sc.uidFetchBodyStructure(set);
	}

	@Override
	public Collection<FlagsList> uidFetchFlags(Collection<Long> set) throws IOException,
			StoreException {
		return sc.uidFetchFlags(set);
	}

	@Override
	public Collection<IMAPHeaders> uidFetchHeaderFields(Collection<Long> set, String[] fields)
			throws IOException, StoreException {
		return sc.uidFetchHeaders(set, fields);
	}

	@Override
	public InputStream uidFetchMessage(long uid) throws IOException,
			StoreException {
		return sc.uidFetchMessage(uid);
	}

	@Override
	public Collection<Long> uidSearch(SearchQuery sq) throws IOException, StoreException {
		return sc.uidSearch(sq);
	}

	@Override
	public void uidStore(Collection<Long> uids, FlagsList flags, boolean set)
			throws IOException, StoreException {
		sc.uidStore(uids, flags, set);
	}

	@Override
	public void destroy() {
		try {
			sc.logout();
		} catch (IMAPException e) {
		}
	}

	@Override
	public boolean keepAlive() {
		return sc.noop();
	}

	@Override
	public List<MailThread> uidThreads() throws IOException, StoreException {
		return sc.uidThreads();
	}

	@Override
	public void renameFolder(String mailbox, String newDisplayName)
			throws StoreException {
		try {
			sc.rename(mailbox, newDisplayName);
		} catch (IMAPException e) {
			throw new StoreException(e.getMessage(), e);
		}
	}

	@Override
	public Collection<Envelope> uidFetchEnvelopes(Collection<Long> set) throws IOException,
			StoreException {
		return sc.uidFetchEnvelope(set);
	}

	@Override
	public NameSpaceInfo namespace() {
		return sc.namespace();
	}
	
}
