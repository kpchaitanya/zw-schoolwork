package edu.gatech.oad.rocket.findmythings.server.security;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.util.StringUtils;

import com.google.appengine.api.datastore.PhoneNumber;
import com.google.inject.Inject;

import edu.gatech.oad.rocket.findmythings.server.model.AppMember;

public class ProfileIniRealm extends IniRealm implements ProfileRealm {

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ProfileIniRealm.class.getName());

	public static final String PROFILES_SECTION_NAME = "profiles";

	private class IniAccount extends SimpleAccount implements AppMember {

		private static final long serialVersionUID = -1836831286248630414L;
		private String name = null;
		private PhoneNumber phone = null;
		private String address = null;
		private boolean isAdmin = false;

		IniAccount(String principal, String credentials, String realmName, boolean isAdmin) {
			super(principal, credentials, realmName);
			setIsAdmin(isAdmin);
		}

		@Override
		public boolean isRegistered() {
			return true;
		}

		@Override
		public String getEmail() {
			return (String)this.getPrincipals().getPrimaryPrincipal();
		}

		@Override
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		private void setName(String name) {
			this.name = name;
		}

		@Override
		public PhoneNumber getPhone() {
			return phone;
		}

		/**
		 * @param phone the phone to set
		 */
		private void setPhone(PhoneNumber phone) {
			this.phone = phone;
		}

		@Override
		public String getAddress() {
			return address;
		}

		/**
		 * @param address the address to set
		 */
		private void setAddress(String address) {
			this.address = address;
		}

		public boolean isAdmin() {
			return isAdmin;
		}

		private void setIsAdmin(boolean isAdmin) {
			this.isAdmin = isAdmin;
		}

	}

	public ProfileIniRealm() {
		super();
		setAuthenticationTokenClass(UsernamePasswordToken.class);
		setCredentialsMatcher(new PasswordMatcher());
	}

	@Inject
	public ProfileIniRealm(Ini ini) {
		this();
		setAuthenticationTokenClass(UsernamePasswordToken.class);
		setCredentialsMatcher(new PasswordMatcher());
		setIni(ini);
	}

    @Override
    protected void processUserDefinitions(Map<String, String> users) {
	if (users == null || users.isEmpty()) {
    		return;
    	}

    	Ini.Section profilesSection = getIni().getSection(PROFILES_SECTION_NAME);

	for (String email : users.keySet()) {
		String[] passwordAndRolesArray = StringUtils.split(users.get(email));
    		String password = passwordAndRolesArray[0];

    		String profileValue = profilesSection.get(email);
    		String[] profileValuesArray;
    		String name = null;
    		PhoneNumber phone = null;
    		String address = null;
			boolean hasProfile = false;

    		if (profileValue != null) {
    			profileValuesArray = StringUtils.split(profileValue, ',', '"', '"', false, true);
    			if (profileValuesArray.length == 3) {
    				name = profileValuesArray[0];
    				phone = profileValuesArray[1] == null ? null : new PhoneNumber(profileValuesArray[1]);
    				address = profileValuesArray[2];
    			}
				hasProfile = (name != null || phone != null || address != null);
    		}

    		SimpleAccount account = getUser(email);

			if (!(account instanceof IniAccount) && hasProfile) {
    			this.users.remove(email);
    			account = null;
    		}

			Set<String> roles = new HashSet<>();
			Set<Permission> permissions = new HashSet<>();

			for (int i = 1; i < passwordAndRolesArray.length; i++) {
				String roleName = passwordAndRolesArray[i];
				roles.add(roleName);

				SimpleRole role = getRole(roleName);
				if (role != null) {
					permissions.addAll(role.getPermissions());
				}
			}

    		if (account == null) {
				account = new IniAccount(email, password, getName(), roles.contains("admin"));
    		}

    		account.setCredentials(password);
    		account.setRoles(roles);
			account.addObjectPermissions(permissions);

			if (hasProfile) {
    			((IniAccount) account).setName(name);
    			((IniAccount) account).setPhone(phone);
    			((IniAccount) account).setAddress(address);
    		}
    		
    		add(account);
    	}
    }

    @Override
    public boolean accountExists(String email) {
		return email != null && getUser(email) != null;
	}

    @Override
    public AppMember getAccount(String email) {
    	if (email == null) return null;
    	SimpleAccount account = getUser(email);
    	if (account instanceof AppMember)
    		return (AppMember)account;
    	return null;
    }

}
