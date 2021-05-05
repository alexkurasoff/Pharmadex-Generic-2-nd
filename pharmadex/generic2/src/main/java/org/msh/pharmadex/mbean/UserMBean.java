package org.msh.pharmadex.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.Hibernate;
import org.hibernate.exception.ConstraintViolationException;
import org.msh.pharmadex.auth.PassPhrase;
import org.msh.pharmadex.dao.iface.RoleDAO;
import org.msh.pharmadex.dao.iface.WorkspaceDAO;
import org.msh.pharmadex.domain.Address;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.Letter;
import org.msh.pharmadex.domain.Mail;
import org.msh.pharmadex.domain.Role;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.enums.LetterType;
import org.msh.pharmadex.domain.enums.UserRole;
import org.msh.pharmadex.domain.enums.UserType;
import org.msh.pharmadex.service.ApplicantService;
import org.msh.pharmadex.service.LetterService;
import org.msh.pharmadex.service.MailService;
import org.msh.pharmadex.service.UserService;
import org.msh.pharmadex.util.Scrooge;
import org.primefaces.model.DualListModel;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.WebUtils;

/**
 * Created by IntelliJ IDEA.
 * User: usrivastava
 * Date: 1/12/12
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
@ManagedBean
@ViewScoped
public class UserMBean implements Serializable {
	@ManagedProperty(value = "#{userService}")
	UserService userService;
	@ManagedProperty(value = "#{applicantService}")
	ApplicantService applicantService;
	@ManagedProperty(value = "#{mailService}")
	MailService mailService;
	@ManagedProperty(value = "#{roleDAO}")
	RoleDAO roleDAO;
	@ManagedProperty(value = "#{letterService}")
	LetterService letterService;
	@ManagedProperty(value = "#{workspaceDAO}")
	WorkspaceDAO workspaceDAO;


	FacesContext facesContext = FacesContext.getCurrentInstance();
	java.util.ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");
	private User selectedUser;
	private UserType userType;
	private List<User> allUsers;
	private DualListModel<Role> roles = new DualListModel<Role>();
	private List<Role> allRoles;
	private List<Role> listRoles;
	private List<Role> selectedRoles;
	private boolean edit;
	private Long prevApplicantId;
	private Applicant userApp;
	private String email;
	private String applicName = "";
	private Long applicID = new Long(-1);
	private String backTo = "/admin/userslist_bk.faces";
	private boolean showResetPassword = false;

	public String exception() throws Exception {
		throw new Exception();
	}

	/*	@PostConstruct
	private void init() {
		selectedUser = new User();
		selectedUser.setAddress(new Address());
		selectedUser.setApplicant(new Applicant());
		setUserType(UserType.STAFF);
		userApp = new Applicant();   
	}*/

	public void onRowSelect() {
		setEdit(true);		
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType _userType) {
		this.userType = _userType;
		typeChangeListenener();
	}

	public void typeChangeListenener(){
		buildRolesList(userType);
		selectedRoles = new ArrayList<Role>();
		if(selectedUser != null && selectedUser.getUserId() != null){
			if(userType.equals(selectedUser.getType())){
				selectedRoles.addAll(selectedUser.getRoles());
			}
		}
		allRoles.removeAll(selectedRoles);
		roles = new DualListModel<Role>(allRoles, selectedRoles);
		if(userType.equals(UserType.STAFF)){
			cleanAssignCompany();
			selectedUser.setApplicant(userApp);
		}
	}

	public String goToResetPwd() {
		return "/public/resetpwd.faces";
	}

	public String startReset() {
		selectedUser = new User();
		selectedUser.setEmail(email);
		selectedUser = userService.findByUsernameOrEmail(selectedUser);
		resetPassword();
		return "/home.faces";

	}
	@Transactional
	public String saveUser() {
		selectedUser.setRoles(roles.getTarget());
		if(validateEmail()){
			facesContext = FacesContext.getCurrentInstance();
			if (selectedUser.getUserId() != null && selectedUser.getUserId() == 0)
				selectedUser.setUserId(null);
			selectedUser.setType(userType);

			selectedUser.setApplicant(getUserApp());

			if(selectedUser != null && selectedUser.getUsername().isEmpty()){
				String username = selectedUser.getName().replaceAll("\\s", "");
				selectedUser.setUsername(username);
			}
			/*if (selectedUser != null && selectedUser.getApplicant() != null && selectedUser.getApplicant().getApplcntId() != null)
            selectedUser.setApplicant(applicantService.findApplicant(selectedUser.getApplicant().getApplcntId()));
        else
            selectedUser.setApplicant(null);*/
			if (isEdit()) {
				updateuser();
				return "";
			}

			if (userService.isUsernameDuplicated(selectedUser.getUsername())) {
				FacesMessage msg = new FacesMessage(selectedUser.getUsername() + " "
						+ bundle.getString("valid_user_exist"));
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				facesContext.addMessage(null, msg);
				facesContext.validationFailed();
				return "";
			}

			if(getUserService().hasEmail(getSelectedUser())){
				if (userService.isEmailDuplicated(selectedUser.getEmail())) {
					FacesMessage msg = new FacesMessage(selectedUser.getEmail() + " "
							+ bundle.getString("valid_email_exist"));
					msg.setSeverity(FacesMessage.SEVERITY_ERROR);
					facesContext.addMessage(null, msg);
					facesContext.validationFailed();
					return "";
				}
			}

			String password = PassPhrase.getNext();
			selectedUser.setPassword(password);
			selectedUser.setRegistrationDate(new Date());
			Letter letter = letterService.findByLetterType(LetterType.NEW_USER_REGISTRATION);
			String address = calculateAddress();
			if(address != null){
				Mail mail = new Mail();
				mail.setMailto(selectedUser.getEmail());
				if(letter != null)
					mail.setSubject(letter.getSubject());
				mail.setMailto(address);
				mail.setUser(selectedUser);
				mail.setDate(new Date());
				mail.setMessage(bundle.getString("email_user_reg1") + selectedUser.getUsername() + bundle.getString("email_user_reg2") + password + bundle.getString("email_user_reg3"));
				String retvalue;
				try {
					retvalue = userService.createUser(selectedUser);
					allUsers = null;
				} catch (ConstraintViolationException e) {
					facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("email_exists")));
					return "";
				} catch (Exception e) {
					facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), e.getMessage()));
					e.printStackTrace();
					return "";
				}
				if (!retvalue.equalsIgnoreCase("persisted")) {
					facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), retvalue));
					return "";
				} else {
					try {
						if(selectedUser.isEnabled()){
							mailService.sendMail(mail, false);
							facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("send_password_success") +" ("+address+")", ""));
						}else{
							facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("not_send_password_success") +" ("+address+")", ""));
						}
						FacesContext context = FacesContext.getCurrentInstance();
						HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
						WebUtils.setSessionAttribute(request, "userMBean", null);
						return "/admin/userslist_bk.faces";
					} catch (Exception e) {
						e.printStackTrace();
						facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("email_error")));
						return "";
					}
				}
			}else{
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("email_error")));
				return "";
			}
		}else{
			return "";
		}
	}
	/**
	 * Validate selected user's email
	 * Rules:
	 * <ul>
	 * <li>Empty eMail allows only for Company user
	 * <li>If email not empty, then structure of address should be checked unconditionally
	 * </ul>
	 * @return
	 */
	public boolean validateEmail() {
		String errKey = getUserService().validateEmail(getSelectedUser());
		if(errKey.length()>0){
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString(errKey), bundle.getString(errKey)));
			getUserService().reloadUser(getSelectedUser());
			return false;
		}else{
			return true;
		}
	}

	/**
	 * Calculate the address password will be send
	 * If user has password - to user, otherwise to admin
	 * @return null if cannot resolve
	 */
	private String calculateAddress() {
		String ret = "";
		if(getUserService().hasEmail(getSelectedUser())){
			ret= selectedUser.getEmail();
		}else{
			List<User> admins = getUserService().findAdmins();
			if(admins != null & admins.size()>0){
				ret= admins.get(0).getEmail();
			}else{
				return null;
			}
		}
		if (ret.trim().length()>0){
			return ret;
		}else{
			return null;
		}
	}



	public String updateuser() {
		selectedUser.setRoles(roles.getTarget());
		if(validateEmail()){
			selectedUser.setType(userType);

			selectedUser.setApplicant(getUserApp());

			selectedUser.setUpdatedDate(new Date());
			FacesContext facesContext = FacesContext.getCurrentInstance();
			try {
				selectedUser = userService.updateUser(selectedUser);
				allUsers = null;
			} catch (ConstraintViolationException e) {
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("email_exists")));
			} catch (Exception e) {
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), e.getMessage()));
				e.printStackTrace();
			}
			if (selectedUser != null) {
				setEdit(false);
				selectedUser = new User();
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("global.success"), selectedUser.getName() + bundle.getString("global.success")));
				FacesContext context = FacesContext.getCurrentInstance();
				HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
				WebUtils.setSessionAttribute(request, "userMBean", null);
			} else {
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("no_user")));
			}
		}
		return backTo;
	}

	public void addUser() {
		selectedUser = new User();
		selectedUser.setAddress(new Address());
		//selectedUser.setApplicant(new Applicant());
		// set default
		userType = UserType.STAFF;
		typeChangeListenener();
		setEdit(false);
	}

	public String cleanAssignCompany() {
		//userApp = new Applicant();
		userApp=null;
		applicName = "-";
		applicID = new Long(-1);

		return "";
	}

	public String resetPassword() {
		if(validateEmail()){
			String email = calculateAddress();
			if(email != null){
				String password = PassPhrase.getNext();
				selectedUser.setPassword(password);
				System.out.println("Password == " + password);
				selectedUser.setUpdatedDate(new Date());
				Mail mail = new Mail();
				mail.setMailto(email);
				mail.setSubject("Password Reset");
				//        mail.setSubject(bundle.getString("reset_pwd_sub"));
				mail.setUser(selectedUser);
				mail.setDate(new Date());
				mail.setMessage("Your password has been successfully reset In order to access the system please use the username '" + selectedUser.getUsername() + "' and password '" + password + "' ");
				FacesContext facesContext = FacesContext.getCurrentInstance();
				try {
					selectedUser = userService.updateUser(userService.passwordGenerator(selectedUser));
				} catch (Exception e) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), e.getMessage()));
					e.printStackTrace();
					return "";
				}
				if (selectedUser == null) {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("save_error")));
					return "";
				} else {
					try {
						String sender = workspaceDAO.findAll().get(0).getRegistraremail();
						if (sender==null)
							sender = "info@msh.org";
						else if ("".equals(sender))
							sender = "info@msh.org";

						mailService.sendMailFromSender(mail, false,sender);
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("send_password_success") +" ("+email+")", ""));
						FacesContext context = FacesContext.getCurrentInstance();
						HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
						WebUtils.setSessionAttribute(request, "userMBean", null);
						return "/admin/userslist_bk.faces";
					} catch (Exception e) {
						e.printStackTrace();
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("email_error")));
						return "";
					}
				}
			}else{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), bundle.getString("email_error")));
				return "";
			}
		}else{
			return "";
		}

	}

	public void cancelUser() {
		selectedUser = new User();
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		WebUtils.setSessionAttribute(request, "userMBean", null);

	}

	public User getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(User selUser) {
		if(selUser != null && selUser.getUserId() != null){
			this.selectedUser = userService.findUser(selUser.getUserId());
			Hibernate.initialize(this.selectedUser.getApplicant());
			this.selectedRoles = this.selectedUser.getRoles();
			roles.setTarget(selectedRoles);
			if (this.selectedUser.getApplicant() != null && this.selectedUser.getApplicant().getApplcntId() != null){
				userApp = applicantService.findApplicant(this.selectedUser.getApplicant().getApplcntId());
				applicName = userApp.getAppName();
				applicID = userApp.getApplcntId();
			}else{
				userApp = new Applicant();
				applicName = "-";
				applicID = new Long(-1);
			}
			this.prevApplicantId = userApp.getApplcntId();
		}else
			Scrooge.goToHome();
	}

	public List<User> getAllUsers() {
		//if(allUsers==null)
		allUsers = userService.findAllUsers();
		return allUsers;
	}

	public void setAllUsers(List<User> allUsers) {
		this.allUsers = allUsers;
	}

	public DualListModel<Role> getRoles() {
		return roles;
	}

	public void setRoles(DualListModel<Role> roles) {
		this.roles = roles;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public Applicant getUserApp() {
		if(userApp != null) {
			return userApp;
		}else {
			return new Applicant();
		}
	}

	public void setUserApp(Applicant userApp) {
		this.userApp = userApp;

	}

	public String getApplicName(){
		return applicName;
	}

	public void setApplicName(String name){
		this.applicName = name;
	}

	public Long getApplicID(){
		return applicID;
	}

	public void setApplicID(Long id){
		this.applicID = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ApplicantService getApplicantService() {
		return applicantService;
	}

	public void setApplicantService(ApplicantService applicantService) {
		this.applicantService = applicantService;
	}

	public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public RoleDAO getRoleDAO() {
		return roleDAO;
	}

	public void setRoleDAO(RoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}

	public LetterService getLetterService() {
		return letterService;
	}

	public void setLetterService(LetterService letterService) {
		this.letterService = letterService;
	}

	public WorkspaceDAO getWorkspaceDAO() {
		return workspaceDAO;
	}

	public void setWorkspaceDAO(WorkspaceDAO workspaceDAO) {
		this.workspaceDAO = workspaceDAO;
	}

	public String getBackTo() {
		return backTo;
	}

	public void setBackTo(String backTo) {
		this.backTo = backTo;
	}

	private void buildRolesList(UserType type){
		allRoles = new ArrayList<Role>();
		if(listRoles == null)
			listRoles = (List<Role>) roleDAO.findAll();

		if(type != null){
			List<UserRole> list = type.getRolesList();
			if(list != null && list.size() > 0){
				for(Role r:listRoles){
					if(list.contains(UserRole.valueOf(r.getRolename())))
						allRoles.add(r);
				}
			}
		}
	}

	public boolean isShowResetPassword() {
		showResetPassword = false;		
		if(isEdit()){
			showResetPassword  = selectedUser != null? selectedUser.isEnabled():false;	
		}
		return showResetPassword;
	}

	public void setShowResetPassword(boolean showResetPassword) {
		this.showResetPassword = showResetPassword;
	}

}
