package org.minig.obmsync.provider.impl;

import java.util.Map;

import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.setting.SettingClient;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

public class ObmSyncSettingProvider extends AbstractProvider {

	private SettingClient setting;

	public ObmSyncSettingProvider(SettingClient setting) {
		this.setting = setting;
	}

	public void login(String userId, String userPassword) {
		this.token = setting.login(userId, userPassword, "minig");
	}

	public void logout() {
		if (this.token != null) {
			setting.logout(token);
		}
	}

	public Map<String, String> getSettings() throws AuthFault, ServerFault {
		return setting.getSettings(token);
	}

	public ForwardingSettings getEmailForwarding() throws AuthFault,
			ServerFault {
		return setting.getEmailForwarding(token);
	}

	public VacationSettings getVacationSettings() throws AuthFault, ServerFault {
		return setting.getVacationSettings(token);
	}

	public void setEmailForwarding(ForwardingSettings fs) throws AuthFault,
			ServerFault {
		setting.setEmailForwarding(token, fs);
	}

	public void setVacationSettings(VacationSettings vs) throws AuthFault,
			ServerFault {
		setting.setVacationSettings(token, vs);
	}
}
