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

package fr.aliasource.webmail.proxy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Envelope;

import fr.aliasource.utils.RunnableExtensionLoader;
import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.SendParametersFactory;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.SendParameters;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class ActionRegistry {

	private Controller c;
	private Log logger;

	public ActionRegistry(Controller c) {
		this.logger = LogFactory.getLog(getClass());
		this.c = c;
	}

	/**
	 * A reference on the connected clients is needed to implement logout
	 * 
	 * @param clients
	 */
	public void registerActions() {

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				String f = req.getParameter("folder");
				int page = Integer.parseInt(req.getParameter("page"));
				int pageLength = Integer.parseInt(req
						.getParameter("pageLength"));
				long version = Long.parseLong(req.getParameter("version"));
				ConversationReferenceList convs = p.listConversations(
						new IMAPFolder(f), page, pageLength);
				long backendVersion = convs.getPage().getVersion();
				if (backendVersion == version) {
					responder.sendNothingChanged();
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("version mismatch: front: " + version
								+ " back: " + backendVersion);
					}
					responder.sendConversationsPage(convs);
				}
			}

			public String getUriMapping() {
				return "/listConversations.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				c.logout(p);
			}

			public String getUriMapping() {
				return "/logout.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				String convId = req.getParameter("convId");
				ConversationReference cr = p.findConversation(convId);
				if (cr != null) {
					responder.sendConversation(cr);
				} else {
					responder.sendError("conversation " + convId
							+ " does not exist");
				}
			}

			public String getUriMapping() {
				return "/findConversation.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				IFolder f = new IMAPFolder(req.getParameter("folder"));
				String[] a = req.getParameter("messageIds").split(",");
				List<String> mList = Arrays.asList(a);
				List<MessageId> mids = new ArrayList<MessageId>(mList.size());
				for (String s : mList) {
					mids.add(new MessageId(Integer.parseInt(s)));
				}
				responder.sendMessages(p.fetchMessages(f, mids, true));
			}

			public String getUriMapping() {
				return "/fetchMessages.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				String convId = req.getParameter("convId");
				ConversationReference cr = p.findConversation(convId);
				Set<MessageId> allMess = cr.getMessageIds();
				List<MessageId> unread = new LinkedList<MessageId>();
				List<Long> read = new LinkedList<Long>();
				Map<Long, MessageId> uidMap = new HashMap<Long, MessageId>();

				for (Iterator<MessageId> it = allMess.iterator(); it.hasNext();) {
					MessageId m = it.next();
					if (m.isRead() && it.hasNext()) {
						read.add(m.getImapId());
						uidMap.put(m.getImapId(), m);
					} else {
						unread.add(m);
					}
				}

				logger.info("fetching " + unread.size() + " unread msgs and "
						+ read.size() + " read msgs from " + convId);

				IMAPFolder f = new IMAPFolder(cr.getSourceFolder());
				MailMessage[] toShow = p.fetchMessages(f, unread, true);
				
				// only fetch envelope for read messages
				IStoreConnection proto = p.getAccount().getStoreProtocol();
				List<MailMessage> toSummarize = new ArrayList<MailMessage>(
						read.size());
				try {
					proto.select(f.getName());
					Collection<Envelope> enveloppes = proto.uidFetchEnvelopes(read);
					for (Envelope e : enveloppes) {
						toSummarize.add(getShortMailMessage(uidMap.get(e.getUid()), e));
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					proto.destroy();
				}

				// merge the 2 list
				List<MailMessage> all = new ArrayList<MailMessage>(toSummarize
						.size() + toShow.length);
				all.addAll(toSummarize);
				all.addAll(Arrays.asList(toShow));
				Comparator<MailMessage> comp = new Comparator<MailMessage>() {
					@Override
					public int compare(MailMessage o1, MailMessage o2) {
						Long uid1 = o1.getUid();
						Long uid2 = o2.getUid();
						return uid1.compareTo(uid2);
					}
				};
				Collections.sort(all, comp);

				responder.sendMessages(cr,
						all.toArray(new MailMessage[all.size()]));
			}

			private MailMessage getShortMailMessage(MessageId id, Envelope e) {
				MailMessage m = new MailMessage();
				m.setCc(e.getCc());
				m.setTo(e.getTo());
				m.setSender(e.getFrom());
				m.setUid(e.getUid());
				m.setRead(true);
				m.setStarred(id.isStarred());
				m.setAnswered(id.isAnswered());
				m.setBody(null);
				m.setDate(e.getDate());
				return m;
			}

			public String getUriMapping() {
				return "/fetchUnreadMessages.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				IFolder f = new IMAPFolder(req.getParameter("folder"));
				Long uid = Long.parseLong(req.getParameter("uid"));

				InputStream ret = null;
				IStoreConnection con = p.getAccount().getStoreProtocol();
				try {
					con.select(f.getName());
					ret = con.uidFetchMessage(uid);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					con.destroy();
				}
				responder.sendStream(ret);
			}

			public String getUriMapping() {
				return "/downloadEml.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				String type = req.getParameter("type");
				String query = req.getParameter("query");
				int limit = Integer.parseInt(req.getParameter("limit"));
				responder.sendCompletions(p.getPossibleCompletions(type, query,
						limit));
			}

			public String getUriMapping() {
				return "/completion.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				IFolder dest = new IMAPFolder(req.getParameter("folder"));
				String xmlMessage = req.getParameter("message");
				XmlMailMessageParser xmmp = new XmlMailMessageParser();
				MailMessage mm = xmmp.parse(xmlMessage);

				SendParameters sp = SendParametersFactory.createFromParamsMap(req);
				logger.info(sp.toString());
				
				responder.sendConversationIds(new String[] { p.store(dest, mm, sp) });
			}

			public String getUriMapping() {
				return "/store.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				if (req.getParameter("conversations") != null) {
					String[] ids = req.getParameter("conversations").split(",");
					Set<String> convIds = new HashSet<String>();
					convIds.addAll(Arrays.asList(ids));
					String set = req.getParameter("set");
					String unset = req.getParameter("unset");
					if (set != null) {
						p.setFlags(convIds, set);
					} else {
						p.unsetFlags(convIds, unset);
					}
				} else {
					String set = req.getParameter("set");
					String unset = req.getParameter("unset");
					String query = req.getParameter("query");
					if (set != null) {
						p.setFlags(query, set);
					} else {
						p.unsetFlags(query, unset);
					}
				}
			}

			public String getUriMapping() {
				return "/flags.do";
			}
		});

		c.registerControlledAction(new AbstractControlledAction() {
			public void execute(IProxy p, IParameterSource req,
					IResponder responder) {
				String move = req.getParameter("move");
				logger.info("move param value: " + move);
				boolean isMove = "true".equals(req.getParameter("move"));

				Set<String> ret = null;
				if (req.getParameter("conversations") != null) {
					String[] ids = req.getParameter("conversations").split(",");
					Set<String> convIds = new HashSet<String>();
					convIds.addAll(Arrays.asList(ids));
					IFolder dest = new IMAPFolder(req
							.getParameter("destination"));
					if (isMove) {
						ret = p.moveConversation(dest, convIds);
					} else {
						ret = p.copy(dest, convIds);
					}
				} else {
					IFolder dest = new IMAPFolder(req
							.getParameter("destination"));
					String query = req.getParameter("query");
					if (isMove) {
						ret = p.moveConversation(query, dest);
					} else {
						ret = p.copy(query, dest);
					}
				}
				responder.sendConversationIds(ret.toArray(new String[ret.size()]));
			}

			public String getUriMapping() {
				return "/moveConversation.do";
			}
		});

		registerPluginActions();
	}

	private void registerPluginActions() {
		RunnableExtensionLoader<IControlledAction> loader = new RunnableExtensionLoader<IControlledAction>();
		List<IControlledAction> list = loader.loadExtensions(
				ProxyActivator.PLUGIN_ID, "controlledaction",
				"controlled_action", "implementation");

		for (IControlledAction ica : list) {
			c.registerControlledAction(ica);
			if (logger.isInfoEnabled()) {
				logger.info("registered action '" + ica.getUriMapping()
						+ "' from plugin.");
			}
		}
		if (logger.isInfoEnabled()) {
			logger.info("Registered " + list.size()
					+ " plugins provided actions in controller.");
		}
	}

}
