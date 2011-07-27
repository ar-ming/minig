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

package fr.aliasource.webmail.book;

import fr.aliasource.webmail.proxy.ProxyConfiguration;
import fr.aliasource.webmail.proxy.api.ICompletionSource;
import fr.aliasource.webmail.proxy.api.ICompletionSourceFactory;

public class CompletionSourceFactory implements ICompletionSourceFactory {

	private BookCompletionSource bcs;

	public CompletionSourceFactory() {
		
	}

	public ICompletionSource getInstance(String type) {
		return bcs;
	}

	public void init(ProxyConfiguration pc) {
		this.bcs = new BookCompletionSource();
	}

	public void shutdown() {
	}

	public boolean supports(String completionType) {
		return "emails".equals(completionType);
	}

}
