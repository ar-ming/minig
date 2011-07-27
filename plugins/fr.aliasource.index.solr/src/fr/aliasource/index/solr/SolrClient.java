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

package fr.aliasource.index.solr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;

import fr.aliasource.index.core.Hit;
import fr.aliasource.index.core.Index;

/**
 * Remote solr indexes client implementation
 * 
 * @author tom
 * 
 */
public class SolrClient extends Index {

	private CommonsHttpSolrServer solrServer;

	private final String regexpDate = "after:[0-9]{4}-[0-9]{2}-[0-9]{2}|before:[0-9]{4}-[0-9]{2}-[0-9]{2}";
	private final Pattern p = Pattern.compile(regexpDate);

	public SolrClient(CommonsHttpSolrServer solrServer, String type) {
		super(type);
		this.solrServer = solrServer;
	}

	@Override
	public List<Hit> doQuery(String query) {
		Matcher m = p.matcher(query);
		String begin = "1969-01-01T00:00:00Z";
		String end = "2040-01-01T00:00:00Z";
		while (m.find()) {
			String s = query.substring(m.start(), m.end());
			if (s.startsWith("before:")) {
				end = s.split(":")[1] + "T23:59:59Z";
			}
			if (s.startsWith("after:")) {
				begin = s.split(":")[1] + "T00:00:00Z";
			}
		}

		String date = "date:[" + begin + " TO " + end + "]";
		StringBuilder sb = new StringBuilder();
		sb.append(date).append(' ').append(query.replaceAll(regexpDate, ""));
		String parsedQuery = sb.toString();

		if (logger.isDebugEnabled()) {
			logger.debug("doQuery(" + parsedQuery + ")");
		}

		List<Hit> ret = null;

		SolrParams params = createParams(parsedQuery);

		try {
			QueryResponse resp = solrServer.query(params);

			SolrDocumentList results = resp.getResults();
			if (logger.isDebugEnabled()) {
				logger.debug("SOLR query time for " + results.size()
						+ " results: " + resp.getElapsedTime() + "ms.");
			}

			ret = new ArrayList<Hit>(results.size());
			for (SolrDocument doc : results) {
				Map<String, Object> payload = doc.getFieldValueMap();
				Hit hit = new Hit(payload, getType());
				ret.add(hit);
			}
		} catch (SolrServerException e) {
			logger.error("Error querying server for '" + parsedQuery
					+ "' (type: " + getType() + "), url: "
					+ ClientUtils.toQueryString(params, false), e);
			ret = new LinkedList<Hit>();
		}

		return ret;
	}

	private SolrParams createParams(String query) {
		SolrQuery sq = new SolrQuery();
		sq.setQuery(query);
		sq.setFilterQueries("type:" + getType());
		sq.setIncludeScore(true);
		sq.setRows(Integer.MAX_VALUE);
		sq.addSortField("date", SolrQuery.ORDER.desc);
		return sq;
	}

	@Override
	public void doWrite(Map<String, String> data) {
		if (data.isEmpty()) {
			return;
		}

		String[] keys = { "id", "type", "body", "subject", "from", "to", "cc",
				"filename", "has", "in", "is", "date", "data" };
		for (String key : keys) {
			if (logger.isDebugEnabled()) {
				logger.debug("doWrite - " + key + " : " + data.get(key));
			}
			if (!data.containsKey(key)) {
				logger.error("Trying to push data to solr without " + key);
				return;
			}
		}

		SolrInputDocument doc = new SolrInputDocument();
		for (String key : keys) {
			String d = data.get(key);
			if (d != null) {
				doc.addField(key, d);
			}
		}

		try {
			solrServer.add(doc);
		} catch (Exception e) {
			logger.error("Error sending doc to solr", e);
		}
	}

	@Override
	public void commit() {
		try {
			solrServer.commit();
		} catch (Exception e) {
			logger.error("Error on commit", e);
		}
	}

	@Override
	public void optimize() {
		try {
			solrServer.optimize();
		} catch (Exception e) {
			logger.error("Error on optimize", e);
		}
	}

	@Override
	public void deleteById(String id) {
		try {
			solrServer.deleteById(id);
		} catch (Exception e) {
			logger.error("Error on deleteById(" + id + ")", e);
		}
	}

	@Override
	public void deleteByQuery(String query) {
		try {
			solrServer.deleteByQuery(query);
		} catch (Exception e) {
			logger.error("Error on deleteByQuery(" + query + ")", e);
		}
	}

}
