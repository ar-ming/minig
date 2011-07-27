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

package fr.aliasource.webmail.proxy.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.imap.SearchQuery;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.conversation.ConversationReference;
import fr.aliasource.webmail.common.folders.IFolder;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.common.imap.IStoreConnection;
import fr.aliasource.webmail.common.imap.StoreException;
import fr.aliasource.webmail.proxy.api.IFolderService;

public class FolderServiceImpl implements IFolderService {

	private IAccount account;
	private static Log logger = LogFactory.getLog(FolderServiceImpl.class);

	private abstract class FolderServiceCallBack{
		public abstract void execute(IStoreConnection connection, IFolder f) throws Exception;
	}
	
	
	private class FolderServiceTemplate{
		private void update(IFolder folder,FolderServiceCallBack callback){
			IStoreConnection connection = null;
			try {
				connection = account.getStoreProtocol();
				callback.execute(connection, folder);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if (connection != null) {
					connection.destroy();
				}
			}
		}
		
	}
	
	private FolderServiceTemplate template = new FolderServiceTemplate();
	
	
	protected IAccount getAccount() {
		return account;
	}

	public FolderServiceImpl(IAccount ac) {
		this.account = ac;
	}

	

	@Override
	public SortedMap<IFolder, Integer> getSummary() {
		try {
			return getAccount().getCache().getSummaryCache().getData();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new TreeMap<IFolder, Integer>();
		}
	}

	@Override
	public List<IFolder> listAvailableFolders() {
		try {
			refreshCache();
			return getAccount().getCache().getAvailableFolderCache().getData();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new LinkedList<IFolder>();
		}
	}

	public List<IFolder> listSubscribedFolders() {
		try {
			return getAccount().getCache().getSubscribedFolderCache().getData();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new LinkedList<IFolder>();
		}
	}
	
	
	/**
	 * Refresh folder cache
	 */
	private void refreshCache() {
		try {
			getAccount().getCache().getSubscribedFolderCache().update();
			getAccount().getCache().getAvailableFolderCache().update();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Refresh conversation cache
	 */
	private void refreshCache(IFolder f) {
		try {
			getAccount().getCache().getCacheManager().refresh(f);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void deleteFromConversationCache(IFolder f) throws InterruptedException {

		template.update(f, new FolderServiceCallBack(){
			@Override
			public void execute(IStoreConnection connection, IFolder f) throws Exception {
			      connection.select(f.getName());
			      Collection<Long> allUids = connection.uidSearch(new SearchQuery());
			      account.getCache().getConversationCache().fastUpdate(f, new ArrayList<Long>(0), allUids);
			}
		});
		
	
	}


	@Override
	public void createFolder(IFolder f) {
		
		template.update(f, new FolderServiceCallBack(){
			@Override
			public void execute(IStoreConnection connection, IFolder f) throws IOException, StoreException {
				connection.create(f.getName());
			}
			
		});
		
		subscribeFolder(f);
	}
	
	
	@Override
	public void subscribeFolder(IFolder folder) {
		
		List<IFolder> toSubscribed = new ArrayList<IFolder>();
		toSubscribed.addAll(getParents(folder));
		for(IFolder f : toSubscribed){
			template.update(f, new FolderServiceCallBack(){
				@Override
				public void execute(IStoreConnection connection, IFolder f) throws IOException, StoreException {
					connection.subscribe(f.getName());
				}
			});
			refreshCache();
			refreshCache(account.getCache().getSubscribedFolderCache().get(f));
		}
	}

	

	@Override
	public void unSubscribeFolder(IFolder folder) {
		
		List<IFolder> toUnsubscribed = new ArrayList<IFolder>();
		toUnsubscribed.addAll(getChilds(folder));
	
		for(IFolder f : toUnsubscribed){
			template.update(f, new FolderServiceCallBack(){
				@Override
				public void execute(IStoreConnection connection, IFolder f) throws IOException, StoreException {
					connection.unsubscribe(f.getName());
				}
			});
			try {
				deleteFromConversationCache(f);
		    } catch (InterruptedException e) {
		      logger.error(e.getMessage(), e);
		    }
		}
	}

	private Collection<? extends IFolder> getChilds(IFolder folder) {
		List<IFolder> ret  = listSubscribedFolders();
		for (Iterator<IFolder> iterator = ret.iterator(); iterator.hasNext();) {
			IFolder iFolder = (IFolder) iterator.next();
			if(!(iFolder.getName().startsWith(folder.getName())) ){
				iterator.remove();
			}
			
		}
		return ret;
	}
	
	private Collection<? extends IFolder> getParents(IFolder folder) {
		List<IFolder> ret  = listAvailableFolders();
		for (Iterator<IFolder> iterator = ret.iterator(); iterator.hasNext();) {
			IFolder iFolder = (IFolder) iterator.next();
		
			if( iFolder.isSubscribed()
					|| !folder.getName().contains(iFolder.getName())){
				iterator.remove();
				continue;
			}
			boolean skip=false;
			String[] splitted = folder.getName().split("/");
			String name = "";
			for (int i = 0; i < splitted.length; i++) {
				name+=splitted[i];
				if(iFolder.getName().equals(name)){
					
					skip = true;
					break;
				}
				name+="/";
			}
			
			if(!skip){
				iterator.remove();
			}
		}
		
		return ret;
	}
	

	@Override
	public void deleteFolder(IFolder f) {
		template.update(f, new FolderServiceCallBack(){
			@Override
			public void execute(IStoreConnection connection, IFolder f) throws IOException, StoreException {
				connection.delete(f.getName());
			}
			
		});
		refreshCache();
	}

	@Override
	public void moveFolder(IFolder src, IFolder dest) {
		String d = dest.getName() + "/" + src.getDisplayName().trim();
		IFolder fDest = new IMAPFolder(d);
		createFolder(fDest);
		IAccount ac = getAccount();
		List<ConversationReference> all = new LinkedList<ConversationReference>();
		try {
			all = ac.getListConversations().list(src, 1, Integer.MAX_VALUE)
					.getPage();
			Set<String> convIds = new HashSet<String>();
			for (ConversationReference ref : all) {
				convIds.add(ref.getId());
			}
			getAccount().getMoveConversation().move(fDest, convIds);
			deleteFolder(src);
		} catch (Exception se) {
			logger.error(se.getMessage(), se);
		}
	}

	@Override
	public void renameFolder(IFolder folder,  String newName) {
		
		//We remove old folder from cache and index

			List<IFolder> toUnsubscribed = new ArrayList<IFolder>();
			toUnsubscribed.addAll(getChilds(folder));

		
			for(IFolder f : toUnsubscribed){
				try {
					deleteFromConversationCache(f);
				} catch (InterruptedException e) {
					  logger.error(e.getMessage(), e);
				}	
			}
		
		
			String path = folder.getName().substring(0,
					Math.min(folder.getName().lastIndexOf("/")+1,folder.getName().length()));
			final IMAPFolder newfolder = new IMAPFolder(path+newName);
			
			template.update(folder, new FolderServiceCallBack(){
				@Override
				public void execute(IStoreConnection connection, IFolder folder) throws IOException, StoreException {		
						connection.unsubscribe(folder.getName());
						connection.renameFolder(folder.getName(),newfolder.getName());
						connection.subscribe(newfolder.getName());
				}
				
			});
		

			//Refresching list
			refreshCache();
			
			//Add new subfolder to index
			toUnsubscribed = new ArrayList<IFolder>();
			toUnsubscribed.addAll(getChilds(newfolder));
		
			for(IFolder f : toUnsubscribed){
				refreshCache(f);
			}

		
		
	}
}
