package fr.aliasource.webmail.common.conversation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.minig.imap.IMAPHeaders;

public class RawMessageList implements List<RawMessage> {

	private List<RawMessage> l;
	private Map<Long, IMAPHeaders> headers;
	private Collection<Long> uids;

	public RawMessageList(List<RawMessage> l) {
		this.l = l;
	}

	public void add(int index, RawMessage element) {
		l.add(index, element);
	}

	public boolean add(RawMessage e) {
		return l.add(e);
	}

	public boolean addAll(Collection<? extends RawMessage> c) {
		return l.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends RawMessage> c) {
		return l.addAll(index, c);
	}

	public void clear() {
		l.clear();
	}

	public boolean contains(Object o) {
		return l.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return l.containsAll(c);
	}

	public boolean equals(Object o) {
		return l.equals(o);
	}

	public RawMessage get(int index) {
		return l.get(index);
	}

	public int hashCode() {
		return l.hashCode();
	}

	public int indexOf(Object o) {
		return l.indexOf(o);
	}

	public boolean isEmpty() {
		return l.isEmpty();
	}

	public Iterator<RawMessage> iterator() {
		return l.iterator();
	}

	public int lastIndexOf(Object o) {
		return l.lastIndexOf(o);
	}

	public ListIterator<RawMessage> listIterator() {
		return l.listIterator();
	}

	public ListIterator<RawMessage> listIterator(int index) {
		return l.listIterator(index);
	}

	public RawMessage remove(int index) {
		return l.remove(index);
	}

	public boolean remove(Object o) {
		return l.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return l.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return l.retainAll(c);
	}

	public RawMessage set(int index, RawMessage element) {
		return l.set(index, element);
	}

	public int size() {
		return l.size();
	}

	public List<RawMessage> subList(int fromIndex, int toIndex) {
		return l.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return l.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return l.toArray(a);
	}

	public void setHeaders(Map<Long, IMAPHeaders> fetchedHeaders) {
		this.headers = fetchedHeaders;
	}

	public void setUids(Collection<Long> fullUidList) {
		this.uids = fullUidList;
	}

	public Map<Long, IMAPHeaders> getHeaders() {
		return headers;
	}

	public Collection<Long> getUids() {
		return uids;
	}

}
