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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.minig.imap.impl.MailThread;
import org.minig.imap.mime.MimeTree;

import fr.aliasource.utils.FileUtils;

public class BasicStoreTests extends LoggedTestCase {

	private static final int COUNT = 50000;

	public void testSelect() throws IMAPException {
		sc.select("INBOX");
	}

	public void testSelectSpeed() throws IMAPException {
		long time;

		time = System.currentTimeMillis();

		for (int i = 0; i < COUNT; i++) {
			sc.select("INBOX");
		}

		time = System.currentTimeMillis() - time;
		System.out.println(COUNT + " iterations in " + time + "ms. "
				+ (time / COUNT) + "ms avg, " + 1000 / (time / COUNT)
				+ " per sec.");

	}

	public void testCapability() throws IMAPException {
		Set<String> caps = sc.capabilities();
		assertNotNull(caps);
		for (String s : caps) {
			System.out.print(s);
			System.out.print(" ");
		}
		System.out.println();
	}

	public void testCreateSubUnsubRenameDelete() throws IMAPException {
		String mbox = "test" + System.currentTimeMillis();
		String newMbox = "rename" + System.currentTimeMillis();
		try {
			boolean b = sc.create(mbox);
			assertTrue(b);
			boolean sub = sc.subscribe(mbox);
			assertTrue(sub);
			sub = sc.unsubscribe(mbox);
			assertTrue(sub);

			boolean renamed = sc.rename(mbox, newMbox);
			System.out.println("Rename success: " + renamed);
			boolean del = false;
			if (!renamed) {
				del = sc.delete(mbox);
			} else {
				del = sc.delete(newMbox);
			}
			assertTrue(del);

		} catch (IMAPException ime) {
			fail("error on mailbox creation");
		}
	}

	public void testNoop() {
		sc.noop();
	}

	public void testList() throws IMAPException {
		ListResult lr = sc.listAll("", "*");
		assertNotNull(lr);
		System.out.println("IMAP SEPARATOR: '" + lr.getImapSeparator() + "'");
		for (ListInfo li : lr) {
			System.out.println(" => " + li.getName() + " selectable: "
					+ li.isSelectable());
		}
	}

	public void testLsub() throws IMAPException {
		ListResult lr = sc.listSubscribed("", "*");
		assertNotNull(lr);
		System.out.println("IMAP SEPARATOR: '" + lr.getImapSeparator() + "'");
		for (ListInfo li : lr) {
			System.out.println(" => " + li.getName() + " selectable: "
					+ li.isSelectable());
			if (li.isSelectable()) {
				sc.select(li.getName());
			}
		}
	}

	public void testAppend() throws IMAPException {
		FlagsList fl = new FlagsList();
		fl.add(Flag.SEEN);
		long uid = sc.append("INBOX", getRfc822Message(), fl);
		assertTrue(uid > 0);
		long secondUid = sc.append("INBOX", getUtf8Rfc822Message(), fl);
		System.out.println("Added uids : " + uid + " " + secondUid);
		assertTrue(secondUid == uid + 1);
	}

	public void testUidFetchMessage() throws IMAPException {
		FlagsList fl = new FlagsList();
		fl.add(Flag.SEEN);
		long uid = sc.append("INBOX", getUtf8Rfc822Message(), fl);
		sc.select("INBOX");
		InputStream in = sc.uidFetchMessage(uid);
		assertNotNull(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			FileUtils.transfer(in, out, true);
			System.out.println("Received:\n" + out.toString());
		} catch (IOException e) {
			e.printStackTrace();
			fail("error");
		}

		long time = System.currentTimeMillis();

		for (int i = 0; i < COUNT; i++) {
			sc.uidFetchMessage(uid);
			out = new ByteArrayOutputStream();
			try {
				FileUtils.transfer(in, out, true);
			} catch (IOException e) {
				fail("error");
			}
		}

		time = System.currentTimeMillis() - time;
		System.out.println("time: " + time);
		System.out.println("FETCH: " + COUNT + " iterations in " + time
				+ "ms. " + (time / COUNT) + "ms avg, " + 1000
				/ ((time + 0.1) / COUNT) + " per sec.");
	}

	public void testNested() throws IMAPException {
		sc.select("INBOX");
		Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(5194l));
		if (mts.size() == 1) {
			System.out.println(mts.iterator().next().toString());
		}
	}

	/**
	 * Loads specific uid's in my mailbox with complex headers
	 * 
	 * @throws IMAPException
	 */
	public void testUidFetchHeadersBroken() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<IMAPHeaders> mts = sc.uidFetchHeaders(
					Arrays.asList(4947l, 5256l, 5011l, 4921l, 4837l), new String[] {
							"subject", "from" });
			for (IMAPHeaders h : mts) {
				System.out.println("uid: " + h.getUid() + " subject: "
						+ h.getSubject() + " from: " + h.getFrom().getMail());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	/**
	 * Loads specific uid's in my mailbox with complex trees
	 * 
	 * @throws IMAPException
	 */
	public void testUidFetchBodyStructureBroken() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(47339l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 47339 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}

	}

	public void testMiniBroken7() throws IMAPException {
		sc.select("Dossiers partagés/minigbroken");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(7l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 7 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox3() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(3l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 3 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox72() throws IMAPException {
		sc.select("Dossiers partagés/obm-dev");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(72l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 72 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox3709() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(3709l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 3709 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox414() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(414l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 414 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox3916() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(3916l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 3916 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox3711() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(3711l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 3711 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox356() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(356l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 356 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testInbox4() throws IMAPException {
		sc.select("INBOX");
		try {
			Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(4l));
			if (mts.size() == 1) {
				System.out.println("tree:\n" + mts.iterator().next().toString());
			} else {
				System.out.println("uid 4 not found");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail();
		}
	}

	public void testUidFetchBodyStructure() throws IMAPException {
		FlagsList fl = new FlagsList();
		fl.add(Flag.SEEN);
		Collection<Long> uid = Arrays.asList(
				sc.append("INBOX", getUtf8Rfc822Message(), fl),
				sc.append("INBOX", getRfc822Message(), fl));
		sc.select("INBOX");
		sc.uidFetchBodyStructure(uid);

		long time = System.currentTimeMillis();

		for (int i = 0; i < COUNT; i++) {
			sc.uidFetchBodyStructure(uid);
		}

		time = System.currentTimeMillis() - time;
		System.out.println("time: " + time);
		System.out.println("FETCH BS: " + COUNT + " iterations in " + time
				+ "ms. " + (time / COUNT) + "ms avg, " + 1000
				/ ((time + 0.1) / COUNT) + " per sec.");

		time = System.currentTimeMillis();
		sc.select("INBOX");
		Collection<Long> allUids = sc.uidSearch(new SearchQuery());
		for (long l : allUids) {
			try {
				sc.uidFetchBodyStructure(Arrays.asList(l));
			} catch (Throwable t) {
				System.err.println("Failure on uid: " + l);
				t.printStackTrace();
				Collection<IMAPHeaders> heads = sc.uidFetchHeaders(Arrays.asList(l),
						new String[] { "Subject" });
				System.out.println("subject: " + heads.iterator().next().getSubject());
				fail();
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("bs for " + allUids.size() + " messages took: "
				+ time + "ms");
	}

	public void testUidSearch() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		assertNotNull(uids);
		assertTrue(uids.size() > 0);

		long time = System.currentTimeMillis();

		for (int i = 0; i < COUNT; i++) {
			Collection<Long> u = sc.uidSearch(sq);
			assertTrue(u.size() > 0);
		}

		time = System.currentTimeMillis() - time;
		System.out.println("time: " + time);
		System.out.println("UID SEARCH: " + COUNT + " iterations in " + time
				+ "ms. " + (time / COUNT) + "ms avg, " + 1000
				/ ((time + 0.1) / COUNT) + " per sec.");
	}

	public void testUidFetchHeadersPerf() throws IMAPException {
		final String[] HEADS_LOAD = new String[] { "Subject", "From", "Date",
				"To", "Cc", "Bcc", "X-Mailer", "User-Agent", "Message-ID" };

		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(new SearchQuery());
		Iterator<Long> iterator = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(iterator.next(), iterator.next());

		long nstime = System.nanoTime();
		for (int i = 0; i < COUNT; i++) {
			Collection<IMAPHeaders> h = sc.uidFetchHeaders(firstTwo, HEADS_LOAD);
			assertNotNull(h);
		}
		nstime = System.nanoTime() - nstime;
		System.out.println("fetchHeaders for " + firstTwo.size()
				+ " uids took " + nstime + "ns (" + (nstime / 1000000000.0)
				+ "secs)");

	}

	public void testUidFetchHeaders() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		String[] headers = new String[] { "date", "from", "subject" };

		long nstime = System.nanoTime();
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());
		System.out.println("fetchHeaders for " + uids.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");

		for (IMAPHeaders header : h) {
			System.out.println("Subject: " + header.getSubject() + " Date: "
					+ header.getDate() + " FromMail: "
					+ header.getFrom().getMail() + " FromDisp: "
					+ header.getFrom().getDisplayName());
		}

	}

	public void testUidFetchHeadersSpeed() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		String[] headers = new String[] { "x-priority" };

		long time = System.currentTimeMillis();
		Collection<Long> uids = sc.uidSearch(sq);
		Collection<Envelope> e = sc.uidFetchEnvelope(uids);
		assertNotNull(e);
		assertEquals(uids.size(), e.size());
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		assertEquals(uids.size(), h.size());

		time = System.currentTimeMillis() - time;
		System.err.println("Done in " + time + "ms.");
	}

	public void testUidFetchEnvelopePerf() throws IMAPException {

		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(new SearchQuery());
		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());

		long nstime = System.nanoTime();
		for (int i = 0; i < COUNT; i++) {
			Collection<Envelope> h = sc.uidFetchEnvelope(firstTwo);
			assertNotNull(h);
		}
		nstime = System.nanoTime() - nstime;
		System.err.println("fetchEnv for " + firstTwo + " uids took " + nstime
				+ "ns (" + (nstime / 1000000000.0) + "secs)");

	}

	public void testUidFetchEnvelope() throws IMAPException {
		sc.select("INBOX");
		Collection<Long> one = Arrays.asList(280l);

		long nstime = System.nanoTime();
		Collection<Envelope> h = sc.uidFetchEnvelope(one);
		nstime = System.nanoTime() - nstime;
		assertEquals(one.size(), h.size());
		System.err.println("fetchEnv for " + one.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");

		for (Envelope e : h) {
			System.err.println("Subject: " + e.getSubject() + " Date: "
					+ e.getDate() + " FromMail: " + e.getFrom().getMail()
					+ " FromDisp: " + e.getFrom().getDisplayName());
		}

	}

	public void testUidFetchEnvelopeReliable() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		for (long l : uids) {
			try {
				Collection<Envelope> h = sc.uidFetchEnvelope(Arrays.asList(l));
				assertEquals(1, h.size());
			} catch (Throwable t) {
				System.err.println("failed on uid " + l);
				t.printStackTrace();
				fail();
			}
		}
	}

	public void testUidFetchFlags() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> iterator = uids.iterator();
		List<Long> firstTwo = Arrays.asList(iterator.next(), iterator.next());

		long nstime = System.nanoTime();
		Collection<FlagsList> h = sc.uidFetchFlags(firstTwo);
		nstime = System.nanoTime() - nstime;
		assertEquals(firstTwo.size(), h.size());
		System.out.println("fetchFlags for " + firstTwo.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");

		nstime = System.nanoTime();
		h = sc.uidFetchFlags(uids);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());
		System.out.println("fetchFlags for " + uids.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");
	}

	public void testUidCopy() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());

		long nstime = System.nanoTime();
		Collection<Long> result = sc.uidCopy(firstTwo, "Sent");
		nstime = System.nanoTime() - nstime;
		assertNotNull(result);
		assertEquals(firstTwo.size(), result.size());
		printLongCollection(result);
		System.out.println("uidCopy for " + firstTwo.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");
	}

	public void testUidStore() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());

		FlagsList fl = new FlagsList();
		fl.add(Flag.ANSWERED);
		long nstime = System.nanoTime();
		boolean result = sc.uidStore(firstTwo, fl, true);
		nstime = System.nanoTime() - nstime;
		assertTrue(result);
		System.out.println("uidStore for " + firstTwo.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");
		result = sc.uidStore(firstTwo, fl, false);
		assertTrue(result);
	}

	public void testUidFetchPartBroken() throws IMAPException {
		// allows test to be green bar when not running on my computer
		try {
			boolean selection = sc.select("Shared Folders/partage");
			if (!selection) {
				return;
			}
		} catch (IMAPException ime) {
			return;
		}

		Collection<MimeTree> mts = sc.uidFetchBodyStructure(Arrays.asList(1l));
		if (mts.size() == 1) {
			System.out.println("mts[0]" + mts.iterator().next().toString());
			InputStream part = sc.uidFetchPart(1, "1");
			try {
				FileUtils.dumpStream(part, System.err, true);
			} catch (IOException e) {
				e.printStackTrace();
				fail();
			}
		}
		// InputStream in = sc.uidFetchPart(uid, "1");
	}

	public void testUidThreads() throws IMAPException {
		sc.select("INBOX");
		List<MailThread> threads = sc.uidThreads();
		assertNotNull(threads);
		assertTrue(threads.size() > 0);
	}

	public void testUidFetchPart() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		long uid = uids.iterator().next();

		long nstime = System.nanoTime();
		InputStream in = sc.uidFetchPart(uid, "1");
		nstime = System.nanoTime() - nstime;
		System.out.println("uidFetchPart took took " + nstime + "ns ("
				+ (nstime / 1000000) + "ms)");
		assertNotNull(in);
		try {
			FileUtils.dumpStream(in, System.out, true);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Cannot dump part stream");
		}
	}

	public void testNamespace() {
		NameSpaceInfo nsi = sc.namespace();
		assertNotNull(nsi);
		System.out.println("perso: '" + nsi.getPersonal() + "'");
		System.out.println("other: '" + nsi.getOtherUsers() + "'");
		System.out.println("shared: '" + nsi.getMailShares() + "'");
	}

}
