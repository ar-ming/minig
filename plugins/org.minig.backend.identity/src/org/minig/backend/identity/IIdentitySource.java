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

import java.util.List;

public interface IIdentitySource {

	List<Identity> getIdentities();

	/**
	 * The identity source is not usable again after this call. This is
	 * mandatory when {@link IIdentitySourceFactory} holds a connection pool or
	 * something like that.
	 */
	void release();

}
