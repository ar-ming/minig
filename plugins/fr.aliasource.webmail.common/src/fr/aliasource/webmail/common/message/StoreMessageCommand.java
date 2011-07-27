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

package fr.aliasource.webmail.common.message;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;

import fr.aliasource.index.core.Hit;
import fr.aliasource.index.core.SearchDirector;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IMoveConversation;
import fr.aliasource.webmail.common.IMoveMessage;
import fr.aliasource.webmail.common.IStoreMessage;
import fr.aliasource.webmail.common.cache.ConversationCache;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;

public class StoreMessageCommand implements IStoreMessage, IMoveConversation,
		IMoveMessage {

	private IAccount account;
	private Log logger;

	private static ExecutorService expunger;

	static {
		expunger = Executors.newFixedThreadPool(Runtime.getRuntime()
				.availableProcessors());
	}

	public StoreMessageCommand(IAccount account) {
		this.logger = LogFactory.getLog(getClass());
		this.account = account;
	}

	@Override
	public String store(IFolder dest, MailMessage m, SendParameters sp) {

		InputStream formattedMessage = null;
		Mime4jFormatter formatter = new Mime4jFormatter(account);
		try {
			formattedMessage = formatter.format(m, sp);
		} catch (Throwable ioe) {
			logger.error(ioe.getMessage(), ioe);
			return null;
		}

		MessageId mid = null;

		IStoreConnection p = account.getStoreProtocol();
		try {
			FlagsList flags = getFlags(dest, m);
			long uid = p.append(dest.getName(), formattedMessage, flags);
			if (uid > 0) {
				mid = new MessageId(uid);
			} else {
				logger.error("Error storing message in " + dest.getName());
			}
		} catch (Throwable e) {
			logger.error("store(" + dest.getName() + ", m: " + m.getSubject()
					+ "') failed", e);
		} finally {
			p.destroy();
		}

		String convId = null;
		if (mid != null) {
			try {
				ConversationCache cc = account.getCache()
						.getConversationCache();
				account.getCache().getCacheManager().refresh(dest);
				convId = cc.findConversationId(dest, mid);
				logger.info("fastUpdate after move added " + convId);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return convId;
	}

	private FlagsList getFlags(IFolder dest, MailMessage m) {
		FlagsList fl = new FlagsList();
		fl.add(Flag.SEEN);
		return fl;
	}

	@Override
	public Set<String> copy(IFolder dest, Set<String> convIds) {
		return moveImpl(dest, convIds, false);
	}

	@Override
	public Set<String> move(IFolder dest, Set<String> convIds) {
		return moveImpl(dest, convIds, true);
	}

	private Set<String> moveImpl(IFolder dest, Set<String> convIds,
			boolean isMove) {
		logger.info("[" + account.getUserId() + "] "
				+ (isMove ? "move" : "copy") + convIds.size()
				+ " conversations to " + dest.getName() + "...");
		Set<IFolder> dirtyFolders = new HashSet<IFolder>();
		ConversationCache cc = account.getCache().getConversationCache();
		List<Collection<Long>> newUids = new LinkedList<Collection<Long>>();

		FlagsList deleted = new FlagsList();
		deleted.add(Flag.DELETED);
		for (String convId : convIds) {
			IFolder orig = cc.getOrigFolder(convId);

			if (isMove) {
				dirtyFolders.add(orig);
			}

			ConversationReference cr = cc.find(convId);
			if (cr == null) {
				continue;
			}
			IStoreConnection p = account.getStoreProtocol();
			try {
				p.select(orig.getName());
				Collection<Long> toMove = cr.getUidSequence();
				Collection<Long> info = p.uidCopy(toMove, dest.getName());
				if (!info.isEmpty()) {
					if (isMove) {
						p.uidStore(toMove, deleted, true);
					}

					// get one of the new uid to return the conversation id
					newUids.add(info);
				}
			} catch (Exception e) {
				logger.error("move failed " + e.getMessage(), e);
			} finally {
				p.destroy();
			}
		}

		dirtyFolders.add(dest);
		refreshCache(dirtyFolders, isMove);

		// find the allocated conversation ids of the new messages
		Set<String> newConvIds = new HashSet<String>();
		for (Collection<Long> i : newUids) {
			MessageId mid = new MessageId(i.iterator().next());
			newConvIds.add(cc.findConversationId(dest, mid));
		}

		return newConvIds;
	}

	private void expungeFolder(IFolder dirty) {
		long time = System.currentTimeMillis();
		IStoreConnection p = account.getStoreProtocol();
		try {
			p.select(dirty.getName());
			p.expunge();
		} catch (Exception e) {
			logger.error("expunge failed " + e.getMessage(), e);
		} finally {
			p.destroy();
		}
		time = System.currentTimeMillis() - time;
		logger.info("expunge of " + dirty.getName() + " took " + time + "ms.");
	}

	public Set<String> copy(String query, IFolder dest) {
		Set<String> uids = new HashSet<String>();
		SearchDirector sd = account.getSearchDirector();
		List<Hit> results = sd.findByType(account.getUserId(), query);
		for (Hit hit : results) {
			uids.add(hit.getPayload().get("id").toString());
		}
		return copy(dest, uids);
	}

	public Set<String> move(String query, IFolder dest) {
		Set<String> uids = new HashSet<String>();
		SearchDirector sd = account.getSearchDirector();
		List<Hit> results = sd.findByType(account.getUserId(), query);
		for (Hit hit : results) {
			uids.add(hit.getPayload().get("id").toString());
		}
		return move(dest, uids);
	}

	@Override
	public Set<String> moveMessage(IFolder dest, String convId,
			Collection<Long> messagesId) {
		return doMoveMessage(dest, convId, messagesId, true);
	}

	private Set<String> doMoveMessage(IFolder dest, String convId,
			Collection<Long> messagesId, boolean isMove) {
		logger.info("[" + account.getUserId() + "] "
				+ (isMove ? "move " : "copy ") + messagesId.size()
				+ " messages to " + dest.getName() + "...");
		Set<IFolder> dirtyFolders = new HashSet<IFolder>();
		ConversationCache cc = account.getCache().getConversationCache();
		List<Collection<Long>> newUids = new LinkedList<Collection<Long>>();

		FlagsList deleted = new FlagsList();
		deleted.add(Flag.DELETED);
		IFolder orig = cc.getOrigFolder(convId);

		if (isMove) {
			dirtyFolders.add(orig);
		}

		IStoreConnection p = account.getStoreProtocol();
		try {
			p.select(orig.getName());
			Collection<Long> info = p.uidCopy(messagesId, dest.getName());
			if (!info.isEmpty()) {
				if (isMove) {
					p.uidStore(messagesId, deleted, true);
				}
				// get one of the new uid to return the conversation id
				newUids.add(info);
			}
		} catch (Exception e) {
			logger.error("move failed " + e.getMessage(), e);
		} finally {
			p.destroy();
		}

		dirtyFolders.add(dest);
		refreshCache(dirtyFolders, isMove);

		// find the allocated conversation ids of the new messages
		Set<String> newConvIds = new HashSet<String>();
		for (Collection<Long> i : newUids) {
			MessageId mid = new MessageId(i.iterator().next());
			newConvIds.add(cc.findConversationId(dest, mid));
		}

		return newConvIds;
	}

	/**
	 * refresh cache for dirtyFolders
	 * 
	 * @param dirtyFolders
	 * @param isMove
	 */
	private void refreshCache(Set<IFolder> dirtyFolders, boolean isMove) {
		for (final IFolder dirty : dirtyFolders) {
			try {
				long time = System.currentTimeMillis();
				account.getCache().getCacheManager().refresh(dirty);
				time = System.currentTimeMillis() - time;
				logger.info("forced refresh on " + dirty.getName()
						+ " after move/copy took " + time + "ms.");
			} catch (Exception e) {
				logger
						.error("error refreshing cache for " + dirty.getName(),
								e);
			}
		}

		if (isMove) {
			// queue async expunge
			for (final IFolder dirty : dirtyFolders) {
				expunger.execute(new Runnable() {
					@Override
					public void run() {
						expungeFolder(dirty);
					}
				});
			}
		}
	}
}
