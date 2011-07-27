package org.minig.threading.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.minig.threading.IThreadable;
import org.minig.threading.Threadable;
import org.minig.threading.Threader;

import com.google.common.base.Joiner;

public class ThreaderTests {

	private long uid;
	
	@Before
	public void setUp() {
		uid = 0;
	}
	
	private IThreadable createStubThreadable(String mid, String... refs) {
		String irt = refs.length > 0 ? refs[refs.length - 1] : null;
		return new Threadable(mid, uid++, irt, Joiner.on(' ').join(refs));
	}

	private String threadsAsString(IThreadable t) {
		StringBuilder sb = new StringBuilder();
		firstLevelThread(t, sb);
		return sb.toString();
	}
	
	private void firstLevelThread(IThreadable t, StringBuilder sb) {
		IThreadable sibling = t;
		while (sibling != null) {
			sb.append('(');
			writeElementAndChildren(sb, sibling);
			sb.append(')');
			sibling = sibling.getNext();
		}
	}

	private void writeElementAndChildren(StringBuilder sb, IThreadable sibling) {
		sb.append(sibling.getMessageID());
		IThreadable child = sibling.getChild();
		if (child != null) {
			sb.append(' ');
			threadsAsString(child, sb);
		}
	}
	
	private void threadsAsString(IThreadable t, StringBuilder sb) {
		if (t.getNext() != null) {
			firstLevelThread(t, sb);
		} else {
			writeElementAndChildren(sb, t);
		}
	}
	
	private String buildThreading(List<IThreadable> ts) {
		Threader threader = new Threader();
		IThreadable result = threader.thread(ts);
		String resultAsString = threadsAsString(result);
		return resultAsString;
	}

	@Test
	public void testThreaderSimpleThread() {
		List<IThreadable> ts = Arrays.asList(
				createStubThreadable("ref3", "ref1", "ref2"),
				createStubThreadable("ref2", "ref1"),
				createStubThreadable("ref1"));

		Assert.assertEquals("(ref1 ref2 ref3)", buildThreading(ts));
	}
	
	@Test
	public void testThreaderTwoRoots() {
		List<IThreadable> ts = Arrays.asList(
				createStubThreadable("ref3", "ref1", "ref2"),
				createStubThreadable("ref2", "ref1"),
				createStubThreadable("ref1"),
				createStubThreadable("ref0"));

		Assert.assertEquals("(ref0)(ref1 ref2 ref3)",  buildThreading(ts));
	}

	@Test
	public void testThreaderRFC() {
		List<IThreadable> ts = Arrays.asList(
				createStubThreadable("96", "3", "6", "44", "7"),
				createStubThreadable("7", "3", "6", "44"),
				createStubThreadable("44", "3", "6"),
				createStubThreadable("6", "3"),
				createStubThreadable("3"),
				createStubThreadable("23", "3", "6", "4"),
				createStubThreadable("4", "3", "6"),
				createStubThreadable("2"));

		Assert.assertEquals("(3 6 (44 7 96)(4 23))(2)", buildThreading(ts));
	}

	@Test
	public void testThreaderRFCRandomized() {
		List<IThreadable> ts = Arrays.asList(
				createStubThreadable("6", "3"),
				createStubThreadable("3"),
				createStubThreadable("7", "3", "6", "44"),
				createStubThreadable("96", "3", "6", "44", "7"),
				createStubThreadable("44", "3", "6"),
				createStubThreadable("4", "3", "6"),
				createStubThreadable("23", "3", "6", "4"),
				createStubThreadable("2"));

		Assert.assertEquals("(3 6 (44 7 96)(4 23))(2)", buildThreading(ts));
	}
	
	@Test
	public void testThreaderBugzilla1494() {
		List<IThreadable> ts = Arrays.asList(
				createStubThreadable("ref584", "ref377"),
				createStubThreadable("ref464", "ref377", "ref584", "ref482"),
				createStubThreadable("ref147", "ref377", "ref584", "ref482", "ref464"));

		Assert.assertEquals("(ref584 ref464 ref147)", buildThreading(ts));
	}
	
}
