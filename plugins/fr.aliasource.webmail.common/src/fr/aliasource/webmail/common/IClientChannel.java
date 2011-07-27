package fr.aliasource.webmail.common;

public interface IClientChannel {

	void triggerEvent(IAccount ac, ServerEventKind eventKind);
	
}
