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

package org.minig.formatting.tests;

import fr.aliasource.webmail.common.conversation.MailBody;
import fr.aliasource.webmail.formatting.HTMLBodyFormatter;
import junit.framework.TestCase;

public class HTMLBodyFormatterTests extends TestCase{
  
  public void testUrls() {
    String plain = "my url is https://www.minig.org:8080 \n" +
        "and i am clickable ! www.obm.org is an url too. \n" +
        "http://www.minig.org/image/minig-logo,0101-170564-0-2-3-1-jpg-.html is a complex url \n" +
        "http://validator.w3.org/check?uri=http%3A%2F%2Fv2.linagora.org&charset=(detect+automatically)&doctype=Inline&group=0  is a very complex url \n" +
        "david@minig.test is an e-mail address \n" +
        "and david.phan@minig.test is an e-mail address too \n" +
        "david@minig.test and david.phan@minig.test are two addresses\n " +
        "this is an url : http://www.domain.tld/index.php?action=pouic&email=david@domain.tld";
    MailBody mb = new MailBody();
    mb.addConverted("text/plain", plain);

    HTMLBodyFormatter plb = new HTMLBodyFormatter();
    plb.addAlternateFormat(mb, null, null);
    String plainParsed = mb.getValue("text/html");

    System.out.println("text/html is:\n" + plainParsed);
    assertFalse(plainParsed.contains("skipped"));
  }
  
}
