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

package fr.aliasource.webmail.common.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.cache.JDBCCacheCallback;
import org.minig.cache.RowMapper;

import fr.aliasource.utils.JDBCUtils;
import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.settings.Signature;

/**
 * This class is responsible for caching the signature
 * 
 * @author adrien, matthieu 
 * 
 */
public class SignatureCache extends DatabaseCache<List<Signature>> {

	protected Log logger;

	public static String TABLE_NAME = "minig_signature";


	public SignatureCache(IAccount account) {
		super(account,null,TABLE_NAME);
		this.logger = LogFactory.getLog(getClass());
	}



	@Override
	protected void writeCacheImpl(final List<Signature> data) {
		getJDBCCacheTemplate().execute(new JDBCCacheCallback() {
			@Override
			public void execute(Connection con, int cacheId)
					throws SQLException {
					PreparedStatement delete = null;
					PreparedStatement insert = null;
					try {
						delete = con.prepareStatement("DELETE FROM "+TABLE_NAME+" WHERE minig_cache = ?");
						delete.setInt(1, cacheId);
						delete.executeUpdate();
						for (Signature entry : data) {
							insert = con
									.prepareStatement("INSERT INTO "+TABLE_NAME+" (email,signature,minig_cache) VALUES (?,?,?)");
							insert.setString(1, entry.getEmail());
							insert.setString(2, entry.getSignature());
							insert.setInt(3, cacheId);
							logger.info("saving signature for " + entry.getEmail() + "\n"
									+ entry.getSignature());
							insert.executeUpdate();
						}
					} finally {
						JDBCUtils.cleanup(null, delete, null);
						JDBCUtils.cleanup(null, insert, null);
					}
			}});
	}

	@Override
	protected List<Signature> loadFromCache() {
		List<Signature> signatures = 
			getJDBCCacheTemplate().query("SELECT email,signature FROM "+TABLE_NAME+" "
					,new RowMapper<Signature>(){
						@Override
						public Signature  mapRow(ResultSet rs, int rowNum) throws SQLException{
							return new Signature(rs.getString(1), rs.getString(2));
							
						}
			});

		return signatures;
	}

}
