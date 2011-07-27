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

public class CustomerStoreTests extends LoggedTestCase {

	public void testSelect() throws IMAPException {
		sc.select("INBOX");
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

	// /**
	// * Loads specific uid's in my mailbox with complex trees
	// *
	// * @throws IMAPException
	// */
	// public void testUidFetchBodyStructureBroken() throws IMAPException {
	// MimeTree[] mts = null;
	// sc.select("INBOX");
	// try {
	// mts = sc.uidFetchBodyStructure(new long[] { 47339 });
	// if (mts.length == 1) {
	// System.out.println("tree:\n" + mts[0].toString());
	// } else {
	// System.out.println("uid 47339 not found");
	// }
	// } catch (Throwable t) {
	// t.printStackTrace();
	// fail();
	// }
	//
	// }

	public void testUidSearch() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		assertNotNull(uids);
		assertFalse(uids.isEmpty());
		System.err.println("found " + uids.size() + " in INBOX");
	}

	public void testUidFetchHeaders() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		String[] headers = new String[] { "date", "from", "subject",
				"message-id" };

		long nstime = System.nanoTime();
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());
		System.out.println("fetchHeaders for " + uids.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");

		for (IMAPHeaders header : h) {
			if (header != null) {
				System.out.println(header.getUid()+" Subject: " + header.getSubject()+" "+header.getFrom());
			}
		}

	}

	public void testUidFetchHeadersOneByOne() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		String[] headers = new String[] { "date", "from", "subject",
				"message-id" };

		long nstime = System.nanoTime();
		for (long l : uids) {
			Collection<IMAPHeaders> h = sc.uidFetchHeaders(Arrays.asList(l), headers);
			if (h.size() == 1) {
				IMAPHeaders header = h.iterator().next();
				System.out.println("[" + l + "] Subject: "
						+ header.getSubject() + " Date: " + header.getDate()
						+ " FromMail: " + header.getFrom());
			} else {
				System.err.println("could not read headers for uid " + l);
				fail("could not read headers for uid " + l);
			}
		}
		nstime = System.nanoTime() - nstime;
		System.out.println("fetchHeaders for " + uids.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");

	}

	public void testUidFetches() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		String[] headers = new String[] { "date", "from", "subject",
				"message-id" };

		long nstime = System.nanoTime();
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());
		System.out.println("fetchHeaders for " + uids.size() + " uids took "
				+ nstime + "ns (" + (nstime / 1000000) + "ms)");

		Iterator<Long> it = uids.iterator(); 
		for (int i = 0; i < uids.size(); i++) {
			Long current = it.next();
			try {
				Collection<MimeTree> ret = sc.uidFetchBodyStructure(Arrays.asList(current));
				if (i % 100 == 0) {
					System.out.println(i + " out of " + uids.size());
				}
				System.out.println(ret.iterator().next());
			} catch (Throwable t) {
				t.printStackTrace();
				fail("error for uid " + current);
			}
		}

	}

	public void testUidFetchFlags() throws IMAPException {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());
		
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
