package org.msh.pharmadex.mbean.amendment;

import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.Address;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.amendment.AmdAPPLICANT;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This MBean serves Applicant data amendment
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdAPPLICANTMBean implements IAmendment {

	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;

	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;
	
	private AmdAPPLICANT amendment=null;

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdAPPLICANT)amd;
	}

	@Override
	public Amendment findAmendment(Long id) {
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdAPPLICANT(id);
		}else{
			amendment = new AmdAPPLICANT();
		}
		return amendment;
	}

	@Override
	public void cleanUp() {
		setAmendment(null);
	}

	@Override
	public String validateApplicant() {
		return getAmendmentService().validateSelectApplicant(getAmendment());
	}

	@Override
	public String validateProduct() {
		return "";
	}
	
	@Override
	public String validateDetail() {
		if(amendment.getApplicant().getAppName().equals(amendment.getApplName()) &&
				amendment.getApplicant().getAddress().equals(amendment.getAddress()))
			return "noamdchanges";
		return "";
	}

	@Override
	public String save(String step) {
		AmdAPPLICANT amd = getAmendmentService().saveAndFlush(amendment);
		if(amd != null){
			setAmendment(amd);
			return "";
		}else{
			return "global_fail";
		}
	}

	@Override
	public String submit() {
		return save("");
	}

	@Override
	public String reject() {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().rejectAmdAPPLICANT(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdAPPLICANT(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	/**
	 * Get applicant from amendment
	 * @return
	 */
	public Applicant getApplicant(){
		if(amendment != null)
			return amendment.getApplicant();
		return null;
	}
	
	public void setApplicantName(String name){
		amendment.setApplName(name);
	}

	public String getApplicantName(){
		if(amendment.getApplName() != null && amendment.getApplName().length() > 0){
			return amendment.getApplName();
		}else{
			amendment.setApplName(getApplicant().getAppName());
		}
		return amendment.getApplName();
	}
	
	public Address getApplicantAddress(){
		if(amendment.getAddress() != null && amendment.getAddress().getAddress1().length() > 0){
			return amendment.getAddress();
		}else{
			getAmendmentService().copyAddress(getApplicant().getAddress(), amendment);
		}
		return amendment.getAddress();
	}
	
	public boolean isApplicantNameChanged(){
		boolean ch = false;
		if(getApplicant() != null && amendment != null)
			ch = !getApplicant().getAppName().equals(amendment.getApplName());
		return ch;
	}

	public boolean isApplicantAddressChanged(){
		boolean ch = false;
		if(amendment != null && amendment.getApplicant() != null)
			ch = !amendment.getApplicant().getAddress().equals(amendment.getAddress());
		return ch;
	}
	
	public boolean  hasApprDate(){
		if(getAmendment() != null && getAmendment().getApprDate() != null)
			return true;
		return false;
	}

	public UserSession getUserSession() {
		return userSession;
	}
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	
	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}
	
	/**
	 * Get old address for Summary
	 * @return
	 */
	public Address getOldAddress(){
		if (getAmendment().getState() == AmdState.APPROVED){
			return getAmendment().getAddress();
		}else{
			return getApplicant().getAddress();
		}
	}
	
	/**
	 * Get new address for Summary
	 * @return
	 */
	public Address getNewAddress(){
		if (getAmendment().getState() == AmdState.APPROVED){
			return getApplicant().getAddress();
		}else{
			return getAmendment().getAddress();
		}
	}
	
	/**
	 * Get old applicant name for Summary
	 * @return
	 */
	public String getOldName(){
		if (getAmendment().getState() == AmdState.APPROVED){
			return amendment.getApplName();
		}else{
			return getApplicant().getAppName();
		}
	}
	
	/**
	 * Get new Applicant name for summary
	 * @return
	 */
	public String getNewName(){
		if (getAmendment().getState() == AmdState.APPROVED){
			return getApplicant().getAppName();
		}else{
			return amendment.getApplName();
		}
	}
}
