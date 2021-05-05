package org.msh.pharmadex.mbean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.commons.io.IOUtils;
import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.FeePayment;
import org.msh.pharmadex.domain.Invoice;
import org.msh.pharmadex.domain.enums.InvoiceState;
import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.TemplateType;
import org.msh.pharmadex.mbean.product.ProdTable;
import org.msh.pharmadex.mbean.product.TemplMBean;
import org.msh.pharmadex.service.InvoiceService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.util.JsfUtils;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.data.domain.Sort;

/**

 */
@ManagedBean
@ViewScoped
public class RetentionMBean implements Serializable {

	private static final long serialVersionUID = 3701073302604698257L;

	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;

	@ManagedProperty(value="#{selectApplicantMBean}")
	private SelectApplicantMBean selectApplicantMBean;

	@ManagedProperty("#{flash}")
	private Flash flash;

	@ManagedProperty(value="#{invoiceService}")
	private InvoiceService invoiceService;

	@ManagedProperty(value="#{productService}")
	ProductService productService;

	@ManagedProperty(value = "#{templMBean}")
	TemplMBean templMBean;	

	private List<ProdTable> curlist = new ArrayList<ProdTable>();
	/**
	 * flag - cur edit type - HUMAN|VETERINARY
	 */
	private ProdCategory lastClicked = ProdCategory.HUMAN;

	private static final String RETURN_TO = "return_to";
	private static final String RETENTION_ID = "retentionId";
	public static final String STEP_APPLICANT = "applTab";
	public static final String STEP_PRODUCT = "prodTab";
	public static final String STEP_PAYMENT = "payment";

	private Invoice invoice  = null;

	private List<String> years = new ArrayList<String>();

	private List<ProdTable> filteredProducts;
	private FeePayment  payment = null;
	/**
	 * What to do in case cancel and/or submit
	 */
	private String returnPath="/home.faces";

	@SuppressWarnings("serial")
	private static final Map<String, Integer> mapNumTabs = new HashMap<String, Integer>(){
		{			
			put(STEP_APPLICANT, 1);
			put(STEP_PRODUCT, 2);		
			put(STEP_PAYMENT, 3);			
		}
	};

	public String exception() throws Exception {
		throw new Exception();
	}

	@PostConstruct
	private void initOldRetention() {
		Long retentId = (Long) getFlash().get(RETENTION_ID);
		findInvoice(retentId);
		initYears();
		if(retentId != null){			
			getSelectApplicantMBean().setSelectedApplicant(getInvoice().getApplicant());	
			setPayment(getPayment());
		}
		//return path
		String path = (String) getFlash().get(RETURN_TO);
		if(path != null){
			setReturnPath(path);
		}
	}

	/**
	 * list cur year+next year
	 */
	private void initYears() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
		Date curDate = new Date();
		String curYear = dateFormat.format(curDate);		
		curDate.setYear(curDate.getYear()+1);
		String nextYear = dateFormat.format(curDate);	

		List<String> y = new ArrayList<String>();
		y.add(curYear);
		y.add(nextYear);
		if(getInvoice().getCurYear() == null || getInvoice().getCurYear().length()==0){
			setCurYear(curYear);
		}
		setYears(y);		
	}

	/**
	 * set Applicant
	 */
	private void initNewRetention() {
		getSelectApplicantMBean().setSelectedApplicant(getSelectApplicantMBean().getSelectedApplicant());		
		updateCurList();
	}

	/**
	 * @return true - if cur user - isCompany || isStaff, else - false
	 */
	public boolean showRetentionInMenu(){
		if(userSession.isCompany() || userSession.isStaff())
			return true;	
		else
			return false;
	}

	/**
	 * @return Last id Invoice + 1
	 */
	public String getInvoicenumber(){
		String idLast = "1";
		List <Invoice> allInvoice = getInvoiceService().getInvoiceDAO().findAll(new Sort(Sort.Direction.DESC, "id"));
		if(allInvoice!=null && allInvoice.size()>0){
			if(allInvoice.get(0)!=null){
				idLast = (allInvoice.get(0).getId()+1)+"";
			}
		}
		return idLast;
	}
	/**
	 * 
	 * @param id - id Invoice
	 * @return find Invoice by id, or create new
	 */
	public Invoice findInvoice(Long id) {	
		if(id != null && id > 0){			
			setInvoice(getInvoiceService().findInvoicesById(id));
		}else{
			invoice = new Invoice();			
			invoice.setPayment(new FeePayment());
			//add created date and creator for any
			invoice.setCreatedDate(new Date());
			invoice.setCreatedBy(getUserSession().getLoggedInUserObj());
			setInvoice(invoice);
		}
		return invoice;
	}

	/**
	 * Serve the Wizard
	 * @param event
	 * @return
	 */
	public String onFlowProcess(FlowEvent event) {
		String currentWizardStep = event.getOldStep();
		String nextWizardStep = event.getNewStep();
		if(!isClickPrevious(currentWizardStep, nextWizardStep)){
			String err = validateBeforeNext(currentWizardStep);	

			if(currentWizardStep.equals(STEP_APPLICANT) && err.length() == 0){
				initNewRetention();
			}
			if(currentWizardStep.endsWith(STEP_PRODUCT) && err.length() == 0)
				createInvoiceFile();

			return testNextStep(currentWizardStep, nextWizardStep, err);
		}else{ //previous no check!
			return testNextStep(nextWizardStep, nextWizardStep,"");
		}
	}

	/**
	 * Button Previous has been clicked
	 * @param curWizStep
	 * @param nextWizStep
	 * @return
	 */
	private boolean isClickPrevious(String curWizStep, String nextWizStep){
		int cur = getMapnumtabs().get(curWizStep);
		int next = getMapnumtabs().get(nextWizStep);
		if(cur > next){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * validate requared value before click Next or Save
	 * @param step
	 * @return
	 */
	public String validateBeforeNext(String step){
		String err = "";		
		if(step.equals(STEP_APPLICANT)){
			err = getSelectApplicantMBean().validateApplicant();
		}else if(step.equals(STEP_PRODUCT)){
			err = validateYear();
		}else if(step.equals(STEP_PAYMENT)){
			err = validatePayment();
		}
		return err;
	}

	/**	 
	 * @return true - if year selected, else - false
	 */
	public String validateYear(){
		if (!"".equals(getCurYear())){
			return "";
		}else{
			return "selectyear";
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
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("retentionfrm:messages");
	}
	/**
	 * Test and determine the next step
	 * In addition, set buttons visibility
	 * @param currentWizardStep
	 * @param nextWizardStep
	 * @param err
	 * @return next valid step name
	 */
	private String testNextStep(String currentWizardStep, String nextWizardStep, String err) {
		String nextStep = nextWizardStep;
		if(err.length()!=0){
			informUser(err, FacesMessage.SEVERITY_ERROR);
			nextStep=currentWizardStep;
		}else{
			save(currentWizardStep);
			FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("retentionfrm:messages");
		}
		return nextStep;
	}
	/**----*/
	public String getTab1Id(){
		return STEP_APPLICANT;
	}

	public String getTab2Id(){
		return STEP_PRODUCT;
	}

	public String getTab3Id(){
		return STEP_PAYMENT;
	}

	public static Map<String, Integer> getMapnumtabs() {
		return mapNumTabs;
	}
	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public SelectApplicantMBean getSelectApplicantMBean() {
		return selectApplicantMBean;
	}

	public void setSelectApplicantMBean(SelectApplicantMBean selectApplicantMBean) {
		this.selectApplicantMBean = selectApplicantMBean;
	}

	public Flash getFlash() {
		return flash;
	}

	public void setFlash(Flash flash) {
		this.flash = flash;
	}

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public List<ProdTable> getCurlist() {
		return curlist;
	}

	public void setCurlist(List<ProdTable> curlist) {
		this.curlist = curlist;
	}

	public ProdCategory getLastClicked() {
		return lastClicked;
	}

	public List<ProdTable> getFilteredProducts() {
		return filteredProducts;
	}

	public void setFilteredProducts(List<ProdTable> filteredProducts) {
		this.filteredProducts = filteredProducts;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	/**
	 * Applicant change listener
	 * @param event
	 */
	public void appChangeListenener(AjaxBehaviorEvent event) {
		updateCurList();
	}

	/**
	 * show cur year
	 */
	public List<String> completeYearList(String query) {
		try {
			List<String> y = getYears();					
			return JsfUtils.completeSuggestions(query, y);
		} catch (Exception ex){
			ex.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ex.getMessage()));
		}
		return null;
	}
	/**
	 * @param aplicant_id - Applicant id
	 * @param category - HUMAN
	 * @return size list of all REGISTERED product this applicant which have select Product Category
	 */
	public int findSizeHumanProductByApplicant(){
		if(getSelectApplicantMBean().getApplicant()!=null && 
				getSelectApplicantMBean().getApplicant().getApplcntId()!=null && getSelectApplicantMBean().getApplicant().getApplcntId()>0){
			return	getProductService().findSizeProductByTypeApplicant(getSelectApplicantMBean().getApplicant().getApplcntId(), ProdCategory.HUMAN, getCurYear());
		}
		return 0;
	}
	/**
	 * @param aplicant_id - Applicant id
	 * @param category - VETERINARY
	 * @return size list of all REGISTERED product this applicant which have select Product Category
	 */
	public int findSizeVeterenaryProductByApplicant(){
		if(getSelectApplicantMBean().getApplicant()!=null && 
				getSelectApplicantMBean().getApplicant().getApplcntId()!=null && getSelectApplicantMBean().getApplicant().getApplcntId()>0){
			return	getProductService().findSizeProductByTypeApplicant(getSelectApplicantMBean().getApplicant().getApplcntId(), ProdCategory.VETERINARY, getCurYear());
		}
		return 0;
	}

	/**
	 * @param aplicant_id - Applicant id
	 * @param category - HUMAN
	 * @return all REGISTERED product this applicant which have select Product Category
	 */
	public  List<ProdTable> findHumanProductByApplicant(){
		List<ProdTable> prodTables = new ArrayList<ProdTable>();
		if(getSelectApplicantMBean().getApplicant()!=null && 
				getSelectApplicantMBean().getApplicant().getApplcntId()!=null && getSelectApplicantMBean().getApplicant().getApplcntId()>0){
			prodTables = getProductService().findProdTableByApplicants(getSelectApplicantMBean().getApplicant().getApplcntId(), ProdCategory.HUMAN, getCurYear());
			setFilteredProducts(prodTables);
			lastClicked = ProdCategory.HUMAN;
		}		
		return prodTables;
	}
	/**
	 * @param aplicant_id - Applicant id
	 * @param category - VETERINARY
	 * @return all REGISTERED product this applicant which have select Product Category
	 */
	public  List<ProdTable> findVeterenaryProductByApplicant(){
		List<ProdTable> prodTables = new ArrayList<ProdTable>();
		if(getSelectApplicantMBean().getApplicant()!=null && 
				getSelectApplicantMBean().getApplicant().getApplcntId()!=null && getSelectApplicantMBean().getApplicant().getApplcntId()>0){
			prodTables = getProductService().findProdTableByApplicants(getSelectApplicantMBean().getApplicant().getApplcntId(), ProdCategory.VETERINARY, getCurYear());
			setFilteredProducts(prodTables);
			lastClicked = ProdCategory.VETERINARY;
		}		
		return prodTables;
	}

	public void updateCurList(){
		switch (lastClicked) {
		case HUMAN:
			curlist = findHumanProductByApplicant();
			break;
		case VETERINARY:
			curlist = findVeterenaryProductByApplicant();
			break;
		default:
			break;
		}
	}

	/**
	 * Save current Invoice. If success - Success message, else - Error message.
	 */
	public void save(String step){
		String err = validateBeforeNext(step);
		if(err.length() == 0){						
			getInvoice().setState(InvoiceState.SAVED);				
			err = silentSave();
			if(err.length() == 0)
				informUser("global.saved", FacesMessage.SEVERITY_INFO);
			else
				informUser(err, FacesMessage.SEVERITY_ERROR);		
		}else{
			informUser(err, FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Save current Invoice. State will be "SAVED".
	 */
	public String silentSave(){		
		getInvoice().setInvoiceNumber(getInvoicenumber());
		getInvoice().setUpdatedBy(getUserSession().getLoggedInUserObj());
		getInvoice().setUpdatedDate(new Date());		
		getInvoice().setCurYear(getCurYear());

		if(getSelectApplicantMBean().getApplicant()!=null)
			getInvoice().setApplicant(getSelectApplicantMBean().getApplicant());

		Invoice inv = saveAndFlush(getInvoice());
		if(inv != null){
			findInvoice(inv.getId());
			return "";
		}else{
			return "global_fail";
		}
	}

	private Invoice saveAndFlush(Invoice invoice){
		return getInvoiceService().getInvoiceDAO().saveAndFlush(invoice);
	}

	/**
	 * Cancel current amendment
	 */
	public String cancel(){
		return getReturnPath();
	}

	/**
	 * Submit current Invoice. State will be "SUBMITTED". Simply delegate this to an invoice specific bean
	 */
	public String submit(){
		getInvoice().setState(InvoiceState.SUBMITTED);	
		getInvoice().setIssueDate(new Date());
		String err = silentSave();		

		if(err.length() == 0){		
			informUser("global.success", FacesMessage.SEVERITY_INFO);
		}else
			informUser(err, FacesMessage.SEVERITY_ERROR);

		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
		return getReturnPath();
	}

	/**
	 * create invoice calculate sum
	 * add set template to Invoice into the field invoiceFile
	 */
	public void createInvoiceFile() {
		String name = "Retention"+".docx";
		try {
			StreamedContent streamedContent = getTemplMBean().getReportInvoice(name, TemplateType.RETENTION, getInvoice());
			getInvoice().setInvoiceFile(IOUtils.toByteArray(streamedContent.getStream()));
			saveAndFlush(getInvoice());			
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}


	/**
	 * Get payment from current Invoice
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

	/**
	 * @param payment - payment from current Invoice
	 */
	public void setPayment(FeePayment payment) {
		this.payment = payment;
	}

	/**
	 * 
	 * @return true-if in Payment tab all fields is filled out, else - false
	 */
	public boolean visibleSubmit(){
		boolean flag  = false;
		if("".equals(validatePayment())){
			flag  = true;
		}
		return flag;
	}

	/**
	 * @return  - select year
	 */
	public String getCurYear() {
		return getInvoice().getCurYear();
	}

	/**
	 * @param curYear - select year
	 */
	public void setCurYear(String curYear) {
		getInvoice().setCurYear(curYear);
	}

	/**
	 * @return  - list cur year, next after cur year
	 */
	public List<String> getYears() {
		return years;
	}

	/**
	 * @param years - list cur year, next after cur year
	 */
	public void setYears(List<String> years) {
		this.years = years;
	}

	public TemplMBean getTemplMBean() {
		return templMBean;
	}

	public void setTemplMBean(TemplMBean templMBean) {
		this.templMBean = templMBean;
	}

	/**
	 * Download a template
	 */
	public StreamedContent getFile() {

		String name = "Retention"+".docx";
		InputStream ist = new ByteArrayInputStream(getInvoice().getInvoiceFile());		
		StreamedContent download = new DefaultStreamedContent(ist, "docx", name);
		return download;
	}

	public String getReturnPath() {
		return returnPath;
	}
	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}
	/**
	 * Set submit/cancel path to the flash storage
	 * @param path
	 */
	public void setReturnPathToFlash(String path){
		getFlash().put(RETURN_TO, path);		
	}

	public static String getRetentionId() {
		return RETENTION_ID;
	}

	/**
	 * Store invoice's id to the flash storage
	 * @param amdId
	 */
	public void setRetentionIdToFlash(Long retId){
		getFlash().put(RETENTION_ID, retId);
	}


	/**
	 * Who can initiate invoices?
	 * @return
	 */
	public boolean canInitInvoice(){
		return getUserSession().isCompany() || getUserSession().isStaff();
	}

	/**
	 * Saved invoices are invoices that initiated, however is not submitted yet<br>
	 * Selection criteria depends on current user<br>
	 * For logic see the service
	 * @return list of DTO objects or empty list
	 */
	public List<InvoiceListDTO> getSavedInvoices(){
		List<InvoiceState> state = new ArrayList<InvoiceState>();
		state.add(InvoiceState.SAVED);
		List<InvoiceListDTO> invs = getInvoiceService().fetchInvoicesByState(getUserSession().getLoggedInUserObj(),state, userSession);
		return invs;
	}
	/**
	 * @return all invoices - InvoiceState = SUBMITTED
	 */
	public List<InvoiceListDTO> getSubmittedInvoices(){
		List<InvoiceState> state = new ArrayList<InvoiceState>();
		state.add(InvoiceState.SUBMITTED);
		List<InvoiceListDTO> invs = getInvoiceService().fetchInvoicesByState(getUserSession().getLoggedInUserObj(),state, userSession);
		return invs;
	}
	/**
	 * @return all invoices - InvoiceState = APPROVED || SUBMITTED, curUserRole = screner
	 */
	public List<InvoiceListDTO> getSubmittedApprovedInvoices(){
		List<InvoiceState> state = new ArrayList<InvoiceState>();
		state.add(InvoiceState.SUBMITTED);
		state.add(InvoiceState.APPROVED);
		List<InvoiceListDTO> invs = getInvoiceService().fetchInvoicesByState(getUserSession().getLoggedInUserObj(), state, userSession);
		return invs;
	}
	/**
	 * @return all invoices - InvoiceState = APPROVED, curUserRole = receiver
	 */
	public List<InvoiceListDTO> getApprovedInvoices(){
		List<InvoiceState> state = new ArrayList<InvoiceState>();
		state.add(InvoiceState.APPROVED);
		List<InvoiceListDTO> invs = getInvoiceService().fetchInvoicesByState(getUserSession().getLoggedInUserObj(), state, userSession);
		return invs;
	}
	/**
	 * @return all invoices - InvoiceState = REJECTED
	 */
	public List<InvoiceListDTO> getRejectedInvoices(){
		List<InvoiceState> state = new ArrayList<InvoiceState>();
		state.add(InvoiceState.REJECTED);
		List<InvoiceListDTO> invs = getInvoiceService().fetchInvoicesByState(getUserSession().getLoggedInUserObj(), state, userSession);
		return invs;
	}




}
