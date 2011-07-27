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

package fr.aliasource.webmail.invitation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.exception.InvalidICSException;
import org.minig.obmsync.exception.ObmSyncConnectionException;
import org.minig.obmsync.service.EventService;
import org.minig.obmsync.service.IEventService;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.aliasource.utils.DOMUtils;
import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.common.conversation.MailMessage;
import fr.aliasource.webmail.common.conversation.MessageId;
import fr.aliasource.webmail.common.folders.IMAPFolder;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

/**
 * 
 * @author adrienp
 * 
 */
public class GetInvitationInfoAction extends AbstractControlledAction {

	private static final Log logger = LogFactory
			.getLog(GetInvitationInfoAction.class);

	public GetInvitationInfoAction() {

	}

	/*
	 * créer le rdv s'il n'existe pas
	 */
	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		String messageId = req.getParameter("messageId");
		String folder = req.getParameter("folder");

		List<MessageId> listMessageId = new ArrayList<MessageId>();
		listMessageId.add(new MessageId(Long.parseLong(messageId)));

		MailMessage[] mailMessages = p.getAccount().getLoadMessages().load(
				new IMAPFolder(folder), listMessageId);

		try {
			IEventService eventService = new EventService(p.getAccount());
		
			if (mailMessages.length > 0) {
				MailMessage mm = mailMessages[0];
				List<Event> eventsFromICS = eventService.parseIcs(mm.getInvitation());
				
				//ics invitation should have only one appointment
				if(eventsFromICS.size()>0){
					Event eventICS = eventsFromICS.get(0);
					
					Event event = eventService.getEventFromExtId(eventICS
							.getExtId());
					
					if (event == null) {
						event = eventService.createEvent(eventICS);
					}

					List<Event> events = eventService.getListEventsOfDays(event
							.getDate());
					String participationState = eventService
							.getParticipationState(event);
					eventService.logout();
					Document doc = getXmlDocument(event, events, participationState);
					responder.sendDom(doc);
				}
			}

		}  catch (AuthFault e) {
			logger.error(e.getMessage(),e);
			responder.sendError(e.getMessage());
		} catch (InvalidICSException e) {
			logger.error(e.getMessage(),e);
			responder.sendError(e.getMessage());
		} catch (ObmSyncConnectionException e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e.getMessage());
		}
	}

	@Override
	public String getUriMapping() {
		return "/getInvitationInfo.do";
	}

	private String getCalendarUrl() {
		ObmConfIni oci = new ObmConfIni();

		String protocol = oci.get("external-protocol");
		String url = oci.get("external-url");
		String prefix = oci.get("obm-prefix");

		return protocol + "://" + url + prefix + "/calendar/calendar_index.php";
	}

	private Document getXmlDocument(Event event, List<Event> eventsOfDay,
			String participationState) throws Exception {
		Document doc;
		doc = DOMUtils.createDoc("http://minig.org/xsd/invitationInfo",
				"invitationInfo");

		Element root = doc.getDocumentElement();
		Element att = DOMUtils.createElement(root, "calendarUrl");
		att.setTextContent(getCalendarUrl());
		addEventNode(root, event, participationState);
		addListEvent(root, eventsOfDay);
		return doc;
	}

	private void addListEvent(Element root, List<Event> eventsOfDay) {
		Element eventsElem = DOMUtils.createElement(root, "eventsOfDay");
		for (Event event : eventsOfDay) {
			Element eventElem = DOMUtils
					.createElement(eventsElem, "eventOfDay");
			eventElem.setAttribute("title", event.getTitle());
			eventElem.setAttribute("start", Long.toString(clearDate(
					event.getDate()).getTime()));
			eventElem
					.setAttribute("allDay", Boolean.toString(event.isAllday()));

		}
	}

	private void addEventNode(Element root, Event event, String going) {

		Element eventElem = DOMUtils.createElement(root, "event");

		Element att = DOMUtils.createElement(eventElem, "extId");
		att.setTextContent(event.getExtId());

		att = DOMUtils.createElement(eventElem, "title");
		att.setTextContent(event.getTitle());

		att = DOMUtils.createElement(eventElem, "location");
		att.setTextContent(event.getLocation());

		att = DOMUtils.createElement(eventElem, "owner");
		att.setTextContent(event.getOwner());

		att = DOMUtils.createElement(eventElem, "going");
		att.setTextContent(going);

		att = DOMUtils.createElement(eventElem, "attendees");
		List<Attendee> whos = event.getAttendees();
		for (Attendee attendee : whos) {
			Element attWho = DOMUtils.createElement(att, "attendee");
			if (StringUtils.isNotEmpty(attendee.getDisplayName())) {
				attWho.setTextContent(attendee.getDisplayName());
			} else {
				attWho.setTextContent(attendee.getEmail());
			}
		}

		att = DOMUtils.createElement(eventElem, "start");
		att.setTextContent(Long.toString(event.getDate().getTime()));

		long lDate = event.getDate().getTime() + (event.getDuration()*1000);
		att = DOMUtils.createElement(eventElem, "end");
		att.setTextContent(Long.toString(lDate));

	}

	private Date clearDate(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.YEAR, 0);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_YEAR, 0);
		return cal.getTime();
	}

}
