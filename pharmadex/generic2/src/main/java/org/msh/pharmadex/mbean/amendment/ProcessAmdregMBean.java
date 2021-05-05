package org.msh.pharmadex.mbean.amendment;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.FeePayment;
import org.msh.pharmadex.domain.amendment.AmdConditions;
import org.msh.pharmadex.domain.amendment.AmdDocuments;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.enums.PharmadexDB;
import org.msh.pharmadex.domain.enums.YesNoNA;
import org.msh.pharmadex.service.amendment.AmendmentDictionaryService;
import org.msh.pharmadex.service.amendment.AmendmentService;
import org.msh.pharmadex.util.JsfUtils;
import org.msh.pharmadex.util.Scrooge;
import org.primefaces.event.TabChangeEvent;

/**
 * Serve amendment initiation process. Common logic for all types of amendments
 * Can initialize new amendment or take one from the flash
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class ProcessAmdregMBean {

	private int selectedTab;
	private PharmadexDB pharmadexDB = null;
	private String rejectComment = "";
	private String approveComment = "";

	private static final String RETURN_TO = "return_to";
	private static final String AMENDMENT_ID = "amendmentId";
	private static final String AMENDMENT_PHARM = "pharm";
	private int maxLength = 500;
	private int maxCutLength = maxLength-15;
	
	@ManagedProperty(value="#{userSession}")
	UserSession userSession;

	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty("#{flash}")
	private Flash flash;
	
	@ManagedProperty(value="#{amendmentDictionaryService}")
	private AmendmentDictionaryService amendmentDictionaryService;

	/**
	 * What to do in case cancel and/or submit
	 */
	private String returnPath="/home.faces";


	/**
	 * Current amendment. common part
	 */
	private IAmendment amendmentMbean;

	@PostConstruct
	public void initAmendment(){
		//cleanAmdMBeans();
		setRejectComment("");
		
		this.pharmadexDB = (PharmadexDB) getFlash().get(AMENDMENT_PHARM);
		if(pharmadexDB != null){
			Long amdId = (Long) getFlash().get(AMENDMENT_ID);
			if(amdId != null){
				setRightAmdMBean(amdId);

				if(userSession.isHead()){
					setRejectComment(getAmendmentMbean().getAmendment().getStaffComment());
					setApproveComment(getAmendmentMbean().getAmendment().getStaffComment());
				}
			}else
				informUser("global_fail", FacesMessage.SEVERITY_ERROR);
		}else
			setReturnPath("/secure/amendment/amdsubmittedlist.faces");
		//return path
		String path = (String) getFlash().get(RETURN_TO);
		if(path != null){
			setReturnPath(path);
		}
	}
	
	public List<AmdListDTO> getSubmittedAmendments(){
		List<AmdListDTO> amds = getAmendmentService().fetchSubmittedAmds(userSession);
		return amds;
	}
	
	/**
	 * Inform user
	 * @param key message key
	 * @param severity
	 */
	private void informUser(String key, Severity severity) {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
		context.addMessage(null, new FacesMessage(severity, bundle.getString(key), ""));
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:growl");
	}
		
	public String getReturnPath() {
		return returnPath;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}
	
	/**
	 * Set amendment type and init a new Amendment in a corresponding MBean
	 * @param amdType
	 */
	public void setAmdType(PharmadexDB pharmDB) {
		this.pharmadexDB = pharmDB;
	}
	
	public PharmadexDB getPharmadexDB() {
		return pharmadexDB;
	}
	/**
	 * Set amendment - specific MBean in accordance with amendment's type
	 * @param amendment
	 */
	private void setRightAmdMBean(Long amdId) {
		IAmendment amdMbean = (IAmendment)JsfUtils.getManagedBean(getPharmadexDB().getMBeanName());
		amdMbean.findAmendment(amdId);
		setAmendmentMbean(amdMbean);	
	}
	
	public void onTabChange(TabChangeEvent event) {
		if(getPharmadexDB()!=null){
			if(!(getAmendmentMbean().getAmendment()!=null
					&& getAmendmentMbean().getAmendment().getId()!=null
					&& getAmendmentMbean().getAmendment().getId()>0)){
				
				Scrooge.goToHome();
				return ;
			}
		}		
	}
	
	/**
	 * Cancel current amendment
	 */
	public String cancel(){
		return getReturnPath();
	}
	/**
	 * State will be "RECOMMENDED"	
	 */		
	public String recommended(){
		//TODO timeline
		String comm = getAmendmentMbean().getAmendment().getStaffComment();								
		String err = validateMaxLength(comm);
		if(!"".equals(err)){
			getAmendmentMbean().getAmendment().setStaffComment(comm.substring(0, maxCutLength).trim());		
		}		 
		if(err.length()==0){
			getAmendmentMbean().getAmendment().setState(AmdState.RECOMMENDED);
			getAmendmentMbean().getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
			getAmendmentMbean().getAmendment().setUpdatedDate(new Date());	
			err = getAmendmentMbean().save("");
			if("".equals(err)){
				informUser("global.success", FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
				return cancel();
			}else{
				informUser(err, FacesMessage.SEVERITY_ERROR);
				return null;
			}			
		}else{
			informUser(err, FacesMessage.SEVERITY_ERROR);
			return null;
		}			
	}
	
	/**
	 * State will be "NOT_RECOMMENDED"	
	 */	
	public String notRecommended(){
		String comm = getAmendmentMbean().getAmendment().getStaffComment();
		if(comm != null && comm.trim().length() > 0){
			//TODO timeline			
			String err = validateMaxLength(comm);
			if(!"".equals(err)){
				getAmendmentMbean().getAmendment().setStaffComment(comm.substring(0, maxCutLength).trim());
			}
			
			if(err.length()==0){
				getAmendmentMbean().getAmendment().setState(AmdState.NOT_RECOMMENDED);
				getAmendmentMbean().getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
				getAmendmentMbean().getAmendment().setUpdatedDate(new Date());
				err = getAmendmentMbean().save("");
				if("".equals(err)){
					informUser("global.success", FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
					return cancel();
				}else{
					informUser(err, FacesMessage.SEVERITY_ERROR);
					return null;
				}
				
			}else{
				informUser(err, FacesMessage.SEVERITY_ERROR);
				return null;
			}			
		}else{
			informUser("error_empty_comment", FacesMessage.SEVERITY_ERROR);
			return null;
		}
	}
	/**
	 * State will be "REJECTED"	
	 */			
	public String reject(){
		//TODO timeline
		String comm = getRejectComment();
		String err = validateMaxLength(getRejectComment());	
		if(!"".equals(err)){			
			setRejectComment(comm.substring(0, maxCutLength).trim());
		}
		
		if(err.length() == 0){
			if(userSession.isReviewer()){			
				getAmendmentMbean().getAmendment().setStaffComment(comm);				
			}else if(userSession.isHead()){			
				getAmendmentMbean().getAmendment().setRegistrarComment(comm);				
			}
			
			getAmendmentMbean().getAmendment().setState(AmdState.REJECTED);
			getAmendmentMbean().getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
			getAmendmentMbean().getAmendment().setUpdatedDate(new Date());
			
			err = getAmendmentMbean().reject();
			if(err.length() == 0){
				informUser("global.success", FacesMessage.SEVERITY_INFO);
				FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
				return cancel();
			}
		}
		
		informUser(err, FacesMessage.SEVERITY_ERROR);
		return null;
	}
	/**
	 * State will be "APPROVED"	
	 */	
	public String approve(){
		//TODO timeline
		if(!FacesContext.getCurrentInstance().isValidationFailed()){
			String comm = getApproveComment();	
			
			String err = validateMaxLength(getApproveComment());
			if(err.length() == 0){
				 getAmendmentMbean().getAmendment().setRegistrarComment(comm);
				 getAmendmentMbean().getAmendment().setState(AmdState.APPROVED);
				 getAmendmentMbean().getAmendment().setEffectiveDate(new Date());
				 getAmendmentMbean().getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
				 getAmendmentMbean().getAmendment().setUpdatedDate(new Date());
				 
				 err = getAmendmentMbean().implement(null);
				 if(err.length() == 0){
					 
					informUser("global.success", FacesMessage.SEVERITY_INFO);
					FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
					return cancel();
				}
			}else{
				comm = comm.substring(0, maxCutLength).trim();
				setApproveComment(comm);
			}
			
			informUser(err, FacesMessage.SEVERITY_ERROR);
			return null;
		}
		return null;
	}
	/**
	 * Check for the maximum  length field : ApproveComment || StaffComment || RejectComment
	 */	
	public String validateMaxLength(String com){
		if(com.length()>maxLength){
			return "Error.maxLength";
		}
		return "";		
	}
	
	public boolean canVerifCheckList(){
		if (userSession.isReviewer()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.SUBMITTED)
					&& getAmendmentMbean().getAmendment().getPayment().isPayment_received())
				return true;
		}
		return false;
	}
	
	public boolean canVerifPaymant(){
		if (userSession.isReviewer()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.SUBMITTED)
					&& !getAmendmentMbean().getAmendment().getPayment().isPayment_received())
				return false;
		}
		return true;
	}
	
	public boolean canRecomNotRecom() {
		if (userSession.isReviewer()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.SUBMITTED)
					&& getAmendmentMbean().getAmendment().getPayment().isPayment_received()){
				
				String err = validateDocs();
				if(err.length()==0){
					 err = validateConditions();
					 if(err.length()==0){
						 return true;
					 }
				}				
			}				
		}		
		return false;
	}
	
	public boolean canRejected() {
		if(userSession.isReviewer()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.SUBMITTED))
				return true;
		}
		if (userSession.isHead()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.RECOMMENDED)
					|| getAmendmentMbean().getAmendment().getState().equals(AmdState.NOT_RECOMMENDED))
				return true;
		}
		return false;
	}
	
	public boolean canApproved() {
		if (userSession.isHead()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.RECOMMENDED)
					|| getAmendmentMbean().getAmendment().getState().equals(AmdState.NOT_RECOMMENDED))
				return true;
		}
		return false;
	}
		
	public void changeStatusListener() {
		getAmendmentMbean().getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
		getAmendmentMbean().getAmendment().setUpdatedDate(new Date());
		String err = getAmendmentMbean().save("");
		if(err.length()==0){
			informUser("global.success", FacesMessage.SEVERITY_INFO);
		}else{
			informUser(err, FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Get name of Summary tab template
	 * @return
	 */
	public String getSummaryTemplate(){
		if(getPharmadexDB() != null)
			return getPharmadexDB().getSummaryTemplate();
		return "/templates/amendment/badamd.xhtml";
	}

	/**
	 * Get payment from current amendment
	 * @return
	 */
	public FeePayment getPayment(){
		if(getAmendmentMbean()!= null){
			if(getAmendmentMbean().getAmendment()!=null){
				return getAmendmentMbean().getAmendment().getPayment();
			}else{
				return null;
			}
		}else{
			return null;
		}
	}

	public void onAnswerChangeListenener() {
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:amdmenu");
	}
	/**
	 * getAll AmendmentDocument by current Amendment
	 */ 
	public List<AmdDocuments> getListAmdDocs() {
		return getAmendmentMbean().getAmendment().getDocuments();
	}
	/**
	 * getAll AmendmentCondition by current Amendment
	 */ 
	public List<AmdConditions> getListAmdConditions(){
		return getAmendmentMbean().getAmendment().getConditions();
	}
	/**
	 * 	System saves checklists filled by Reviewer
	 */
	public void save(){
		Amendment amd = getAmendmentService().saveAndFlush(getAmendmentMbean().getAmendment());		
		if(amd != null){			
			informUser("global.success", FacesMessage.SEVERITY_INFO);
		}else{
			informUser("global_fail", FacesMessage.SEVERITY_ERROR);
		}		
	}
	private String validateConditions(){		
		if(getListAmdConditions() != null && getListAmdConditions().size()>0){
			int index = 0;
			for(AmdConditions it:getListAmdConditions()){
				if(it.getAnswer() != null && it.getAnswer().getStaffValue() != null && 
						(it.getAnswer().getStaffValue().equals(YesNoNA.YES) || it.getAnswer().getStaffValue().equals(YesNoNA.NO) ))							
					index++;
			}
			if(index != getListAmdConditions().size())
				return "amdcondition_incomplete";
		}		
		return "";
	}
	
	private String validateDocs(){		
		if(getListAmdDocs() != null && getListAmdDocs().size()>0){
			int index = 0;
			for(AmdDocuments it:getListAmdDocs()){
				if(it.getAnswer() != null && it.getAnswer().getStaffValue() != null && 
						(it.getAnswer().getStaffValue().equals(YesNoNA.YES) || it.getAnswer().getStaffValue().equals(YesNoNA.NO) ))							
					index++;
			}
			if(index != getListAmdDocs().size())
				return "amddocs_incomplete";
		}
		return "";
	}

	/**
	 * Store amendment's id to the flash storage
	 * @param amdId
	 */
	public void setAmdIdToFlash(Long amdId){
		getFlash().put(AMENDMENT_ID, amdId);
	}

	/**
	 * Store amendment's id to the flash storage
	 * @param amdId
	 */
	public void setAmdTypeToFlash(PharmadexDB pharmDB){
		getFlash().put(AMENDMENT_PHARM, pharmDB);
	}

	/**
	 * Set submit/cancel path to the flash storage
	 * @param path
	 */
	public void setReturnPathToFlash(String path){
		getFlash().put(RETURN_TO, path);
	}

	
	public Flash getFlash() {
		return flash;
	}

	public void setFlash(Flash flash) {
		this.flash = flash;
	}	
	
	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public int getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(int selectedTab) {
		this.selectedTab = selectedTab;
	}

	public IAmendment getAmendmentMbean() {
		return amendmentMbean;
	}

	public void setAmendmentMbean(IAmendment amendmentMbean) {
		this.amendmentMbean = amendmentMbean;
	}

	public String getRejectComment() {
		return rejectComment;
	}

	public void setRejectComment(String rejectComment) {
		this.rejectComment = rejectComment;
	}

	public String getApproveComment() {
		return approveComment;
	}
	public void setApproveComment(String approveComment) {
		this.approveComment = approveComment;
	}

	public AmendmentDictionaryService getAmendmentDictionaryService() {
		return amendmentDictionaryService;
	}

	public void setAmendmentDictionaryService(AmendmentDictionaryService amendmentDictionaryService) {
		this.amendmentDictionaryService = amendmentDictionaryService;
	}

	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}
}
