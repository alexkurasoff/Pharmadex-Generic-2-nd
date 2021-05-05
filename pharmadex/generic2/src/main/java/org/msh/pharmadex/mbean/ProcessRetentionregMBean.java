package org.msh.pharmadex.mbean;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.FeePayment;
import org.msh.pharmadex.domain.Invoice;
import org.msh.pharmadex.domain.enums.InvoiceState;
import org.msh.pharmadex.mbean.product.ProcessProdBnNA;
import org.msh.pharmadex.service.InvoiceService;
import org.msh.pharmadex.service.ProdApplicationsService;
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
public class ProcessRetentionregMBean {

	private int selectedTab;
	
	//private String rejectComment = "";
	//private String approveComment = "";
	private Applicant applicant =null;

	private static final String RETURN_TO = "return_to";
	private static final String RETENTION_ID = "retentionId";
	
	private int maxLength = 500;
	private int maxCutLength = maxLength-15;
	private Invoice invoice  = null;
	
	@ManagedProperty(value="#{userSession}")
	UserSession userSession;
		
	@ManagedProperty(value="#{retentionMBean}")
	RetentionMBean retentionMBean;

	@ManagedProperty("#{flash}")
	private Flash flash;
	
	@ManagedProperty(value="#{invoiceService}")
	private InvoiceService invoiceService;
	
	@ManagedProperty(value="#{prodApplicationsService}")
	ProdApplicationsService prodApplicationsService;
	
	/**
	 * What to do in case cancel and/or submit
	 */
	private String returnPath="/home.faces";


	@PostConstruct
	public void initInvoice(){
		//cleanAmdMBeans();
				
		Long invId = (Long) getFlash().get(RETENTION_ID);
		if(invId != null){
			findInvoice(invId);
			if(getInvoice()!=null){
				getRetentionMBean().setInvoice(invoice);
				getRetentionMBean().updateCurList();
				
				if(getInvoice().getApplicant()!=null){
					setApplicant(getInvoice().getApplicant());
				}
				
				/*if(getInvoice().getState().equals(InvoiceState.APPROVED)){
					
				}*/
			}						
			
			/*if(userSession.isHead()){
				setRejectComment(getAmendmentMbean().getAmendment().getStaffComment());
				setApproveComment(getAmendmentMbean().getAmendment().getStaffComment());
			}*/
		}/*else{
			informUser("global_fail", FacesMessage.SEVERITY_ERROR);
		}*/
		//return path
		String path = (String) getFlash().get(RETURN_TO);
		if(path != null){
			setReturnPath(path);
		}
	}
	/**
	 * 
	 * @param id - id Invoice
	 * @return find Invoice by id, or create new
	 */
	public Invoice findInvoice(Long id) {	
		if(id != null && id > 0){			
			setInvoice(getInvoiceService().findInvoicesById(id));
		}
		return invoice;
	}
	
	public List<InvoiceListDTO> getSubmittedInvoices(){
		List<InvoiceState> state = new ArrayList<InvoiceState>();
		state.add(InvoiceState.SUBMITTED);
		List<InvoiceListDTO> invs = getInvoiceService().fetchInvoicesByState(userSession.getLoggedInUserObj(), state, userSession);
		return invs;
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
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("retentionfrm:growl");
	}
		
	public String getReturnPath() {
		return returnPath;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}
		
	/**
	 * Cancel current amendment
	 */
	public String cancel(){
		return getReturnPath();
	}
	
	/**
	 * State will be "REJECTED"	
	 */			
	public String reject(){
		
		getInvoice().setState(InvoiceState.REJECTED);		
		//getInvoice().setRejectDate(new Date());
		getInvoice().setUpdatedBy(getUserSession().getLoggedInUserObj());
		getInvoice().setUpdatedDate(new Date());
		
		String err = save();//reject();
		if(err.length() == 0){
			informUser("global.success", FacesMessage.SEVERITY_INFO);
			FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
			return cancel();
		}
		
		informUser(err, FacesMessage.SEVERITY_ERROR);
		return null;
	}
	/**
	 * State will be "APPROVED"
	 *  get prodApplication which regExpiryDate < selected year in invoice
	 *	set regExpiryDate = selected date in dialog 
	 *	replace all prodApplication
	 */	
	public String approve(){
		//TODO timeline
		if(!FacesContext.getCurrentInstance().isValidationFailed()){	 		 
				
			 SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			 if(getInvoice().getApplicant()!=null && getInvoice().getRegExpiryDate()!=null){
				 String expiryDate =dateFormat.format(getInvoice().getRegExpiryDate());
				 getProdApplicationsService().changeRegExpiryDate(getInvoice().getApplicant().getApplcntId(), getInvoice().getCurYear(), expiryDate);
				 
				 getInvoice().setState(InvoiceState.APPROVED);		
				 getInvoice().setApprDate(new Date());				
				 getInvoice().setUpdatedBy(getUserSession().getLoggedInUserObj());
				 getInvoice().setUpdatedDate(new Date());		
				 String err = save();
				 if(err.length() == 0){
					 informUser("global.success", FacesMessage.SEVERITY_INFO);
					 FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
					 return cancel();
				 }else{ 
					 informUser(err, FacesMessage.SEVERITY_ERROR);
					 return null;
				 }						 	
			}			
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
		
	/*
	public boolean canRecomNotRecom() {
		if (userSession.isReviewer()){
			if(getInvoice().getState().equals(InvoiceState.SUBMITTED)
					&& getInvoice().getPayment().isPayment_received()){
				return true;				
			}				
		}		
		return false;
	}
	*/
	public boolean canRejected() {
		/*if(userSession.isReviewer()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.SUBMITTED))
				return true;
		}
		if (userSession.isHead()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.RECOMMENDED)
					|| getAmendmentMbean().getAmendment().getState().equals(AmdState.NOT_RECOMMENDED))
				return true;
		}*/
		if (userSession.isReceiver()){
			if(getInvoice().getState().equals(InvoiceState.SUBMITTED))
				return true;
		}
		return false;
	}
	
	public boolean canApproved() {
		/*if (userSession.isHead()){
			if(getAmendmentMbean().getAmendment().getState().equals(AmdState.RECOMMENDED)
					|| getAmendmentMbean().getAmendment().getState().equals(AmdState.NOT_RECOMMENDED))
				return true;
		}
		return false;*/
		if (userSession.isReceiver()){
			if(getInvoice().getState().equals(InvoiceState.SUBMITTED) && 
					getInvoice().getPayment().isPayment_received())
				return true;
		}
		return false;
	}
		
	public void changeStatusListener() {
		getInvoice().setUpdatedBy(getUserSession().getLoggedInUserObj());
		getInvoice().setUpdatedDate(new Date());
		String err = save();
		if(err.length()==0){
			informUser("global.success", FacesMessage.SEVERITY_INFO);
		}else{
			informUser(err, FacesMessage.SEVERITY_ERROR);
		}
	}
	

	/**
	 * Get payment from current amendment
	 * @return
	 */
	public FeePayment getPayment(){
		if(getInvoice()!= null){
			if(getInvoice().getPayment()!=null){
				return getInvoice().getPayment();
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
	 * Save current Invoice. State will be "SAVED".
	 */
	public String save(){			
		Invoice inv = getInvoiceService().getInvoiceDAO().saveAndFlush(invoice); //getInvoiceService().getInvoiceDAO().saveAndFlush(getInvoice());
		if(inv != null){
			findInvoice(inv.getId());
			return "";
		}else{
			return "global_fail";
		}
	}

	public void onTabChange(TabChangeEvent event) {		
		if(!(getInvoice()!=null
				&& getInvoice().getId()!=null
				&& getInvoice().getId()>0)){
			
			Scrooge.goToHome();
			return ;
		}				
	}
	/**
	 * @return true - if all field in Payment fielded out
	 */
	public String validatePayment(){
		if(getPayment()!=null){
			if(getPayment().getFeeSum()!=null && getPayment().getPayment_form()!=null && 
					getPayment().getPayment_payDate()!=null && !"".equals(getPayment().getPayment_receipt())){
				return "";
			}
		}
		return "selectPayment";
	}

	public boolean canVerifPaymant(){
		if (userSession.isReceiver()){
			if(getInvoice().getState().equals(InvoiceState.SUBMITTED)
					&& !getInvoice().getPayment().isPayment_received())
				return false;
		}
		return true;
	}
	
	
	/**
	 * Store invoice's id to the flash storage
	 * @param amdId
	 */
	public void setRetentionIdToFlash(Long retId){
		getFlash().put(RETENTION_ID, retId);
	}

	/**
	 * When invoice has been approved
	 * @return
	 */
	public Date getApprDate(){
		Date ret = getInvoice().getApprDate();
		if(ret==null){
			getInvoice().setApprDate(new Date());
		}
		return getInvoice().getApprDate();
	}
	
	/**
	 * When invoice has been approved
	 */
	public void setApprDate(Date dt){
		getInvoice().setApprDate(dt);
	}
	
	/**
	 * registration expire date should be calculated by common algorithm!
	 * @return
	 */
	public Date getRegExpiryDate(){
		Date ret = getInvoice().getRegExpiryDate();
		if (ret == null){
			Integer year = new Integer(getInvoice().getCurYear());
			//determine true expire date for a year given
			LocalDate baseLDate = LocalDate.of(year, 1, 1);
			Date ldate = Date.from(baseLDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			Date trueExpDate = ProcessProdBnNA.calcNextRegExpireDate(ldate);
			//set new expire date
			getInvoice().setRegExpiryDate(ProcessProdBnNA.calcNextRegExpireDate(trueExpDate));
		}
		return getInvoice().getRegExpiryDate();
	}
	
	public void setRegExpiryDate(Date date){
		getInvoice().setRegExpiryDate(date);
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

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}
	public Applicant getApplicant() {
		return applicant;
	}
	public void setApplicant(Applicant applicant) {
		this.applicant = applicant;
	}
	public RetentionMBean getRetentionMBean() {
		return retentionMBean;
	}
	public void setRetentionMBean(RetentionMBean retentionMBean) {
		this.retentionMBean = retentionMBean;
	}
	public ProdApplicationsService getProdApplicationsService() {
		return prodApplicationsService;
	}
	public void setProdApplicationsService(ProdApplicationsService prodApplicationsService) {
		this.prodApplicationsService = prodApplicationsService;
	}
	

}
