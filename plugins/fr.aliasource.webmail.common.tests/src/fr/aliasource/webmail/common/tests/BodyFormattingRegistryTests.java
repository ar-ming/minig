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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import fr.aliasource.webmail.common.conversation.BodyFormattingRegistry;
import fr.aliasource.webmail.common.conversation.MailBody;

public class BodyFormattingRegistryTests extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCtor() {
		MailBody mb = new MailBody("text/plain", "blabla");
		BodyFormattingRegistry bfr = new BodyFormattingRegistry(mb);
		assertNotNull(bfr);
	}

	private MailBody formatHtml(String html) {
		MailBody mb = new MailBody("text/html", html);
		BodyFormattingRegistry bfr = new BodyFormattingRegistry(mb);
		bfr.format();
		// for (String format : mb.availableFormats()) {
		// System.out.println("available format: " + format);
		// }
		assertTrue(mb.availableFormats().contains("text/cleanHtml"));
		return mb;
	}

	public void testCleaner() {
		MailBody mb = null;
		String fmt = "text/cleanHtml";
		
		mb = formatHtml("<html><body>blabla</body>");
		System.out.println("cleaned:\n" + mb.getValue(fmt));
		assertClean(mb.getValue(fmt));

		mb = formatHtml("<html><body>blabla<img src=\"youporn.jpg\"/></body>");
		System.out.println("cleaned:\n" + mb.getValue(fmt));
		assertClean(mb.getValue(fmt));

		mb = formatHtml("<html><body><script>while (true) {}</script>blabla<img src=\"youporn.jpg\"/></body>");
		System.out.println("cleaned:\n" + mb.getValue(fmt));
		assertClean(mb.getValue(fmt));
	}

	private void assertPartialClean(String value) throws AssertionFailedError {
		assertFalse(value.contains("<script"));
		assertFalse(value.contains("<style"));
	}

	private void assertClean(String value) {
		assertPartialClean(value);
		assertFalse(value.contains("<img"));
	}

	public void testPartialCleaner() {
		MailBody mb = null;
		String fmt = "text/partialCleanHtml";

		mb = formatHtml("<html><body>blabla</body>");
		System.out.println("partial cleaned:\n" + mb.getValue(fmt));
		assertPartialClean(mb.getValue(fmt));
		
		mb = formatHtml("<html><body>blabla<img src=\"youporn.jpg\"/></body>");
		System.out.println("cleaned:\n" + mb.getValue(fmt));
		assertPartialClean(mb.getValue(fmt));

		mb = formatHtml("<html><body>blabla<a target=\"here\" href=\"titi\">blabla</a><img src=\"youporn.jpg\"/></body>");
		System.out.println("cleaned:\n" + mb.getValue(fmt));
		assertPartialClean(mb.getValue(fmt));
	}
}
