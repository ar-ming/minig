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

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.response.SolrPingResponse;

import fr.aliasource.index.core.IIndexFactory;
import fr.aliasource.index.core.IIndexingParameters;
import fr.aliasource.index.core.Index;
import fr.aliasource.index.solr.impl.SelfOptimizingServer;

/**
 * Creates clients for the solr server indexes
 * 
 * @author tom
 *
 */
public class SolrClientFactory implements IIndexFactory {

	private String configuredUrl;
	private Log logger;
	private Map<String, SelfOptimizingServer> servers;
	
	public SolrClientFactory() {
		logger = LogFactory.getLog(getClass());
		servers = Collections.synchronizedMap(new HashMap<String, SelfOptimizingServer>());
	}

	@Override
	public Index getIndex(String type) {
		String serverUrl = configuredUrl;
		serverUrl = finalUrl(type, serverUrl);
		SelfOptimizingServer srv = null;
		synchronized (servers) {
			srv = servers.get(serverUrl);
			if (srv == null) {
				srv = initSrv(serverUrl, type);
				if (srv != null) {
					servers.put(serverUrl, srv);
				}
			}
		}
		return new SolrClient(srv, type);
	}
	
	private SelfOptimizingServer initSrv(String serverUrl, String type) {
		SelfOptimizingServer sos = null;
		
		try {
			sos = new SelfOptimizingServer(serverUrl);
		} catch (MalformedURLException e) {
			logger.error("Cannot init solr server ", e);
		}

		try {
			SolrPingResponse ping = sos.ping();
			logger.info("Got ping reply in " + ping.getElapsedTime() + "ms. Solr is alive.");
		} catch (Exception e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Ping failed on solr server " + e.getMessage());
			}
		}
		
		return sos;
	}

	private static String finalUrl(String userId, String urlFromConf) {
		String u = userId;
		int idx = u.lastIndexOf("/");
		if (idx > 0) {
			u = u.substring(0, idx);
		}
		String url = urlFromConf;
		if (!url.startsWith("http")) {
			String ip = fr.aliasource.webmail.common.Activator.getDefault().getLocatorRegistry()
					.getHostName(u, null, url);
			url = "http://" + ip + ":8080/solr/webmail";
		}
		return url;
	}


	@Override
	public void init(IIndexingParameters params) {
		configuredUrl = params.getPropertyValue("solr.server.url");
		
		if (configuredUrl != null) {
			logger.info("SOLR server url set to '"+configuredUrl+"'");
		} else {
			logger.warn("SOLR server url not set");
		}
	}

}
