/*
 * Copyright (c) 2005 Nokia
 * All rights reserved.
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is the MobileNews NNTP client.
 * 
 * The Initial Developer of the Original Code is Nokia. Written by Jimmy Kjllman. 
 * Portions (the "Threader" class and "IThreadable" interface in package newsclient.thread)
 * use code from the Grendel mail/news client which is licensed under Mozilla Public License Version 1.1. 
 * See http://lxr.mozilla.org/mozilla/source/grendel/sources/grendel/view/Threader.java
 * 
 * Contributor(s): ______________________________________.
 * 
 */

package org.minig.threading;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public final class DummyEnumerator<T> implements Enumeration<T> {

	public static final Enumeration<Object> enumeration = new DummyEnumerator<Object>();

	public boolean hasMoreElements() {
		return false;
	}

	public T nextElement() {
		throw new NoSuchElementException();
	}

}
