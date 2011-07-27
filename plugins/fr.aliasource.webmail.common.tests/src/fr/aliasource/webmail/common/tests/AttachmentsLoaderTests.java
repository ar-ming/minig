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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.message.MailMessageLoader;

public class AttachmentsLoaderTests extends WebmailTestCase{
  
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
    long id = 7920;
    try {
      loader.select(store);
      MailMessage mm = loader.fetch(account, new MessageId(id), store, true);
      Set<MailMessage> mms = new HashSet<MailMessage>();
      mms.add(mm);

      if (mms.size() > 0) {
        testFwd(mms);
        System.out.println("********************************************************************************************");
      } else {
        System.err.println("No message "+id+" in your INBOX");
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    } finally {
      store.destroy();
    }
  }
  
  private void testFwd(Set<MailMessage> mms) {
    Iterator<MailMessage> it = mms.iterator();
    while(it.hasNext()) {
      MailMessage mm = it.next();
      if (mm != null) {
        System.out.println("Subject : " + mm.getSubject() + " ("+mm.getAttachements().size()+")");
        for (String attach : mm.getAttachements().keySet()) {
          System.out.println("\t" + attach + " - " + mm.getAttachements().get(attach));
        }
        Set<MailMessage> fwdMessage = mm.getForwardMessage();
        testFwd(fwdMessage);
      }
    }
  }
}
