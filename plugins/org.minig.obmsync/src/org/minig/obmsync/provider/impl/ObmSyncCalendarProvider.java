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

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.exception.InvalidICSException;
import org.minig.obmsync.service.IEventService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.CalendarClient;

import fr.aliasource.utils.FileUtils;

public class ObmSyncCalendarProvider {

	private static final Log logger = LogFactory.getLog(ObmSyncCalendarProvider.class);
	private CalendarClient cal;
	private AccessToken token;

	public ObmSyncCalendarProvider(CalendarClient cal, AccessToken token) {
		this.cal = cal;
		this.token = token;
	}

	public CalendarInfo getMyCalendar(String userId) throws ServerFault,
			AuthFault {
		CalendarInfo[] listCalInfo = cal.listCalendars(token);
		for (CalendarInfo calInfo : listCalInfo) {
			if (calInfo.getUid().equals(userId.split("@")[0])) {
				return calInfo;
			}
		}
		return null;
	}

	public Event getEventFromExtId(String userId, String uid) throws AuthFault,
			ServerFault {
		return cal
				.getEventFromExtId(token, getMyCalendar(userId).getUid(), uid);
	}

	public Event createEvent(String userId, Event event) throws AuthFault,
			ServerFault {
		CalendarInfo myCalendar = getMyCalendar(userId);
		boolean find = false;
		for (Attendee att : event.getAttendees()) {
			if (myCalendar.getMail().equals(att.getEmail())) {
				find = true;
			}
		}
		if (!find) {
			Attendee att = new Attendee();
			att.setEmail(myCalendar.getMail());
			att.setState(ParticipationState.NEEDSACTION);
			att.setRequired(ParticipationRole.OPT);
			event.addAttendee(att);
		}
		logger.info("event to create: " + event + " event.title "
				+ event.getTitle());
		cal.createEvent(token, getMyCalendar(userId).getUid(), event);
		return event;
	}

	public void logout() {
		cal.logout(token);
	}

	public List<Event> getListEventsFromIntervalDate(String userId, Date start,
			Date end) throws AuthFault, ServerFault {
		return cal.getListEventsFromIntervalDate(token, getMyCalendar(userId)
				.getUid(), start, end);
	}

	public void updateParticipationState(String userId, Event event,
			String participationState) throws AuthFault, ServerFault {
		for (Attendee att : event.getAttendees()) {
			if (getMyCalendar(userId).getMail().equals(att.getEmail())) {
				if (IEventService.PARTICIPATION_STATE_ACCEPTED
						.equals(participationState)) {
					att.setState(ParticipationState.ACCEPTED);
				} else if (IEventService.PARTICIPATION_STATE_DECLINED
						.equals(participationState)) {
					att.setState(ParticipationState.DECLINED);
				} else if (IEventService.PARTICIPATION_STATE_NEEDSACTION
						.equals(participationState)) {
					att.setState(ParticipationState.NEEDSACTION);
				}
			}
		}
		cal.modifyEvent(token, getMyCalendar(userId).getUid(), event, true);
	}

	public String getParticipationState(String userId, Event event)
			throws AuthFault, ServerFault {
		for (Attendee att : event.getAttendees()) {
			if (getMyCalendar(userId).getMail().equals(att.getEmail())) {
				if (ParticipationState.ACCEPTED.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_ACCEPTED;
				} else if (ParticipationState.DECLINED.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_DECLINED;
				} else if (ParticipationState.DELEGATED.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_ACCEPTED;
				} else if (ParticipationState.TENTATIVE.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_ACCEPTED;
				} else {
					return IEventService.PARTICIPATION_STATE_NEEDSACTION;
				}
			}
		}
		return "";
	}

	public String getUserEmail() throws AuthFault, ServerFault {
		return cal.getUserEmail(token);
	}

	public List<Event> parseICS(InputStream icsFile) throws AuthFault, InvalidICSException{
		String ics = "";
		try {
			ics = FileUtils.streamString(icsFile, true);
			return cal.parseICS(token, ics);
		} catch (Exception e) {
			logger.error("ICS: "+ics);
			throw new InvalidICSException(e);
		}
	}
}
