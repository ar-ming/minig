package org.minig.obmsync.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.exception.ObmSyncConnectionException;
import org.minig.obmsync.provider.impl.ObmSyncProviderFactory;
import org.minig.obmsync.provider.impl.ObmSyncSettingProvider;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LoginUtils;

public class SettingService implements ISettingService {

	private ObmSyncSettingProvider provider;

	private String userId;
	private String userPassword;

	private static final Log logger = LogFactory.getLog(SettingService.class);

	public SettingService(IAccount ac) throws ObmSyncConnectionException {
		this(LoginUtils.lat(ac), ac.getUserPassword());
	}
	
	public SettingService(String userId, String userPassword)
			throws ObmSyncConnectionException {
		this.userId = userId;
		this.userPassword = userPassword;
		provider = ObmSyncProviderFactory.getSettingProvider(userId);
	}

	private void logout() {
		provider.logout();
	}

	private void login() {
		provider.login(userId, userPassword);
	}

	@Override
	public Map<String, String> getSettings() throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		try {
			this.login();
			ret.putAll(provider.getSettings());
			AccessToken token = provider.getToken();
			Method m = token.getClass().getMethod("getVersion");
			String major = null;
			String minor = null;
			String release = null;
			if (m != null) {
				Object version = m.invoke(token);
				major = version.getClass().getMethod("getMajor")
						.invoke(version).toString();
				minor = version.getClass().getMethod("getMinor")
						.invoke(version).toString();
				release = version.getClass().getMethod("getRelease").invoke(
						version).toString();
				logger.info("reflect method: " + major + ", " + minor + ", "
						+ release);
			} else {
				logger.warn("getVersion method not found");
				major = "2";
				minor = "2";
				release = "0";
			}
			ret.put("os-major", major);
			ret.put("os-minor", minor);
			ret.put("os-release", release);
		} catch (Throwable t) {
			logger.error("error getting obm-sync version", t);
		} finally {
			this.logout();
		}

		return ret;
	}

	@Override
	public MinigForward getEmailForwarding() throws Exception {
		MinigForward ret = new MinigForward();
		try {
			this.login();
			ForwardingSettings fs = provider.getEmailForwarding();
			ret.setEnabled(fs.isEnabled());
			ret.setEmail(fs.getEmail());
			ret.setLocalCopy(fs.isLocalCopy());
		} finally {
			this.logout();
		}

		return ret;
	}

	@Override
	public MinigVacation getVacationSettings() throws Exception {
		MinigVacation ret = new MinigVacation();
		try {
			this.login();
			VacationSettings vs = provider.getVacationSettings();
			ret.setEnabled(vs.isEnabled());
			ret.setStart(vs.getStart());
			ret.setEnd(vs.getEnd());
			ret.setText(vs.getText());
		} finally {
			this.logout();
		}

		return ret;
	}

	@Override
	public void setEmailForwarding(MinigForward fs) throws Exception {
		try {
			this.login();
			ForwardingSettings ofs = new ForwardingSettings();
			ofs.setEnabled(fs.isEnabled());
			ofs.setLocalCopy(fs.isLocalCopy());
			ofs.setEmail(fs.getEmail());
			provider.setEmailForwarding(ofs);
		} finally {
			this.logout();
		}
	}

	@Override
	public void setVacationSettings(MinigVacation fs) throws Exception {
		try {
			this.login();
			VacationSettings ovs = new VacationSettings();
			ovs.setEnabled(fs.isEnabled());
			ovs.setStart(fs.getStart());
			ovs.setEnd(fs.getEnd());
			ovs.setText(fs.getText());
			provider.setVacationSettings(ovs);
		} finally {
			this.logout();
		}
	}

}
