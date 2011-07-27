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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minig.backend.settings.ISettingsProvider;
import org.minig.backend.settings.ISettingsProviderFactory;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.settings.Signature;

public class IdentitySettingsProvider implements ISettingsProviderFactory {

	private IdentityManager manager;

	public IdentitySettingsProvider() {
		this.manager = Activator.getDefault().getManager();
	}

	private class IdentitySP implements ISettingsProvider {

		private String userId;
		private List<Signature> signatures;

		public IdentitySP(IAccount ac) {
			this.userId = ac.getUserId();
			if(userId.indexOf("@") == -1){
				userId += "@"+ac.getDomain();
			}
			this.signatures = ac.getCache().getSignatureCache().getCachedData();
			
		}

		@Override
		public String getCategory() {
			return "identities";
		}

		@Override
		public Map<String, String> getData() {
			List<Identity> ids = manager.getIdentities(userId);
			Map<String, String> ret = new HashMap<String, String>();
			for (int i = 0; i < ids.size(); i++) {
				String suffix = "";
				if (i > 0) {
					suffix = "_" + i;
				}
				Identity id = ids.get(i);
				ret.put("fullname" + suffix, id.getFullName());
				ret.put("email" + suffix, id.getEmail());
				ret.put("signature" + suffix, getSignature(id.getEmail()));
			}
			ret.put("nb_identities", Integer.toString(ids.size()));
			return ret;
		}

		@Override
		public void destroy() {
		}
		
		private String getSignature(String email){
			if(signatures!=null){
				for(Signature signature : signatures){
					if(signature.getEmail().equals(email)){
						return signature.getSignature();
					}
				}
			}
			return "";
		}

	}

	@Override
	public ISettingsProvider getProvider(IAccount ac) {
		return new IdentitySP(ac);
	}

}
