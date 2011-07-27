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

package fr.aliasource.webmail.book;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;

public class MinigContact implements Comparable<MinigContact> {

	private static final Log logger = LogFactory.getLog(MinigContact.class);
	protected Contact obm;

	protected MinigContact() {

	}

	public MinigContact(String lastName, String firstName, String email, String function,
			String homeVoice, String cellVoice, String street, String zip,
			String location) {
		obm = new Contact();
		obm.setLastname(lastName);
		obm.setFirstname(firstName);
		obm.addEmail("INTERNET;X-OBM-Ref1", new Email(email));
		obm.setTitle(function);
		// TODO add missing fields
	}

	public void addAddress(String lbl, MinigAddress p) {
		obm
				.addAddress(lbl, new Address(p.getStreet(), p.getZipCode(), p
						.getExpressPostal(), p.getTown(), p.getCountry(), p
						.getState()));
	}

	public <T> T adapt(Class<T> target) {
		if (target.equals(Contact.class)) {
			return target.cast(obm);
		}
		logger.warn("Cannot cast MinigContact to type " + target);
		return null;
	}

	public void addEmail(String lbl, MinigEmail email) {
		obm.addEmail(lbl, new Email(email.getEmail()));
	}

	public void addIMIdentifier(String lbl, MinigIM imid) {
		obm.addIMIdentifier(lbl, new InstantMessagingId(imid.getProtocol(),
				imid.getId()));
	}

	public void addPhone(String lbl, MinigPhone p) {
		obm.addPhone(lbl, new Phone(p.getNumber()));
	}

	public void addWebsite(String lbl, MinigWebsite p) {
		obm.addWebsite(lbl, new Website(p.getUrl()));
	}

	public boolean equals(Object obj) {
		return obm.equals(obj);
	}

	public String getAka() {
		return obm.getAka();
	}

	public Date getAnniversary() {
		return obm.getAnniversary();
	}

	public Integer getAnniversaryId() {
		return obm.getAnniversaryId();
	}

	public String getAssistant() {
		return obm.getAssistant();
	}

	public Date getBirthday() {
		return obm.getBirthday();
	}

	public Integer getBirthdayId() {
		return obm.getBirthdayId();
	}

	public String getComment() {
		return obm.getComment();
	}

	public String getCompany() {
		return obm.getCompany();
	}

	public Map<String, MinigEmail> getEmails() {
		Map<String, MinigEmail> ret = new HashMap<String, MinigEmail>();
		for (String lbl : obm.getEmails().keySet()) {
			Email e = obm.getEmails().get(lbl);
			ret.put(lbl, new MinigEmail(e.getEmail()));
		}
		return ret;
	}

	public String getFirstname() {
		return obm.getFirstname();
	}

	public String getLastname() {
		return obm.getLastname();
	}

	public String getManager() {
		return obm.getManager();
	}

	public String getMiddlename() {
		return obm.getMiddlename();
	}

	public String getService() {
		return obm.getService();
	}

	public String getSpouse() {
		return obm.getSpouse();
	}

	public String getSuffix() {
		return obm.getSuffix();
	}

	public String getTitle() {
		return obm.getTitle();
	}

	public int hashCode() {
		return obm.hashCode();
	}

	public boolean isCollected() {
		return obm.isCollected();
	}

	public void setAka(String aka) {
		obm.setAka(aka);
	}

	public void setAnniversary(Date anniversary) {
		obm.setAnniversary(anniversary);
	}

	public void setAnniversaryId(Integer anniversaryId) {
		obm.setAnniversaryId(anniversaryId);
	}

	public void setAssistant(String assistant) {
		obm.setAssistant(assistant);
	}

	public void setBirthday(Date birthday) {
		obm.setBirthday(birthday);
	}

	public void setBirthdayId(Integer birthdayId) {
		obm.setBirthdayId(birthdayId);
	}

	public void setCollected(boolean collected) {
		obm.setCollected(collected);
	}

	public void setComment(String comment) {
		obm.setComment(comment);
	}

	public void setCompany(String company) {
		obm.setCompany(company);
	}

	public void setEntityId(Integer entityId) {
		obm.setEntityId(entityId);
	}

	public void setFirstname(String firstname) {
		obm.setFirstname(firstname);
	}

	public void setLastname(String lastname) {
		obm.setLastname(lastname);
	}

	public void setManager(String manager) {
		obm.setManager(manager);
	}

	public void setMiddlename(String middlename) {
		obm.setMiddlename(middlename);
	}

	public void setService(String service) {
		obm.setService(service);
	}

	public void setSpouse(String spouse) {
		obm.setSpouse(spouse);
	}

	public void setSuffix(String suffix) {
		obm.setSuffix(suffix);
	}

	public void setTitle(String title) {
		obm.setTitle(title);
	}

	public void setUid(Integer uid) {
		obm.setUid(uid);
	}

	public String toString() {
		return obm.toString();
	}

	@Override
	public int compareTo(MinigContact o) {
		if (obm.getUid() == o.obm.getUid()) {
			return 0;
		} else {
			return getLastname().compareToIgnoreCase(o.getLastname());
		}

	}

	private boolean empty(String s) {
		return s == null || s.trim().length() == 0;
	}

	public String getDisplayName() {
		StringBuilder ret = new StringBuilder();

		// last filled & maybe first
		if (!empty(getLastname())) {
			ret.append(getLastname());
			if (!empty(getFirstname())) {
				ret.append(' ');
				ret.append(getFirstname());
			}
		} else if (!empty(getFirstname())) { // last empty, first filled
			ret.append(getFirstname());
		} else { // both are empty
			logger.warn("contact with both first & last empty: obm.id: "
					+ obm.getUid() + " mails: " + getEmails().size());
			ret.append("[John Doe] (obm.id: " + obm.getUid() + ")");
		}

		return ret.toString();
	}

}
