package org.minig.obmsync.service;

import java.util.Map;

public interface ISettingService {

	Map<String, String> getSettings() throws Exception;

	MinigForward getEmailForwarding() throws Exception;

	void setEmailForwarding(MinigForward fs) throws Exception;
	
	MinigVacation getVacationSettings() throws Exception;

	void setVacationSettings(MinigVacation fs) throws Exception;

}
