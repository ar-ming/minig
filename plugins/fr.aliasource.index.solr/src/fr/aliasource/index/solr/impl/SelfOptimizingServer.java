package fr.aliasource.index.solr.impl;

import java.net.MalformedURLException;
import java.util.Timer;

import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;

import fr.aliasource.index.solr.SolrClient;

public class SelfOptimizingServer extends StreamingUpdateSolrServer {

	private static final long serialVersionUID = -7100369356092799025L;
	private static IndexOptimizer iopt;

	public SelfOptimizingServer(String url) throws MalformedURLException {
		super(url, 100, 8);
		setRequestWriter(new BinaryRequestWriter());

		if (iopt == null) {
			SolrClient sc = new SolrClient(this, null);
			iopt = new IndexOptimizer(sc);
			Timer t = new Timer();
			t.schedule(iopt, 0, 3600 * 1000);
		}

	}

}
