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

import java.io.IOException;
import java.io.InputStream;

import fr.aliasource.utils.FileUtils;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.message.Mime4jFormatter;
import fr.aliasource.webmail.common.message.SendParameters;

public class MimeFormatterTests extends WebmailTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFormat() {
		MailMessage mm = getDummyMessage(new String[] { getMyMail() });
		Mime4jFormatter mf = new Mime4jFormatter(account);

		try {
			InputStream in = mf.format(mm, new SendParameters());
			FileUtils.dumpStream(in, System.err, true);
		} catch (IOException e) {
			e.printStackTrace();
			fail("should not happen");
		}

	}

}
