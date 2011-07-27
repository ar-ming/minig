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

package fr.aliasource.index.core.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.aliasource.index.core.IIndexFactory;
import fr.aliasource.index.core.IIndexingParameters;
import fr.aliasource.index.core.Index;
import fr.aliasource.index.core.SearchActivator;
import fr.aliasource.utils.RunnableExtensionLoader;

/**
 * Maintains the type to Index mapping, and creates new Indexes when needed.
 * 
 * @author tom
 * 
 */
public class IndexManager {

	private Map<String, Index> typeIndexes;
	private Log logger;
	private IIndexFactory factory;

	public IndexManager(IIndexingParameters params) {
		logger = LogFactory.getLog(getClass());
		typeIndexes = new HashMap<String, Index>();
		RunnableExtensionLoader<IIndexFactory> loader = new RunnableExtensionLoader<IIndexFactory>();
		List<IIndexFactory> factories = loader.loadExtensions(
				SearchActivator.PLUGIN_ID, "indexfactory", "index_factory",
				"implementation");
		if (factories.size() < 1) {
			logger.fatal("No IIndexFactory declared, abort");
			throw new RuntimeException("No IIndexFactory declared, abort");
		}
		factory = factories.get(0);
		factory.init(params);
	}

	public Index getIndex(String type) {
		return typeIndexes.get(type);
	}

	/**
	 * The crawler lock is held while doing this, so this needs to be very fast
	 * 
	 * @param type
	 * @param data
	 */
	public void index(String type, Map<String, String> data) {
		typeIndexes.get(type).queueWrite(data);
	}

	public void setupIndex(String type) {
		Index idx = factory.getIndex(type);
		typeIndexes.put(type, idx);
	}

	public void delete(String type, String id) {
		typeIndexes.get(type).queueDeletion(id);
	}

}
