package org.msh.pharmadex.service;

import org.msh.pharmadex.auth.UserDetailsAdapter;
import org.msh.pharmadex.dao.UserDAO;
import org.msh.pharmadex.dao.iface.RoleDAO;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.Role;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: usrivastava
 * Date: 1/11/12
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class UserService implements Serializable {

	private static final long serialVersionUID = -4704319317657081206L;
	@Autowired
	UserDAO userDAO;

	@Autowired
	RoleDAO roleDAO;

	@Autowired
	private ApplicantService applicantService;

	@Autowired
	private PasswordEncoder passwordEncoder;


	private Long userId=null;
	private User user=null;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	/**
	 * calls very often some cache implemented
	 * @param id
	 * @return
	 */
	public User findUser(Long id) {
		if(getUserId() != id){
			setUser(userDAO.findUser(id));
			setUserId(id);
		}
		return getUser();
	}

	public List<User> findAllUsers() {
		return userDAO.allUsers();
	}

	public List<User> findUsersBySite(Long id) {
		return userDAO.findByRxSite(id);
	}

	public List<User> findUnregisteredUsers() {
		return userDAO.findNotRegistered();
	}

	/**
	 * To reset user's password, put an empty string to the field password
	 * @param username
	 * @return
	 * @throws NoResultException
	 */
	public User findUserByUsername(String username) throws NoResultException {
		User ret= userDAO.findByUsername(username);

		return ret;
	}

	public List<User> findUserByApplicant(Long applicantId) throws NoResultException {
		return userDAO.findByApplicant(applicantId);
	}

	public String createPublicUser(User user) {
		//Set the user enable to access the system
		user.setEnabled(true);
		List<Role> rList = new ArrayList<Role>();
		Role r = roleDAO.findOne(1);
		Role r2 = roleDAO.findOne(4);
		rList.add(r);
		rList.add(r2);
		user.setRoles(rList);
		return userDAO.saveUser(passwordGenerator(user));
	}

	public String createUser(User user) {
		return userDAO.saveUser(passwordGenerator(user));
	}


	public User passwordGenerator(User user) {
		UserDetailsAdapter userDetails = new UserDetailsAdapter(user);
		String password = userDetails.getPassword();
		user.setPassword(passwordEncoder.encode(password));
		System.out.println("========================================");
		System.out.println("password +== " + password);
		System.out.println("========================================");
		return user;
	}

	public String changePwd(User user, String oldpwd, String newpwd1) {
		if (!verifyUser(user.getUserId(), oldpwd)) return "PWDERROR";

		user.setPassword(newpwd1);
		user = passwordGenerator(user);
		userDAO.updateUser(user);
		return "persisted";
	}

	public boolean verifyUser(Long userID, String oldpwd) {
		User userFromDb = userDAO.findUser(userID);
//		Object salt = saltSource.getSalt(new UserDetailsAdapter(userFromDb));

//		if (!passwordEncoder.isPasswordValid(userFromDb.getPassword(), oldpwd, salt))
//			return true;
//		return false;
		return passwordEncoder.matches(oldpwd, userFromDb.getPassword());
	}

	public boolean userHasRole(User user, String roleName){
		if (user==null) return false;
		if (roleName==null) return false;
		if ("".equals(roleName)) return false;
		long id = user.getUserId();
		user = userDAO.findUser(id);
		List<Role> roles = user.getRoles();
		if (roles==null) return false;
		if (roles.size()==0) return false;
		String intRoleName="";
		if ("moderator".equalsIgnoreCase(roleName)){
			intRoleName = "ROLE_MODERATOR";
		}else if ("head".equalsIgnoreCase(roleName)){
			intRoleName = "ROLE_HEAD";
		}else if ("assesor".equalsIgnoreCase(roleName)){
			intRoleName = "ROLE_REVIEWER";
		}else if ("assesor".equalsIgnoreCase(roleName)){
			intRoleName = "ROLE_CSD";
		}
		for(Role role:roles){
			if (role.getRolename().equalsIgnoreCase(intRoleName))
				return true;
		}
		return false;
	}

	public boolean userHasRole(User user, UserRole role){
		if (user == null) return false;
		if (role == null) return false;

		long id = user.getUserId();
		user = userDAO.findUser(id);
		if(user != null){
			List<Role> roles = user.getRoles();
			if (roles == null) return false;
			if (roles.size() == 0) return false;

			for(Role r:roles){
				if(r.getRolename().equals(role.name()))
					return true;
			}
		}

		return false;
	}

	public List<User> findProcessors() {
		return userDAO.findProcessors();
	}

	public List<User> findModerators() {
		return userDAO.findModerators();
	}

	public User updateUser(User user) {
		return userDAO.updateUser(user);
	}

	public User findByUsernameOrEmail(User u) {
		return userDAO.findByUsernameOrEmail(u);
	}

	public boolean isUsernameDuplicated(String username) {
		return userDAO.isUsernameDuplicated(username);
	}

	public boolean isEmailDuplicated(String email) {
		return userDAO.isEmailDuplicated(email);
	}

}
