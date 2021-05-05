package org.msh.pharmadex.mbean;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.service.ApplicantService;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.util.JsfUtils;


@ManagedBean
@ViewScoped
public class SelectApplicantMBean {

	@ManagedProperty(value="#{userSession}")
	UserSession userSession;
	
	@ManagedProperty(value="#{prodApplicationsService}")
	private ProdApplicationsService prodApplicationsService;
	
	@ManagedProperty(value = "#{applicantService}")
	ApplicantService applicantService;
	
	private Applicant selectedApplicant = null;

	public boolean hasApplicant(){
		return getApplicant() != null;
	}
	
	/**
	 * Get applicant from amendment
	 * @return
	 */
	public Applicant getApplicant(){
		return selectedApplicant;
	}
	/**
	 * Set applicant to amendment
	 * @param applicant
	 */
	public void setApplicant(Applicant applicant){
		this.selectedApplicant = applicant;
	}

	public User getApplicantUser(){
		setApplicant(getApplicantService().findApplicant(getApplicant().getApplcntId()));
		return getApplicantService().fetchResponsibleUser(getApplicant());
	}

	public Applicant getSelectedApplicant() {
		return selectedApplicant;
	}

	public void setSelectedApplicant(Long selAppId) {
		if(selAppId != null && selAppId> 0)
			this.selectedApplicant = getApplicantService().findApplicant(selAppId);
		else
			this.selectedApplicant = null;
	}
	
	public void setSelectedApplicant(Applicant selApp) {
		if(selApp != null && selApp.getApplcntId() > 0)
			this.selectedApplicant = getApplicantService().findApplicant(selApp.getApplcntId());
		else
			this.selectedApplicant = null;
	}

	/**
	 * show applicants available to this user
	 * 
	 */
	public List<Applicant> completeApplicantList(String query) {
		try {
			List<Applicant> applicants = getProdApplicationsService().fetchApplicants(getUserSession());
			return JsfUtils.completeSuggestions(query, applicants);
		} catch (Exception ex){
			ex.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ex.getMessage()));
		}
		return null;
	}

	/**
	 * Applicant change listener
	 * @param event
	 */
	public void appChangeListenener(AjaxBehaviorEvent event) {
		//nothing to do yet!!!
	}

	public boolean hasSelectedApplicant(){
		return getSelectedApplicant() != null;
	}

	/**
	 * Store selected applicant to amendment data
	 */
	public void storeSelectedApplicant(){
		setApplicant(getSelectedApplicant());
	}
	
	/**
	 * varification select Applicant on tab Applicant
	 * @return if not select - selectapplicant
	 */
	public String validateApplicant(){
		if(getSelectedApplicant() != null && 
				getSelectedApplicant().getApplcntId() > 0 &&
				getSelectedApplicant().getAppName().length() > 0){
			return "";
		}else{
			return "selectapplicant";
		}
	}
	
	public ProdApplicationsService getProdApplicationsService() {
		return prodApplicationsService;
	}

	public void setProdApplicationsService(ProdApplicationsService prodApplicationsService) {
		this.prodApplicationsService = prodApplicationsService;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public ApplicantService getApplicantService() {
		return applicantService;
	}

	public void setApplicantService(ApplicantService applicantService) {
		this.applicantService = applicantService;
	}
	
	

}
