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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IAccount;

/**
 * File based caching command implementation
 * 
 * @author tom
 * 
 * @param <W>
 *            the type of the cached data
 */
public abstract class FileCache<W> extends AbstractCache<W> {

	private String cDirectory;
	private File cFile;
	protected Semaphore updateLock;
	protected Log logger;
	private String category;
	private String cacheName;
	protected IDirectCommand<W> command;

	protected FileCache(IAccount account, String category, String cacheName,
			IDirectCommand<W> command) {
		super(account);
		this.command = command;
		updateLock = new Semaphore(1);
		this.category = category;
		this.cacheName = cacheName;
		this.cDirectory = account.getCache().getCachePath() + File.separator
				+ category;
		new File(cDirectory).mkdirs();
		this.cFile = new File(cDirectory + File.separator + cacheName + ".xml");
		this.logger = LogFactory.getLog(getClass());

		if (!exists()) {
			initEmpty();
		}
	}

	private void initEmpty() {
		Document dom;
		try {
			dom = DOMUtils.createDoc(getCacheNamespace(), category);
			DOMUtils.serialise(dom, new FileOutputStream(getCacheFile()));
		} catch (Exception e) {
			logger.error("error on initEmpty for " + category + "/" + cacheName
					+ " " + e.getMessage(), e);
		}
	}

	protected String getCacheNamespace() {
		return "http://obm.aliasource.fr/xsd/" + category;
	}

	protected String getCacheDirectory() {
		return cDirectory;
	}

	protected File getCacheFile() {
		return cFile;
	}

	protected boolean exists() {
		return cFile.exists();
	}

	public W update() throws InterruptedException {
		W data = null;
		try {
			data = command.getData();
		} catch (Exception e) {
			// this is crappy exception handling, but your logs will
			// be filled of exceptions
			logger.error(category + "/" + cacheName + " getData() failed "
					+ e.getMessage(), e);
			return null;
		}

		updateLock.acquire();
		try {
			writeToCache(data);
			if (logger.isDebugEnabled()) {
				logger.debug(category + "/" + cacheName + " updated.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			updateLock.release();
		}
		return data;
	}

	public W getCachedData() {
		if (exists()) {
			return loadFromCache();
		} else {
			return null;
		}
	}

	public W getData() {
		if (!exists()) {
			try {
				update();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}
		return loadFromCache();
	}

	public void customParse(DefaultHandler handler) {
		try {
			updateLock.acquire();
			DOMUtils.saxParse(new FileInputStream(getCacheFile()), handler);
		} catch (Exception e) {
			logger.error("Error parsing "+getCacheFile().getAbsolutePath(), e);
		} finally {
			updateLock.release();
		}
	}

	public Document getDOM() {
		Document doc = null;
		try {
			updateLock.acquire();
			doc = DOMUtils.parse(getCacheFile());
		} catch (Exception e) {
			logger.error(e, e);
		} finally {
			updateLock.release();
		}
		return doc;
	}

	protected boolean useDOMForLoad() {
		return true;
	}

	protected W loadFromCache() {
		if (useDOMForLoad()) {
			return loadCacheFromDOM(getDOM());
		} else {
			return loadCacheFromDOM(null);
		}
	}

	protected abstract W loadCacheFromDOM(Document doc);

}
