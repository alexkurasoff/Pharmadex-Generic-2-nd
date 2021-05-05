package org.msh.pharmadex.mbean.amendment;

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

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.FeePayment;
import org.msh.pharmadex.domain.ProdAppChecklist;
import org.msh.pharmadex.domain.ProdCompany;
import org.msh.pharmadex.domain.ProdExcipient;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.amendment.AmdConditions;
import org.msh.pharmadex.domain.amendment.AmdDocuments;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.AmendmentDictionary;
import org.msh.pharmadex.domain.enums.AmendmentProcess;
import org.msh.pharmadex.domain.enums.CompanyType;
import org.msh.pharmadex.domain.enums.PharmadexDB;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.msh.pharmadex.domain.enums.YesNoNA;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentDictionaryService;
import org.msh.pharmadex.service.amendment.AmendmentService;
import org.msh.pharmadex.util.JsfUtils;
import org.primefaces.event.FlowEvent;

/**
 * Serve amendment initiation process. Common logic for all types of amendments
 * Can initialize new amendment or take one from the flash
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdreghomeMBean {

	private static final String RETURN_TO = "return_to";
	private static final String AMENDMENT_ID = "amendmentId";
	private static final String AMENDMENT_PHARM = "pharm";
	public static final String STEP_TYPE = "type";
	public static final String STEP_APPLICANT = "applTab";
	public static final String STEP_PRODUCT = "prodTab";
	public static final String STEP_AMDDOC = "amddoc";
	public static final String STEP_DETAIL = "detail";
	public static final String STEP_PAYMENT = "payment";
	public static final String STEP_SUMMARY = "summary";

	public static int maxLengthComment = 500;

	@ManagedProperty(value="#{userSession}")
	UserSession userSession;

	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty(value="#{prodApplicationsService}")
	private ProdApplicationsService prodApplicationsService;

	@ManagedProperty(value="#{productService}")
	ProductService productService;

	@ManagedProperty(value="#{amendmentDictionaryService}")
	private AmendmentDictionaryService amendmentDictionaryService;

	@ManagedProperty(value="#{editAmdDictionaryMBean}")
	private EditAmdDictionaryMBean editAmdDictionaryMBean;

	@ManagedProperty("#{flash}")
	private Flash flash;

	/**
	 * What to do in case cancel and/or submit
	 */
	private String returnPath="/home.faces";
	private Long prodAppId = null;

	/**
	 * Current amendment. common part
	 */
	private IAmendment amendmentMbean;
	private Applicant selectedApplicant = null;
	private PharmadexDB pharmadexDB = null;

	private boolean visibleSubmit = false;
	private boolean visibleProductTab = false;

	@SuppressWarnings("serial")
	private static final Map<String, Integer> mapNumTabs = new HashMap<String, Integer>(){
		{
			put(STEP_TYPE, 1);
			put(STEP_APPLICANT, 2);
			put(STEP_PRODUCT, 3);
			put(STEP_AMDDOC, 4);
			put(STEP_DETAIL, 5);
			put(STEP_PAYMENT, 6);
			put(STEP_SUMMARY, 7);
		}
	};

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public String getTab1Id(){
		return STEP_TYPE;
	}

	public String getTab2Id(){
		return STEP_APPLICANT;
	}

	public String getTab3Id(){
		return STEP_PRODUCT;
	}

	public String getTab4Id(){
		return STEP_AMDDOC;
	}

	public String getTab5Id(){
		return STEP_DETAIL;
	}

	public String getTab6Id(){
		return STEP_PAYMENT;
	}

	public String getTab7Id(){
		return STEP_SUMMARY;
	}

	public static Map<String, Integer> getMapnumtabs() {
		return mapNumTabs;
	}

	public PharmadexDB getPharmadexDB() {
		return pharmadexDB;
	}

	/**
	 * Set amendment type and init a new Amendment in a corresponding MBean
	 * @param PharmadexDB
	 */
	public void setPharmadexDB(PharmadexDB pharmDB) {
		PharmadexDB oldValue = getPharmadexDB();
		this.pharmadexDB = pharmDB;
		if(oldValue != getPharmadexDB()){
			initNewAmd();
		}
	}

	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService service) {
		this.amendmentService = service;
	}

	public AmendmentDictionaryService getAmendmentDictionaryService() {
		return amendmentDictionaryService;
	}

	public void setAmendmentDictionaryService(AmendmentDictionaryService amendmentDictionaryService) {
		this.amendmentDictionaryService = amendmentDictionaryService;
	}

	public EditAmdDictionaryMBean getEditAmdDictionaryMBean() {
		return editAmdDictionaryMBean;
	}

	public void setEditAmdDictionaryMBean(EditAmdDictionaryMBean editAmdDictionaryMBean) {
		this.editAmdDictionaryMBean = editAmdDictionaryMBean;
	}

	public Flash getFlash() {
		return flash;
	}

	public void setFlash(Flash flash) {
		this.flash = flash;
	}

	public String getReturnPath() {
		return returnPath;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}

	public IAmendment getAmendmentMbean() {
		return amendmentMbean;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setAmendmentMbean(IAmendment amendmentMbean) {
		this.amendmentMbean = amendmentMbean;
	}

	public ProdApplicationsService getProdApplicationsService() {
		return prodApplicationsService;
	}

	public void setProdApplicationsService(ProdApplicationsService prodApplicationsService) {
		this.prodApplicationsService = prodApplicationsService;
	}

	public boolean isVisibleSubmit() {
		return visibleSubmit;
	}

	public void setVisibleSubmit(boolean visibleSubmit) {
		this.visibleSubmit = visibleSubmit;
	}

	public boolean isSaveAmendment(){
		boolean isSave = false;
		if(getAmendmentMbean() != null && getAmendmentMbean().getAmendment() != null && 
				getAmendmentMbean().getAmendment().getState() != null && 
				getAmendmentMbean().getAmendment().getState().equals(AmdState.SAVED))
			isSave = true;
		return isSave;
	}
	/**
	 * ProductTab show product data by PharmadexDB != PharmadexDB.APPLICAN
	 * @return
	 */
	public boolean isVisibleProductTab(){
		visibleProductTab = false;
		if(getPharmadexDB() != null && !getPharmadexDB().equals(PharmadexDB.APPLICANT)){
			visibleProductTab = true;
		}
		return visibleProductTab;
	}

	public boolean hasApplicant(){
		return getApplicant() != null;
	}

	public AmendmentDictionary getAmendmentDictionary(){
		return getAmendmentMbean().getAmendment().getDictionary();
	}
	/**
	 * Get applicant from amendment
	 * @return
	 */
	public Applicant getApplicant(){
		return getAmendmentMbean().getAmendment().getApplicant();
	}
	/**
	 * Set applicant to amendment
	 * @param applicant
	 */
	public void setApplicant(Applicant applicant){
		getAmendmentService().storeApplicantToAmd(getAmendmentMbean().getAmendment(), applicant);
	}

	public User getApplicantUser(){
		setApplicant(getAmendmentService().findApplicant(getApplicant().getApplcntId()));
		return getAmendmentService().fetchResponsibleUser(getApplicant());
	}

	public Applicant getSelectedApplicant() {
		return selectedApplicant;
	}

	public void setSelectedApplicant(Applicant selApp) {
		if(selApp != null && selApp.getApplcntId() > 0)
			this.selectedApplicant = getAmendmentService().findApplicant(selApp.getApplcntId());
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

		selectedApplicant = null;
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
			if(currentWizardStep.equals(STEP_TYPE) && err.length() == 0)
				updatePharmadexDB();

			return testNextStep(currentWizardStep, nextWizardStep, err);
		}else{ //previous no check!
			return testNextStep(nextWizardStep, nextWizardStep,"");
		}
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
			// excess!!! FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:messages");
			nextStep=currentWizardStep;
		}else
			FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:messages");
		prepareButtons(nextStep);
		return nextStep;
	}

	private void updatePharmadexDB(){
		AmendmentDictionary selectDic = getEditAmdDictionaryMBean().getSelectedItem();
		if(selectDic != null){
			PharmadexDB pharm = selectDic.getPharmadexDB();
			setPharmadexDB(pharm);
		}
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
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:messages");
	}
	/**
	 * Prepare Save and Submit buttons visibility
	 * @param nextStep real next step
	 */
	private void prepareButtons(String nextStep) {
		setVisibleSubmit(nextStep.equals(STEP_SUMMARY));
	}

	/**
	 * Try to get amendment and initialize MBean for a corresponding amendment type. Silent!
	 */
	@PostConstruct
	public void initOldAmendment(){
		//old amendment
		this.pharmadexDB = (PharmadexDB) getFlash().get(AMENDMENT_PHARM);
		if(pharmadexDB != null){
			Long amdId = (Long) getFlash().get(AMENDMENT_ID);
			if(amdId != null){
				setRightAmdMBean(amdId);

				getEditAmdDictionaryMBean().setSelectedItem(getAmendmentMbean().getAmendment().getDictionary());
				getEditAmdDictionaryMBean().setListAmdConditions(getAmendmentMbean().getAmendment().getConditions());
			}
		}else
			setReturnPath("/secure/amendment/amdsavedlist.faces");
		//return path
		String path = (String) getFlash().get(RETURN_TO);
		if(path != null){
			setReturnPath(path);
		}
	}

	/**
	 * initialize an amendment
	 */
	private void initNewAmd() {
		cleanAmdMBeans();

		setRightAmdMBean(null);

		getAmendmentMbean().getAmendment().setDictionary(getEditAmdDictionaryMBean().getSelectedItem());
		
		getAmendmentMbean().getAmendment().setConditions(getEditAmdDictionaryMBean().getListAmdConditions());
		if(getEditAmdDictionaryMBean().getListAmdConditions() != null){
			for(AmdConditions amdC:getEditAmdDictionaryMBean().getListAmdConditions()){
				amdC.setAmendment(getAmendmentMbean().getAmendment());
			}
		}

		List<AmdDocuments> listAmdDocs = getAmendmentDictionaryService().fetchAmdDocumentByDictionary(getEditAmdDictionaryMBean().getSelectedItem(), getAmendmentMbean().getAmendment());
		getAmendmentMbean().getAmendment().setDocuments(listAmdDocs);
		
		List<ProdAppChecklist> listChecklist = getAmendmentService().fetchAmdProdAppChecklist(getAmendmentMbean().getAmendment(), getUserSession().getLoggedInUserObj());
		getAmendmentMbean().getAmendment().setProdAppChecklist(listChecklist);
		
		//add payment for any
		getAmendmentMbean().getAmendment().setPayment(new FeePayment());
		//add created date and creator for any
		getAmendmentMbean().getAmendment().setCreatedDate(new Date());
		getAmendmentMbean().getAmendment().setCreatedBy(getUserSession().getLoggedInUserObj());

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

	private void cleanAmdMBeans() {
		//getAmendmentMbean().setAmendment(null);
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
	 * Get name of detail tab template
	 * @return
	 */
	public String getDetailTemplate(){
		if(getPharmadexDB() != null)
			return getPharmadexDB().getDetaileTemplate();
		return "/templates/amendment/badamd.xhtml";
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
	 * Get name of Dictioanary Docs or CheckList tab template
	 * @return
	 */
	public String getTemplateDocsTab(){
		if(isProcessNEWAPPL())
			return "/templates/prodappchecklist.xhtml";
		return "/templates/amendment/amdDocsList.xhtml";
	}
	
	/**
	 * Get edit ot show template by choose dictionary type
	 * @return
	 */
	public String getTemplateTypeTab(){
		if(isSaveAmendment())
			return "/templates/amendment/amdDictionary.xhtml";
		
		return "/templates/amendment/fullListDictionary.xhtml";
	}

	public String getDocsTabHeader(){
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
		if(isProcessNEWAPPL())
			return bundle.getString("checklist");
		return bundle.getString("selectamddocs");
	}
	public boolean isProcessNEWAPPL(){
		if(getAmendmentMbean() != null && getAmendmentMbean().getAmendment() != null
				&& getAmendmentMbean().getAmendment().getDictionary() != null
				&& getAmendmentMbean().getAmendment().getDictionary().getProcess() != null
				&& getAmendmentMbean().getAmendment().getDictionary().getProcess().equals(AmendmentProcess.NEW_APPLICATION))
			return true;
		return false;
	}

	public List<ProdAppChecklist> getAmdProdAppChecklists() {
		if(isProcessNEWAPPL()){
			return getAmendmentMbean().getAmendment().getProdAppChecklist();
			
		}
		return new ArrayList<ProdAppChecklist>();
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

	/**
	 * Save current Amendment. State will be "SAVED". Simply delegate this to an amendment specific bean
	 */
	public void save(String step){
		//TODO timeline
		String err = validateBeforeNext(step);
		if(err.length() == 0){
			getAmendmentMbean().getAmendment().setState(AmdState.SAVED);
			getAmendmentMbean().getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
			getAmendmentMbean().getAmendment().setUpdatedDate(new Date());

			err = getAmendmentMbean().save(step);
			if(err.length() == 0)
				informUser("global.success", FacesMessage.SEVERITY_INFO);
			else
				informUser(err, FacesMessage.SEVERITY_ERROR);
		}else{
			informUser(err, FacesMessage.SEVERITY_ERROR);
		}
	}

	/**
	 * Cancel current amendment
	 */
	public String cancel(){
		return getReturnPath();
	}

	/**
	 * Submit current Amendment. State will be "SUBMITTED". Simply delegate this to an amendment specific bean
	 */
	public String submit(){
		//TODO timeline
		getAmendmentMbean().getAmendment().setState(AmdState.SUBMITTED);
		getAmendmentMbean().getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
		getAmendmentMbean().getAmendment().setUpdatedDate(new Date());
		String err = getAmendmentMbean().submit();
		if(err.length() == 0){
			if(isProcessNEWAPPL()){
				prodAppId = getAmendmentService().createNewApplication(getAmendmentMbean().getAmendment(), getUserSession().getLoggedInUserObj());
				getFlash().put("prodAppID", prodAppId);
				informUser("global.success", FacesMessage.SEVERITY_INFO);
				return "/internal/processreg.faces";
			}else
				informUser("global.success", FacesMessage.SEVERITY_INFO);
		}else
			informUser(err, FacesMessage.SEVERITY_ERROR);

		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
		return getReturnPath();
	}
	/**
	 * Who can initiate amendments?
	 * @return
	 */
	public boolean canInitAmendment(){
		return getUserSession().isCompany() || getUserSession().isStaff();
	}

	/**
	 * Who can see submitted amendment list
	 * @return
	 */
	public boolean canSeeSubmittedAmendments(){
		return canInitAmendment() || getUserSession().isReviewer() || getUserSession().isHead();
	}

	/**
	 * Saved amendments are amendments that initiated, however is not submitted yet<br>
	 * Selection criteria depends on current user<br>
	 * For logic see the service
	 * @return list of DTO objects or empty list
	 */
	public List<AmdListDTO> getSavedAmendments(){
		List<AmdListDTO> amds = getAmendmentService().fetchAmendmentsByState(getUserSession().getLoggedInUserObj(), AmdState.SAVED, userSession);
		return amds;
	}

	public Long getProdAppId(){
		return prodAppId;
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

	/**
	 * validate requared value before click Next or Save
	 * @param step
	 * @return
	 */
	public String validateBeforeNext(String step){
		String err = "";
		if(step.equals(STEP_TYPE)){
			err = validateTabType();
		}

		if(step.equals(STEP_APPLICANT)){
			err = getAmendmentMbean().validateApplicant();
		}
		if(step.equals(STEP_PRODUCT)){
			err = getAmendmentMbean().validateProduct();
		}
		if(step.equals(STEP_AMDDOC)){
			err = validateTabDocs();
		}

		if(step.equals(STEP_DETAIL)){
			if(getAmendmentMbean().getAmendment().getAppComment() != null && 
					getAmendmentMbean().getAmendment().getAppComment().trim().length() > 5){
				if(getAmendmentMbean().getAmendment().getAppComment().length() > maxLengthComment)
					err = "Error.maxLength";
				else
					err = getAmendmentMbean().validateDetail();
			}else
				err = "reasonrequired";
		}
		return err;
	}

	private String validateTabType(){
		if(isSaveAmendment())
			return "";
		
		if(getEditAmdDictionaryMBean().getSelectedItem() != null){
			if(getEditAmdDictionaryMBean().getListAmdConditions() != null){
				int index = 0;
				for(AmdConditions it:getEditAmdDictionaryMBean().getListAmdConditions()){
					if(it.getAnswer() != null && it.getAnswer().getValue() != null
							&& it.getAnswer().getValue().equals(YesNoNA.YES))
						index++;
				}

				if(index != getEditAmdDictionaryMBean().getListAmdConditions().size())
					return "amdcondition_incomplete";
			}
		}else
			return "selectDictionaryreq";
		return "";
	}

	/**
	 * validate Documents tab
	 * answer YES or NA in list dictionary documents
	 * @return
	 */
	private String validateTabDocs(){
		if(isSaveAmendment() || isProcessNEWAPPL())
			return "";
		
		if(getEditAmdDictionaryMBean().getSelectedItem() != null){
			if(getListAmdDocs() != null){
				int index = 0;
				for(AmdDocuments it:getListAmdDocs()){
					if(it.getAnswer() != null && it.getAnswer().getValue() != null
							&& (it.getAnswer().getValue().equals(YesNoNA.YES) || it.getAnswer().getValue().equals(YesNoNA.NA)))
						index++;
				}

				if(index != getListAmdDocs().size())
					return "amddocs_incomplete";
			}
		}else
			return "selectDictionaryreq";

		return "";
	}

	public List<AmdDocuments> getListAmdDocs() {
		if(getAmendmentMbean().getAmendment() != null){
			return getAmendmentMbean().getAmendment().getDocuments();
		}else{
			return new ArrayList<AmdDocuments>();
		}
	}

	public Long getAmdID(){
		if(getAmendmentMbean() != null && getAmendmentMbean().getAmendment() != null)
			return getAmendmentMbean().getAmendment().getId();
		return null;
	}

	public void onAnswerChangeListenener() {
		//ValueChangeEvent event
	}
	/**
	 * Set product to amendment
	 * @param applicant
	 */
	public void setProduct(Product product){		
		getAmendmentService().storeProductToAmd(getAmendmentMbean().getAmendment(), product);
	}

	public Product getProduct(){
		return getAmendmentMbean().getAmendment().getProduct();
	}
	
	/**
	 * show products available to this user
	 * 
	 */
	public List<Product> completeProductList(String query) {
		try {
			List<Product> products = getAmendmentService().fetchProduct(getAmendmentMbean().getAmendment());
			return JsfUtils.completeSuggestions(query, products);			
		} catch (Exception ex){
			ex.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", ex.getMessage()));
		}
		return null;
	}

	public String getProdAppTypeKey(){
		if(getProduct() != null){
			ProdAppType t = getProductService().getProdAppTypeByProduct(getProduct().getId());
			return t.getKey();
		}
		return null;
	}

	public List<ProdInn> getInns(){
		if(getProduct()!=null){
			Product prod = getProductService().findProduct(getProduct().getId());	
			if(prod!=null){
				return prod.getInns();				
			}			
		}
		return null;
	}
	
	public List<ProdExcipient> getExcipients(){
		if(getProduct()!=null){
			Product prod = getProductService().findProduct(getProduct().getId());	
			if(prod!=null){
				return prod.getExcipients();				
			}			
		}
		return null;
	}
	
	/**
	 * Get full list of product manufacturers (Inns included)
	 * @return
	 */
	public List<ProdCompany> getProdCompanies() {
		List<ProdCompany> prodCompanies = new ArrayList<ProdCompany>();

		if(getProduct()!=null){
			Product prod = getProductService().findProduct(getProduct().getId());			
			if(prod != null){
				List<ProdCompany> comps = prod.getProdCompanies();
				List<ProdInn> inns = prod.getInns();

				if(comps != null && comps.size() > 0)
					prodCompanies.addAll(comps);

				if(inns != null && inns.size() > 0){
					for(ProdInn inn:inns){
						if(inn.getCompany() != null){
							ProdCompany pcom = new ProdCompany(prod, inn.getCompany(), CompanyType.API_MANUF);
							prodCompanies.add(pcom);
						}
					}
				}
			}
		}		
		return prodCompanies;
	}

}
