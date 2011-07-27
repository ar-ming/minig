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

package org.minig.backend.identity;

import java.util.LinkedList;
import java.util.List;

public class IdentityManager {

	private List<IIdentitySourceFactory> factories;

	IdentityManager(List<IIdentitySourceFactory> factories) {
		this.factories = factories;
	}

	public List<Identity> getIdentities(String userEmail) {
		List<Identity> ret = new LinkedList<Identity>();

		for (IIdentitySourceFactory factory : factories) {
			IIdentitySource source = factory.getIdentitySource(userEmail);
			if(source!=null){
			try {
				ret.addAll(source.getIdentities());
			} finally {
					source.release();
			}
		}
		}

		return ret;
	}

}
