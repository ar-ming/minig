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

package org.minig.obmsync.service;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.minig.obmsync.exception.InvalidICSException;
import org.minig.obmsync.exception.ObmSyncConnectionException;
import org.minig.obmsync.provider.impl.ObmSyncCalendarProvider;
import org.minig.obmsync.provider.impl.ObmSyncProviderFactory;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;

import fr.aliasource.webmail.common.IAccount;
import fr.aliasource.webmail.common.LoginUtils;

public class EventService implements IEventService {


	private ObmSyncCalendarProvider icp;
	private IAccount ac;

	public EventService(IAccount ac) throws ObmSyncConnectionException {
		this.ac = ac;
		icp = ObmSyncProviderFactory.getCalendarProvider(LoginUtils.lat(ac), ac.getUserPassword());
		
	}

	@Override
	public Event getEventFromExtId(String externalUID) throws AuthFault, ServerFault {
		return icp.getEventFromExtId(ac.getUserId(), externalUID); 
	}

	@Override
	public Event createEvent(Event event) throws AuthFault, ServerFault {
		return icp.createEvent(ac.getUserId(),event);
	}

	@Override
	public List<Event> getListEventsOfDays(Date day) throws AuthFault, ServerFault {
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(day);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)+1);
		Date end = cal.getTime();
		
		return icp.getListEventsFromIntervalDate(ac.getUserId(),start, end);
	}

	@Override
	public void updateParticipationState(Event event, String going) throws AuthFault, ServerFault {
		icp.updateParticipationState(ac.getUserId(), event, going);
	}

	@Override
	public String getParticipationState(Event event) throws AuthFault, ServerFault {		
		return icp.getParticipationState(ac.getUserId(),event);
	}

	@Override
	public List<Event> parseIcs(InputStream icsFile) throws AuthFault, InvalidICSException {
		return icp.parseICS(icsFile);
	}

	@Override
	public String getUserEmail() throws Exception {
		return icp.getUserEmail();
	}

	@Override
	public void logout() {
		icp.logout();
	}

}
