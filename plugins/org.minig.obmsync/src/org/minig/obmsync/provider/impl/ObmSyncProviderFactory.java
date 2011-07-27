/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.obmsync.provider.impl;

import org.minig.obmsync.ObmSyncConfIni;
import org.minig.obmsync.exception.ObmSyncConnectionException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.setting.SettingClient;
import org.obm.sync.locators.CalendarLocator;
import org.obm.sync.locators.SettingLocator;

import fr.aliasource.webmail.common.Activator;

public class ObmSyncProviderFactory {

	private static String finalUrl(String userId, String urlFromConf) {
		String url = urlFromConf;
		if (!url.startsWith("http")) {
			String ip = Activator.getDefault().getLocatorRegistry()
					.getHostName(userId, null, url);
			url = "http://" + ip + ":8080/obm-sync/services";
		}
		return url;
	}

	public static ObmSyncCalendarProvider getCalendarProvider(String userId,
			String userPassword) throws ObmSyncConnectionException {
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = finalUrl(userId, obmCalendarParameters
				.getPropertyValue("obmsync.server.url"));
		try {
			CalendarClient cal = new CalendarLocator().locate(url);
			AccessToken token = cal.login(userId, userPassword, "minig");
			return new ObmSyncCalendarProvider(cal, token);
		} catch (Throwable e) {
			throw new ObmSyncConnectionException(
					"Connection to obm-sync server is impossible");
		}
	}

	public static ObmSyncSettingProvider getSettingProvider(String userId)
			throws ObmSyncConnectionException {
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = finalUrl(userId, obmCalendarParameters
				.getPropertyValue("obmsync.server.url"));
		try {
			SettingClient setting = SettingLocator.locate(url);
			return new ObmSyncSettingProvider(setting);
		} catch (Throwable e) {
			throw new ObmSyncConnectionException(
					"Connection to obm-sync server is impossible");
		}

	}

	public static ObmSyncBookProvider getBookProvider(String userId)
			throws ObmSyncConnectionException {
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = finalUrl(userId, obmCalendarParameters
				.getPropertyValue("obmsync.server.url"));
		try {
			BookClient book = new BookClient(url);
			return new ObmSyncBookProvider(book);
		} catch (Throwable e) {
			throw new ObmSyncConnectionException(
					"Connection to obm-sync server is impossible");
		}

	}

}
