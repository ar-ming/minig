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
import org.minig.imap.IMAPHeaders;
import org.minig.imap.ListResult;
import org.minig.imap.NameSpaceInfo;
import org.minig.imap.QuotaInfo;
import org.minig.imap.SearchQuery;
import org.minig.imap.impl.MailThread;
import org.minig.imap.mime.MimeTree;

import fr.aliasource.webmail.pool.IPoolable;

/**
 * IMAP store abstraction API
 * 
 * @author tom
 * 
 */
public interface IStoreConnection extends IPoolable {

	public void uidStore(Collection<Long> uids, FlagsList flags, boolean set)
			throws IOException, StoreException;

	public void expunge() throws IOException, StoreException;

	public ListResult list(String reference, String mailbox)
			throws IOException, StoreException;

	public ListResult lsub(String reference, String mailbox)
			throws IOException, StoreException;

	public boolean select(String mailbox) throws IOException, StoreException;

	public void subscribe(String mailbox) throws IOException, StoreException;

	public void unsubscribe(String mailbox) throws IOException, StoreException;

	public QuotaInfo getQuota(String mailbox);

	public Collection<Long> uidCopy(Collection<Long> set, String mailbox) throws IOException,
			StoreException;

	public InputStream uidFetchPart(long uid, String address)
			throws IOException, StoreException;

	public Collection<MimeTree> uidFetchBodystructure(Collection<Long> set) throws IOException,
			StoreException;

	public Collection<FlagsList> uidFetchFlags(Collection<Long> set) throws IOException,
			StoreException;

	public Collection<IMAPHeaders> uidFetchHeaderFields(Collection<Long> set, String[] fields)
			throws IOException, StoreException;

	public Collection<Envelope> uidFetchEnvelopes(Collection<Long> set) throws IOException,
			StoreException;

	public InputStream uidFetchMessage(long uid) throws IOException,
			StoreException;

	public Collection<Long> uidSearch(SearchQuery sq) throws IOException, StoreException;

	public void create(String mailbox) throws IOException, StoreException;

	public long append(String name, InputStream formattedMessage,
			FlagsList flags) throws IOException, StoreException;

	public void delete(String mailbox) throws IOException, StoreException;

	public List<MailThread> uidThreads() throws IOException, StoreException;

	public void renameFolder(String fname, String newName)
			throws StoreException;

	public NameSpaceInfo namespace();
	
}
