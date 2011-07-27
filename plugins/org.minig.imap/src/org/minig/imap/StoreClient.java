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

package org.minig.imap;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.minig.imap.impl.ClientHandler;
import org.minig.imap.impl.ClientSupport;
import org.minig.imap.impl.IResponseCallback;
import org.minig.imap.impl.MailThread;
import org.minig.imap.impl.StoreClientCallback;
import org.minig.imap.mime.MimeTree;

/**
 * IMAP client entry point
 * 
 * @author tom
 * 
 */
public class StoreClient {

	private String password;
	private String login;
	private int port;
	private String hostname;

	private Log logger = LogFactory.getLog(getClass());
	private ClientHandler handler;
	private ClientSupport cs;
	private SocketConnector connector;

	public StoreClient(String hostname, int port, String login, String password) {
		this.hostname = hostname;
		this.port = port;
		this.login = login;
		this.password = password;

		IResponseCallback cb = new StoreClientCallback();
		handler = new ClientHandler(cb);
		cs = new ClientSupport(handler);
		cb.setClient(cs);
		connector = new SocketConnector();
	}

	/**
	 * Logs into the IMAP store
	 * 
	 * @return true if login is successful
	 * @throws IMAPException
	 */
	public boolean login() throws IMAPException {
		return login(true);
	}
	
	/**
	 * Logs into the IMAP store
	 * 
	 * @return true if login is successful
	 * @throws IMAPException
	 */
	public boolean login(Boolean activateTLS) throws IMAPException {
		if (logger.isDebugEnabled()) {
			logger.debug("login attempt to " + hostname + ":" + port + " for "
					+ login + " / " + password);
		}

		SocketAddress sa = new InetSocketAddress(hostname, port);
		boolean ret = false;
		if (cs.login(login, password, connector, sa, activateTLS)) {
			ret = true;
		}
		return ret;
	}

	/**
	 * Logs out & disconnect from the IMAP server. The underlying network
	 * connection is released.
	 * 
	 * @throws IMAPException
	 */
	public void logout() throws IMAPException {
		if (logger.isDebugEnabled()) {
			logger.debug("logout attempt for " + login);
		}
		cs.logout();
	}

	/**
	 * Opens the given IMAP folder. Only one folder quand be active at a time.
	 * 
	 * @param mailbox
	 *            utf8 mailbox name.
	 * @throws IMAPException
	 */
	public boolean select(String mailbox) throws IMAPException {
		return cs.select(mailbox);
	}

	public boolean create(String mailbox) throws IMAPException {
		return cs.create(mailbox);
	}

	public boolean subscribe(String mailbox) throws IMAPException {
		return cs.subscribe(mailbox);
	}

	public boolean unsubscribe(String mailbox) throws IMAPException {
		return cs.unsubscribe(mailbox);
	}

	public boolean delete(String mailbox) throws IMAPException {
		return cs.delete(mailbox);
	}

	public boolean rename(String mailbox, String newMailbox)
			throws IMAPException {
		return cs.rename(mailbox, newMailbox);
	}

	/**
	 * Issues the CAPABILITY command to the IMAP server
	 * 
	 * @return
	 * @throws IMAPException
	 */
	public Set<String> capabilities() throws IMAPException {
		return cs.capabilities();
	}

	public boolean noop() {
		return cs.noop();
	}

	public ListResult listSubscribed(String reference, String mailbox) {
		return cs.listSubscribed(reference, mailbox);
	}

	public ListResult listAll(String reference, String mailbox) {
		return cs.listAll(reference, mailbox);
	}

	public long append(String mailbox, InputStream in, FlagsList fl) {
		return cs.append(mailbox, in, fl);
	}

	public void expunge() {
		cs.expunge();
	}

	public QuotaInfo quota(String mailbox) {
		return cs.quota(mailbox);
	}

	public InputStream uidFetchMessage(long uid) {
		return cs.uidFetchMessage(uid);
	}

	public Collection<Long> uidSearch(SearchQuery sq) {
		return cs.uidSearch(sq);
	}

	public Collection<MimeTree> uidFetchBodyStructure(Collection<Long> uids) {
		return cs.uidFetchBodyStructure(uids);
	}

	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids, String[] headers) {
		return cs.uidFetchHeaders(uids, headers);
	}

	public Collection<Envelope> uidFetchEnvelope(Collection<Long> uids) {
		return cs.uidFetchEnvelope(uids);
	}

	public Collection<FlagsList> uidFetchFlags(Collection<Long> uids) {
		return cs.uidFetchFlags(uids);
	}
	
	public InternalDate[] uidFetchInternalDate(Collection<Long> uids) {
		return cs.uidFetchInternalDate(uids);
	}

	public Collection<Long> uidCopy(Collection<Long> uids, String destMailbox) {
		return cs.uidCopy(uids, destMailbox);
	}

	public boolean uidStore(Collection<Long> uids, FlagsList fl, boolean set) {
		return cs.uidStore(uids, fl, set);
	}

	public InputStream uidFetchPart(long uid, String address) {
		return cs.uidFetchPart(uid, address);
	}

	public List<MailThread> uidThreads() {
		return cs.uidThreads();
	}

	public NameSpaceInfo namespace() {
		return cs.namespace();
	}

}
