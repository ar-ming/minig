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

package fr.aliasource.webmail.common.tests;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.minig.imap.SearchQuery;

import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.MailMessageLoader;

public class MailLoaderTests extends WebmailTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFormat() {
		MailMessageLoader loader = new MailMessageLoader(account
				.getAttachementManager(), new IMAPFolder("INBOX"));
		IStoreConnection store = account.getStoreProtocol();
		try {
			loader.select(store);
			MailMessage mm = loader.fetch(account, new MessageId(4765), store, true);
			if (mm != null) {
				System.out.println("Got subject: " + mm.getSubject());
				MailBody body = mm.getBody();
				String plain = body.getValue("text/plain");
				System.out.println("plain:\n" + plain + "\n------------");
				String html = body.getValue("text/html");
				System.out.println("html:\n" + html + "\n------------");

				mm = loader.fetch(account, new MessageId(5302), store, true);
				assertNotNull(mm);
				System.out.println("Got subject: " + mm.getSubject());
				body = mm.getBody();
				plain = body.getValue("text/plain");
				System.out.println("plain:\n" + plain + "\n------------");
				html = body.getValue("text/html");
				System.out.println("html:\n" + html + "\n------------");
			} else {
				System.err.println("No message 4765 in your INBOX");
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			store.destroy();
		}
	}

	private MailMessage testLoad(String folder, long uid) throws Throwable {
		return testLoad(folder, uid, false);
	}

	private MailMessage testLoad(String folder, long uid, boolean fetchForwards)
			throws Throwable {
		MailMessageLoader loader = new MailMessageLoader(account
				.getAttachementManager(), new IMAPFolder(folder));
		IStoreConnection store = account.getStoreProtocol();
		MailMessage mm = null;
		try {
			loader.select(store);
			mm = loader.fetch(account, new MessageId(uid), store, !fetchForwards);
		} catch (Throwable t) {
			System.err.println("error on uid "+uid);
			fail();
		} finally {
			store.destroy();
		}
		return mm;
	}

	public void testLoadInbox172180() {
		MailMessage mm = null;
		try {
			mm = testLoad("INBOX", 172180);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
		if (mm != null) {

		} else {
			System.err.println("No mailmessage in INBOX with uid " + 172180);
		}
	}

	public void testLoadMinigBrokenFolder() {
		IStoreConnection store = account.getStoreProtocol();
		String folder = "Dossiers partagés/minigbroken";
		Collection<Long> uids = Collections.emptyList();
		try {
			boolean success = store.select(folder);
			if (!success) {
				System.out.println("cannot select " + folder);
				return;
			}
			uids = store.uidSearch(new SearchQuery());
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		} finally {
			store.destroy();
		}

		for (long uid : uids) {
			try {
				testLoad(folder, uid);
			} catch (Throwable e) {
				System.err.println("error on uid " + uid);
				e.printStackTrace();
				fail();
			}
		}
	}

	public void testInbox() {
		IStoreConnection store = account.getStoreProtocol();
		String folder = "Dossiers partagés/obm-dev";
		Collection<Long> uids = Collections.emptyList();
		try {
			boolean success = store.select(folder);
			if (!success) {
				System.out.println("cannot select " + folder);
				return;
			}
			uids = store.uidSearch(new SearchQuery());
			System.err.println("uid list length: " + uids.size());
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		} finally {
			store.destroy();
		}

		long time = System.nanoTime();
		for (long uid : uids) {
			try {
				testLoad(folder, uid);
			} catch (Throwable e) {
				System.err.println("error on uid " + uid);
				e.printStackTrace();
				fail();
			}
		}
		time = System.nanoTime() - time;
		System.err.println("loaded " + uids.size() + " msgs in "
				+ (time / (1000 * 1000)) + "ms.");
	}

	public void testInboxUid393() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 393);
			MailBody body = mm.getBody();
			for (String s : body.availableFormats()) {
				System.err.println("format: "+s);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid8() {
		String folder = "INBOX";
		try {
			testLoad(folder, 8);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid3696() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 3696);
			if (mm != null) {
				Map<String, String> atts = mm.getAttachements();

				System.err.println("att list start");
				for (String s : atts.keySet()) {
					System.err.println("att " + s);
				}
				System.err.println("att list end");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid3() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 3);
			if (mm != null) {
				MailBody b = mm.getBody();
				for (String format : b.availableFormats()) {
					System.out.println("=== format: " + format);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid356() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 356);
			if (mm != null) {
				MailBody b = mm.getBody();
				System.err.println("attachments: "
						+ mm.getAttachements().size());
				for (String format : b.availableFormats()) {
					System.err.println("=== format: " + format);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid523() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 523);
			if (mm != null) {
				System.err.println(mm.getSubject());
				MailBody b = mm.getBody();
				System.err.println("attachments: "
						+ mm.getAttachements().size());
				for (String k : mm.getAttachements().keySet()) {
					System.err.println("attachKey: "+k+" v: "+mm.getAttachements().get(k));
				}
				for (String format : b.availableFormats()) {
					System.err.println("=== format: " + format);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid3711() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 3711);
			if (mm != null) {
				MailBody b = mm.getBody();
				for (String format : b.availableFormats()) {
					System.out.println("=== format: " + format);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid3709() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 3709);
			if (mm != null) {
				MailBody b = mm.getBody();
				for (String format : b.availableFormats()) {
					System.out.println("=== format: " + format);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid3916() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 3916, true);
			if (mm != null) {
				MailBody b = mm.getBody();
				for (String format : b.availableFormats()) {
					System.out.println("=== format: " + format);
				}
				Set<MailMessage> forwards = mm.getForwardMessage();
				System.out.println("forwards: " + forwards.size());
				for (MailMessage rfc822 : forwards) {
					System.out.println("   * forward: " + rfc822.getSubject()
							+ " formats: "
							+ rfc822.getBody().availableFormats().size());
					System.out.println("        * child count: "
							+ rfc822.getForwardMessage().size());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid195851() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 195851);
			if (mm != null) {
				MailBody b = mm.getBody();
				for (String format : b.availableFormats()) {
					System.out.println("=== format: " + format);
					System.out.println("content:\n" + b.getValue(format));
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInboxUid2() {
		String folder = "INBOX";
		try {
			MailMessage mm = testLoad(folder, 4);
			if (mm != null) {
				System.out.println("subject: " + mm.getSubject());
				Set<MailMessage> fwds = mm.getForwardMessage();
				if (fwds != null && !fwds.isEmpty()) {
					for (MailMessage fwd : fwds) {
						System.out.println("  forwarded: " + fwd
								+ "\n      with subject: " + fwd.getSubject());
					}
				}
				MailBody b = mm.getBody();
				for (String format : b.availableFormats()) {
					System.out.println("=== format: " + format);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testBrokenUid7() {
		String folder = "Dossiers partagés/minigbroken";
		try {
			testLoad(folder, 7);
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

}
