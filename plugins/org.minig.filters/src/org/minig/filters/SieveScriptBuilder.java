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

package org.minig.filters;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.service.MinigForward;
import org.minig.obmsync.service.MinigVacation;

import fr.aliasource.utils.FileUtils;
import fr.aliasource.utils.IniFile;
import fr.aliasource.utils.JDBCUtils;

public class SieveScriptBuilder {

	private static final Log logger = LogFactory
			.getLog(SieveScriptBuilder.class);
	private IniFile ac;

	public SieveScriptBuilder() {
		ac = new IniFile("/etc/minig/account_conf.ini") {

			@Override
			public String getCategory() {
				return "account";
			}
		};
	}

	private String getTemplate(String name) {
		InputStream in = Activator.class.getClassLoader().getResourceAsStream(
				name);
		try {
			return FileUtils.streamString(in, true);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return "/* minig was unable to load template named '" + name
					+ "' */\n";
		}
	}

	public String createScript(Connection con, int cacheId, MinigVacation mv, MinigForward mf) throws SQLException {
		StringBuilder script = new StringBuilder(4096);
		
		
		insertTemplate(script, "templates/header.sieve");

		genVacation(script, mv);
		
		genForward(script, mf);
		
		genFilters(con, script, cacheId);

		insertTemplate(script, "templates/footer.sieve");
		return script.toString();
	}

	private void genForward(StringBuilder script, MinigForward mf) {
		if (!mf.isEnabled()) {
			logger.info("forward disabled in sieve script");
			return;
		}

		script.append("# OBM2 - Nomade\n");
		script.append("redirect \"");
		script.append(mf.getEmail());
		script.append("\";\n");
		if (!mf.isLocalCopy()) {
			script.append("discard;\nstop;\n\n");
		}
		script.append("# OBM2 - Nomade\n");
	}

	private void genVacation(StringBuilder script, MinigVacation mv) {
		if (!mv.isEnabled()) {
			logger.info("vacation disabled in sieve script");
			return;
		}
		
		script.append("# OBM2 - Vacation\n");
		script.append("if not header :contains \"Precedence\" [\"bulk\", \"list\"] {\n");
		script.append("\tvacation :days 4 \"");
		String quoted = mv.getText().replace("\"", "\\\"");
		script.append(quoted);
		script.append("\";\n}\n\n");
		script.append("# OBM2 - Vacation\n");
		
	}

	private void genFilters(Connection con, StringBuilder script, int cacheId)
			throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		String q = "SELECT criteria, star, delete_it, mark_read, forward, deliver FROM minig_filters WHERE minig_cache = ?";
		try {
			ps = con.prepareStatement(q);
			ps.setInt(1, cacheId);
			rs = ps.executeQuery();

			while (rs.next()) {
				appendRule(script, rs);
			}

		} finally {
			JDBCUtils.cleanup(null, ps, rs);
		}
	}

	private void appendRule(StringBuilder script, ResultSet rs)
			throws SQLException {
		String allCrits = rs.getString(1);
		String[] crits = allCrits.split("\n"); // criteria
		boolean start = rs.getBoolean(2);
		boolean delete = rs.getBoolean(3);
		boolean markAsRead = rs.getBoolean(4);
		String forward = rs.getString(5);
		String deliver = rs.getString(6);

		String actionBlock = createAction(start, delete, markAsRead, forward,
				deliver);
		for (int i = 0; i < crits.length; i++) {
			String criterion = crits[i];
			if (i == 0) {
				script.append("\n# " + allCrits.replace("\n", " ")
						+ " \nif allof (");
			} else {
				script.append(",\n\t");
			}
			String cond = createCondition(criterion);
			script.append(cond);
		}
		script.append(") {\n");
		script.append(actionBlock);
		script.append("}\n");

	}

	private String createCondition(String criterion) {
		int idx = criterion.indexOf(": ");
		String value = criterion.substring(idx + 2);
		String crit = criterion.substring(0, idx);
		if (crit.equals("from")) {
			return "address :contains \"from\" \"" + value + "\" ";
		} else if (crit.equals("to")) {
			return "address :contains [ \"to\", \"cc\", \"bcc\" ] \"" + value
					+ "\" ";
		} else if (crit.equals("subject")) {
			return "header :contains  \"Subject\" \"" + value + "\" ";
		}

		else {
			logger.warn("unkown filter criterion: " + criterion);
			return "header :contains  \"X-MiniG\" \"Unknow criterion\"";
		}
	}

	private String createAction(boolean start, boolean delete,
			boolean markAsRead, String forward, String deliver) {
		StringBuilder sb = new StringBuilder();
		if (start) {
			sb.append("\tsetflag \"\\\\Flagged\";\n");
		}
		if (markAsRead) {
			sb.append("\tsetflag \"\\\\Seen\";\n");
		}
		if (forward != null) {
			sb.append("\tredirect \"" + forward + "\";\n");
		}
		if (delete) {
			sb.append("\tdiscard;\n");
		} else if (deliver != null) {
			sb.append("\tfileinto \"" + deliver + "\";\n");
		}
		sb.append("\tstop;\n");
		return sb.toString();
	}

	private void insertTemplate(StringBuilder script, String templateName) {
		String header = getTemplate(templateName);

		header = header.replace("${minig:spam}", ac.getData().get(
				"account.folders.spam"));

		script.append(header);
		script.append("\n");
	}

}
