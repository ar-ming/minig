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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.minig.obmsync.service.EventService;
import org.minig.obmsync.service.IEventService;
import org.obm.sync.calendar.Event;

import fr.aliasource.webmail.common.IParameterSource;
import fr.aliasource.webmail.proxy.AbstractControlledAction;
import fr.aliasource.webmail.proxy.api.IProxy;
import fr.aliasource.webmail.proxy.api.IResponder;

public class GoingEventAction extends AbstractControlledAction {

	private static final Log logger = LogFactory
			.getLog(GetInvitationInfoAction.class);

	public GoingEventAction() {

	}

	@Override
	public void execute(IProxy p, IParameterSource req, IResponder responder) {
		try {
			IEventService eventService = new EventService(p.getAccount());

			String extId = req.getParameter("extId");
			String going = req.getParameter("going");

			Event event; 
		
			event = eventService.getEventFromExtId(extId);
			eventService.updateParticipationState(event, going);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			responder.sendError(e.getMessage());
		}

	}

	@Override
	public String getUriMapping() {
		return "/goingEvent.do";
	}

}
