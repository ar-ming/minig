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

package fr.aliasource.webmail.sending;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPException;
import org.columba.ristretto.smtp.SMTPProtocol;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPHeaders;

import com.google.common.collect.Iterables;

import fr.aliasource.webmail.book.BookActivator;
import fr.aliasource.webmail.book.MinigContact;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.SendParametersFactory;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.Mime4jFormatter;
import fr.aliasource.webmail.common.message.SendParameters;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.XmlMailMessageParser;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

/**
 * Uses SMTP to send the message. This class also handles mail address
 * gathering.
 * 
 * @author tom
 * 
 */
public class SendAction extends AbstractControlledAction {

	private Log logger;
	private static final FlagsList ANSWERED = new FlagsList();

	public SendAction() {
		logger = LogFactory.getLog(getClass());
		ANSWERED.add(Flag.ANSWERED);
	}

	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String xmlMessage = req.getParameter("message");

		// reply infos
		String uid = req.getParameter("uid");
		String folder = req.getParameter("folder");
		SendParameters sp = SendParametersFactory.createFromParamsMap(req);
		logger.info(sp.toString());

		IAccount ac = p.getAccount();
		MailMessage mm = new XmlMailMessageParser().parse(xmlMessage);
		List<Address> recipients = getAllRistrettoRecipients(mm);

		try {
			SMTPProtocol smtp = new SMTPProtocol(ac.getTransportHost());
			Mime4jFormatter mf = new Mime4jFormatter(ac);

			smtp.openPort();
			smtp.ehlo(InetAddress.getLocalHost());
			smtp.mail(ristrettoAddr(mm.getSender()));

			for (Address to : recipients) {
				logger.info("to.display: " + to.getDisplayName() + " mail: "
						+ to.getMailAddress());
				Address cleaned = new Address(to.getMailAddress());
				smtp.rcpt(cleaned);
			}

			if (uid != null && folder != null) {
				String[] replyInformations = getOrigMessageId(ac, folder, uid);
				String messageId = replyInformations != null ? replyInformations[0] : null;
				String references = buildReferences(replyInformations);
				smtp.data(mf.format(mm, messageId, references, sp));
			} else {
				smtp.data(mf.format(mm, sp));
			}
			smtp.quit();

		} catch (SMTPException se) {
			logger.error(se.getMessage());
			responder.sendError(se.getMessage());
			return;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError("[mail-send-error] " + e.getMessage());
			return;
		}

		try {
			collectAddresses(ac, recipients);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}

		responder.sendString("OK");

	}

	private String buildReferences(String[] replyInformations) {
		if (replyInformations != null && replyInformations[0] != null) {
			if (replyInformations[1] != null) {
				return replyInformations[1] + " " + replyInformations[0];
			} else {
				return replyInformations[0];
			}
		}
		return null;
	}

	private void collectAddresses(IAccount account, List<Address> recipients) {
		List<MinigContact> ctx = new ArrayList<MinigContact>();
		for (Address a : recipients) {
			ctx.add(new MinigContact(a.getDisplayName().trim(), null, a
					.getMailAddress().trim(), "", "", "", "", "", ""));
		}
		BookActivator.getDefault().getBookManager().insert(account, ctx);
	}

	public List<org.columba.ristretto.message.Address> getAllRistrettoRecipients(
			MailMessage mm) {
		List<org.minig.imap.Address> to = mm.getTo();
		List<org.minig.imap.Address> cc = mm.getCc();
		List<org.minig.imap.Address> bcc = mm.getBcc();
		List<org.columba.ristretto.message.Address> recipients =
			new ArrayList<org.columba.ristretto.message.Address>(to.size() + cc.size() + bcc.size());
		
		for (org.minig.imap.Address addr: Iterables.concat(to, cc, bcc)) {
			recipients.add(ristrettoAddr(addr));
		}
		return recipients;
	}

	private org.columba.ristretto.message.Address ristrettoAddr(
			org.minig.imap.Address a) {
		return new org.columba.ristretto.message.Address(a.getDisplayName(), a
				.getMail());
	}

	private String[] getOrigMessageId(IAccount ac, String folder, String uid) {
		String[] response = null;
		List<Long> id = Arrays.asList(Long.parseLong(uid));
		IStoreConnection isc = ac.getStoreProtocol();
		try {
			isc.select(folder);
			Collection<IMAPHeaders> heads = isc.uidFetchHeaderFields(id,
					new String[] { "message-id" , "references" });
			if (heads.size() == 1) {
				IMAPHeaders headers = heads.iterator().next();
				response = new String[] {
						headers.getRawHeader("message-id"),
						headers.getRawHeader("references")};
			}
			isc.uidStore(id, ANSWERED, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			isc.destroy();
		}
		return response;
	}

	public String getUriMapping() {
		return "/sendMessage.do";
	}

}
