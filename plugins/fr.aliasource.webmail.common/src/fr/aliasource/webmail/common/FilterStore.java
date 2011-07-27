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

package fr.aliasource.webmail.common;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import org.minig.imap.SieveClient;
import org.minig.imap.sieve.SieveScript;

/**
 * {@link SieveClient} delegate
 * 
 * @author tom
 *
 */
public class FilterStore {

	private SieveClient sc;

	public FilterStore(SieveClient sc) {
		this.sc = sc;
	}

	public boolean deletescript(String name) {
		return sc.deletescript(name);
	}

	public String getScript(String name) {
		return sc.getScript(name);
	}

	public List<SieveScript> listscripts() {
		return sc.listscripts();
	}

	public boolean login() {
		return sc.login();
	}

	public void logout() {
		sc.logout();
	}

	public boolean putscript(String name, InputStream scriptContent) {
		return sc.putscript(name, scriptContent);
	}

	public void replaceActiveScript(InputStream content) {
		List<SieveScript> scripts = listscripts();
		for (SieveScript ss : scripts) {
			deletescript(ss.getName());
		}
		String newName = "minig-"+UUID.randomUUID().toString()+".sieve";
		putscript(newName, content);
		activate(newName);
	}

	public void activate(String newName) {
		sc.activate(newName);
	}
	
}
