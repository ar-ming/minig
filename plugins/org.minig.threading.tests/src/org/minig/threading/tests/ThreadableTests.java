package org.minig.threading.tests;

import org.junit.Assert;
import org.junit.Test;
import org.minig.threading.Threadable;

public class ThreadableTests {

	@Test
	public void testKeepOrder() {
		Threadable threadable = new Threadable("mid", 1, "irt", "m1, m2, m3, irt");
		Assert.assertArrayEquals(new String[] {"m1", "m2", "m3", "irt"}, threadable.getReferences());
	}
	
	@Test
	public void testKeepOrderMissingIrtAtTail() {
		Threadable threadable = new Threadable("mid", 1, "irt", "m1, m2, m3");
		Assert.assertArrayEquals(new String[] {"m1", "m2", "m3", "irt"}, threadable.getReferences());
	}

	@Test
	public void testVariousSeparator() {
		Threadable threadable = new Threadable("mid", 1, "irt", "m1, m2 m3");
		Assert.assertArrayEquals(new String[] {"m1", "m2", "m3", "irt"}, threadable.getReferences());
	}

}
