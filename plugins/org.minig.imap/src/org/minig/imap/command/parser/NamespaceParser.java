package org.minig.imap.command.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minig.imap.NameSpaceInfo;
import org.minig.imap.impl.MailboxNameUTF7Converter;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.Var;

@BuildParseTree
public class NamespaceParser extends BaseParser<Object> {
	
	public static final String expectedResponseStart = "* NAMESPACE";
	
	public Rule rule() {
		return Sequence(namespaceCommand(), 
				namespace(), ACTION(setPersonalNamespaces()), 
				whitespaces(),
				namespace(), ACTION(setOtherUserNamespaces()),
				whitespaces(),
				namespace(), ACTION(setSharedFolderNamespaces()));
	}
	
	@SuppressWarnings("unchecked")
	boolean setPersonalNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setPersonal((List<String>) pop());
		push(nsi);
		return true;
	}

	@SuppressWarnings("unchecked")
	boolean setOtherUserNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setOtherUsers((List<String>) pop());
		push(nsi);
		return true;
	}

	@SuppressWarnings("unchecked")
	boolean setSharedFolderNamespaces() {
		swap();
		NameSpaceInfo nsi = (NameSpaceInfo) pop();
		nsi.setMailShares((List<String>) pop());
		push(nsi);
		return true;
	}

	
	Rule namespace() {
		return FirstOf(
				Sequence(nil(), push(Collections.emptyList())),
				group());
	}
	
	Rule group() {
		Var<List<String>> values = new Var<List<String>>(new ArrayList<String>());
		return Sequence('(', 
				Sequence(
						OneOrMore(entry(), values.get().add(
								MailboxNameUTF7Converter.decode((java.lang.String) pop()))),
						push(values.get())),
				')');
	}

	Rule entry() {
		return Sequence('(', 
				stackedExpression(),
				whitespaces(), 
				expression(),
				ZeroOrMore(extension()),
				')',
				whitespaces());
	}
	
	Rule extension() {
		return Sequence(whitespaces(), 
				expression(),
				whitespaces(),
				'(', expression(),
				ZeroOrMore(whitespaces(), expression()),
				')'
			);
	}

	Rule stackedExpression() {
		return Sequence('"', Sequence(printableCharacter(), push(match())),'"', whitespaces());
	}

	
	Rule expression() {
		return Sequence('"', printableCharacter(),'"', whitespaces());
	}
	
	Rule printableCharacter() {
		return ZeroOrMore(FirstOf(CharRange((char)0x20, (char)0x21), CharRange((char)0x23, (char)0x7e)));
	}
	
	Rule nil() {
		return String("NIL");
	}
	
	Rule whitespaces() {
		return ZeroOrMore(' ');
	}
	
	Rule namespaceCommand() {
		return Sequence(String(expectedResponseStart), whitespaces(), push(new NameSpaceInfo())); 
	}
}