package fr.aliasource.webmail.common.folders;

import java.util.List;

import org.minig.imap.ListInfo;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import fr.aliasource.webmail.common.IAccount;

public class AbstractListFoldersCommand {

	protected IAccount account;

	public AbstractListFoldersCommand(IAccount account) {
		super();
		this.account = account;
	}

	protected String extractDisplayName(char delimiter, ListInfo info) {
		Iterable<String> parts = Splitter.on(delimiter).split(info.getName());
		int depth = Iterables.size(parts) - 1;
		String last = Iterables.getLast(parts);
		StringBuilder ret = new StringBuilder(last.length() + depth);
		// prepend spaces
		for (int i = 0; i < depth; i++) {
			ret.append(' ');
		}
		ret.append(last);
		return ret.toString();
	}

	protected boolean isShared(List<String> sharedNamespaces, ListInfo info) {
		for (String namespace: sharedNamespaces) {
			if (info.getName().startsWith(namespace)) {
				return true;
			}
		}
		return false;
	}

}