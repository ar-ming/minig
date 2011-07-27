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

package fr.aliasource.webmail.indexing;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.Address;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.aliasource.index.core.Hit;
import fr.aliasource.index.core.SearchDirector;
import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.ConversationReferenceList;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.cache.ConversationCache;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.conversation.SetConversationReference;
import fr.aliasource.webmail.common.conversation.VersionnedList;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class SearchAction extends AbstractControlledAction {

	private Log logger;

	public SearchAction() {
		logger = LogFactory.getLog(getClass());
		if (logger.isDebugEnabled()) {
			logger.debug("SearchAction created.");
		}
	}

	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		long time = System.currentTimeMillis();
		String query = req.getParameter("query");
		int page = Integer.parseInt(req.getParameter("page"));
		int pageLength = Integer.parseInt(req.getParameter("pageLength"));

		SearchDirector sd = p.getAccount().getSearchDirector();

		int startIdx = (page - 1) * pageLength;

		List<Hit> results = sd.findByType(p.getAccount().getUserId(), query);
		if (logger.isInfoEnabled()) {
			time = System.currentTimeMillis() - time;
			logger.info("[" + p.getAccount().getUserId() + "] " + query
					+ " p: " + page + " l: " + pageLength + " => "
					+ results.size() + " result(s) in " + time + "ms.");
		}
		int endIdx = Math.min(results.size(), startIdx + pageLength);
		int resultsSize = results.size();
		VersionnedList<ConversationReference> resultPage = new VersionnedList<ConversationReference>();
		if (startIdx < results.size()) {
			HashSet<String> notFoundInCache = new HashSet<String>();
			ConversationCache cc = p.getAccount().getCache()
					.getConversationCache();
			for (int i = startIdx; i < endIdx; i++) {
				Map<String, Object> payload = results.get(i).getPayload();
				String convId = payload.get("id").toString();
				ConversationReference cr = null;

				if (convId.contains("/")) {
					cr = cc.find(convId);
				} else {
					// chat ?
					cr = loadChat(payload);
				}

				if (cr != null) {
					resultPage.add(cr);
				} else {
					notFoundInCache.add(convId);
				}
			}
			if (notFoundInCache.size() > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("Messages with ids[");
				for (Iterator<String> it = notFoundInCache.iterator(); it
						.hasNext();) {
					sb.append(it.next());
					if (it.hasNext()) {
						sb.append(", ");
					}
				}
				sb.append("] found by solr are not in cache");
				logger.warn(sb.toString());
				resultsSize -= notFoundInCache.size();
			}
		}
		ConversationReferenceList ret = new ConversationReferenceList(
				resultPage, resultsSize);
		responder.sendConversationsPage(ret);
	}

	private ConversationReference loadChat(Map<String, Object> payload) {
		if (logger.isDebugEnabled()) {
			for (String s : payload.keySet()) {
				logger.debug(" * " + s + ": " + payload.get(s));
			}
		}
		String id = "" + payload.get("id");
		String xmlData = payload.get("data").toString();

		try {
			Document doc = DOMUtils.parse(new ByteArrayInputStream(xmlData
					.getBytes()));
			Element root = doc.getDocumentElement();

			NodeList nl = root.getElementsByTagName("item");
			Set<String> who = new HashSet<String>();
			StringBuilder preview = new StringBuilder();
			StringBuilder html = new StringBuilder();
			MessageBeautifier mb = new MessageBeautifier();
			for (int i = 0; i < nl.getLength(); i++) {
				html.append("<p><b>");
				Element e = (Element) nl.item(i);
				String from = e.getAttribute("from");
				html.append(from);
				html.append("</b>:&nbsp;");
				who.add(from);
				preview.append(e.getTextContent());
				preview.append(" ");
				html.append(mb.beautify(e.getTextContent()));
				html.append("</p>");
			}

			StringBuilder subject = new StringBuilder("Chat with ");
			LinkedList<Address> ads = new LinkedList<Address>();
			Iterator<String> it = who.iterator();
			for (int i = 0; it.hasNext(); i++) {
				String s = it.next();
				if (i > 0) {
					subject.append(", ");
				}
				int idx = s.indexOf("@");
				if (idx > 0) {
					String cut = s.substring(0, idx);
					subject.append(cut);
					ads.add(new Address(cut, s));
				} else {
					subject.append(s);
					ads.add(new Address(s));
				}
			}

			ConversationReference cr = new SetConversationReference(id, subject
					.toString(), "#chat");
			cr.setLastMessageDate(Long.parseLong(root.getAttribute("ts")));
			cr.addMetadata("preview", preview.toString());
			cr.addMetadata("html", html.toString());
			for (Address a : ads) {
				cr.addParticipant(a);
			}
			return cr;
		} catch (Exception e) {
			logger.error("Error loading chat from history");
		}

		return null;
	}

	public String getUriMapping() {
		return "/search.do";
	}

}
