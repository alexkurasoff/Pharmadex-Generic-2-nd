package org.msh.pharmadex.mbean.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.dao.iface.WorkspaceDAO;
import org.msh.pharmadex.domain.AgentAgreement;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.FileTemplate;
import org.msh.pharmadex.domain.Invoice;
import org.msh.pharmadex.domain.ProdAppChecklist;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdCompany;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.Workspace;
import org.msh.pharmadex.domain.enums.CompanyType;
import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.TemplateType;
import org.msh.pharmadex.domain.enums.YesNoNA;
import org.msh.pharmadex.service.ApplicantService;
import org.msh.pharmadex.service.InvoiceService;
import org.msh.pharmadex.service.ProdAppChecklistService;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.TemplService;
import org.msh.pharmadex.service.UserAccessService;
import org.msh.pharmadex.service.UserService;
import org.msh.pharmadex.service.UtilsByReports;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class serves Detail Template as MS Word document
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class TemplMBean {

	@ManagedProperty(value = "#{userAccessService}")
	private UserAccessService userAccessService;

	@ManagedProperty(value = "#{templService}")
	private TemplService templService;

	@ManagedProperty(value= "#{prodApplicationsService}")
	private ProdApplicationsService prodApplicationsService;

	@ManagedProperty(value = "#{userService}")
	private UserService userService;

	@ManagedProperty(value = "#{productService}")
	private ProductService productService;

	@ManagedProperty(value = "#{prodAppChecklistService}")
	ProdAppChecklistService prodAppChecklistService;

	@Autowired
	private UtilsByReports utilsByReports;

	@ManagedProperty(value = "#{applicantService}")
	public ApplicantService applicantService;

	@ManagedProperty(value = "#{userSession}")
	public UserSession userSession;

	@ManagedProperty(value = "#{invoiceService}")
	private InvoiceService invoiceService;

	@ManagedProperty(value = "#{workspaceDAO}")
	WorkspaceDAO workspaceDAO;

	private ProdApplications prodApplications=null;
	private TemplateType templateType ;
	private FileTemplate fileTemplate = null;
	private Date dueDate = null;
	private Map<String, String> key =null;
	private Map<String, String> checkListItem = null;
	private Map<String, String> checkListValue = null; 	
	private boolean isApproved = false;
	private User screenedUser = null;
	private String execSummary = "";
	private String fileName = null;
	private String reasonRejection = null;
	private boolean staffOnly = false;
	/**
	 * invoice field
	 */
	private String year = "";
	/*private String yearHuman = "";
	private String yearVeterinary ="";*/
	private String sizeHuman = "";
	private String sizeVeterinary ="";
	private String costHuman = "";
	private String costVeterinary = "";
	private String amountHuman = "";
	private String amountVeterinary ="";
	private Applicant invoiceApplicant= null;
	private String invoceIssueDate = "";
	private String invoiceNumber ="";
	private String amountAll = "";
	/**
	 * This is a full list of file template regardless of type
	 */
	List<FileTemplate> fullList = new ArrayList<FileTemplate>();

	public UserAccessService getUserAccessService() {
		return userAccessService;
	}

	public void setUserAccessService(UserAccessService userAccessService) {
		this.userAccessService = userAccessService;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public TemplService getTemplService() {
		return templService;
	}

	public void setTemplService(TemplService templService) {
		this.templService = templService;
	}

	public boolean isStaffOnly() {
		/*if(getFileTemplate() != null && getFileTemplate().getStaffOnly() != null)
			staffOnly = getFileTemplate().getStaffOnly();*/
		return staffOnly;
	}

	public void setStaffOnly(boolean staffOnly) {
		this.staffOnly = staffOnly;
	}

	/**
	 * by edit Template - save on change value
	 * by New Template - nothing
	 */
	public void staffOnlyChangeListenener(){
		if(getFileTemplate() != null){
			getFileTemplate().setStaffOnly(staffOnly);
			fileTemplate = getTemplService().save(fileTemplate);	
		}
	}
	/**
	 * Upload a template
	 * by New Template - save value staffOnly on Upload Click
	 * @param event
	 */
	public void handleFileUpload(FileUploadEvent event) {
		//templateType = (TemplateType) getFlash().get("templateType");
		FileTemplate fileTemplate = new FileTemplate ();//getUserAccessService().getWorkspace();
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle resourceBundle = context.getApplication().getResourceBundle(context, "msgs");
		try {
			byte[] fileStream = IOUtils.toByteArray(event.getFile().getInputstream());
			setProdApplications(null); //test only!
			if (checkTemplate(new ByteArrayInputStream(fileStream))){
				fileTemplate.setTemplateType(templateType);
				fileTemplate.setFile(fileStream);
				fileTemplate.setFileName(event.getFile().getFileName());
				fileTemplate.setContentType(event.getFile().getContentType());
				fileTemplate.setStaffOnly(staffOnly);
				//TODO
				getTemplService().save(fileTemplate);						
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,resourceBundle.getString("global.success"), event.getFile().getFileName() + resourceBundle.getString("upload_success"));
				context.addMessage(null, msg);
				setTemplateType(null); //clean up for next choice
			}else{
				uploadError(event, context, resourceBundle);
			}
		} catch (IOException e) {
			uploadError(event, context, resourceBundle);
		}
	}
	/**
	 * Upload a template
	 * @param event
	 */
	public void handleFileChangeUpload(FileUploadEvent event) {
		fileTemplate = getFileTemplate();
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle resourceBundle = context.getApplication().getResourceBundle(context, "msgs");
		try {
			byte[] fileStream = IOUtils.toByteArray(event.getFile().getInputstream());
			setProdApplications(null); //test only!
			if (checkTemplate(new ByteArrayInputStream(fileStream))){				
				fileTemplate.setFile(fileStream);
				fileTemplate.setFileName(event.getFile().getFileName());
				fileTemplate.setContentType(event.getFile().getContentType());
				getTemplService().save(fileTemplate);						
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,resourceBundle.getString("global.success"), event.getFile().getFileName() + " " +resourceBundle.getString("upload_success"));
				context.addMessage(null, msg);
			}else{
				uploadError(event, context, resourceBundle);
			}
		} catch (IOException e) {
			uploadError(event, context, resourceBundle);
		}
	}

	/**
	 * Download a template
	 * @return
	 */
	public StreamedContent getFile() {	
		fileTemplate = getFileTemplate();
		InputStream ist = new ByteArrayInputStream(fileTemplate.getFile());		
		StreamedContent download = new DefaultStreamedContent(ist, "docx", fileTemplate.getFileName());
		return download;
	}
	/**
	 * Does the template exist
	 * @return
	 */
	public boolean isUploaded(FileTemplate fileTemplate){		
		return fileTemplate.getFileName() != null && fileTemplate.getFileName().length()>5;
	}
	/**
	 * Display upload error
	 * @param event
	 * @param context
	 * @param resourceBundle
	 */
	public void uploadError(FileUploadEvent event, FacesContext context, ResourceBundle resourceBundle) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,resourceBundle.getString("global_fail"), event.getFile().getFileName() + resourceBundle.getString("upload_fail"));
		context.addMessage(null, msg);
	}
	/**
	 * Validate the template
	 * @param template
	 * @return true when check is OK
	 */
	private boolean checkTemplate(InputStream template) {
		FacesContext context = FacesContext.getCurrentInstance();
		int found = 0;
		ResourceBundle resourceBundle = context.getApplication().getResourceBundle(context, "msgs");
		try {
			found = getTemplService().testIt(template);

			if(found>0){
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,resourceBundle.getString("global.success"),
						found + " "+ resourceBundle.getString("elfound"));
				context.addMessage(null, msg);
				return true;
			}else{
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,resourceBundle.getString("global_fail"), resourceBundle.getString("templateVoid"));
				context.addMessage(null, msg);
				return false;
			}
		} catch (ELException e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,resourceBundle.getString("global_fail"), e.getLocalizedMessage());
			context.addMessage(null, msg);
			return false;
		} catch (IOException e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,resourceBundle.getString("global_fail"), e.getLocalizedMessage());
			context.addMessage(null, msg);
			return false;
		}
	}

	public List<FileTemplate> getFullList() {
		List<FileTemplate> listFileTemplate = getTemplService().findAll();
		setFullList(listFileTemplate);
		return listFileTemplate;
	}
	/**
	 * @return All TemplateType
	 */
	public List<TemplateType> getAllTemplateType(){
		List<TemplateType> templateType = new ArrayList<TemplateType>();
		templateType.add(TemplateType.DOSSIER_RECIEPT);
		templateType.add(TemplateType.DEFICIENCY);
		templateType.add(TemplateType.CERTIFICATE);
		templateType.add(TemplateType.REJECTION);
		templateType.add(TemplateType.SCREENING_NOTIFICATION);
		templateType.add(TemplateType.SCREENING_OUTCOME);
		templateType.add(TemplateType.RETENTION);
		return templateType;
	}
	/**
	 * @return not select TemplateType in FileTemplate
	 */
	public List<TemplateType> completeTemplateTypeList(){		
		List<TemplateType> templateType = getAllTemplateType();		
		List<FileTemplate> listFileTemplate = getTemplService().findAll();
		if(listFileTemplate!=null && listFileTemplate.size()>0){
			for(FileTemplate ft : listFileTemplate){				
				if(ft.getTemplateType()!=null){
					templateType.remove(ft.getTemplateType());
				}
			}
		}		
		return templateType;		
	}

	/**
	 * @return Applicant
	 */
	public Applicant getApplicant(){
		if(getProdApplications()!=null && getProdApplications().getApplicant()!=null){
			return getProdApplications().getApplicant();
		}
		return null;
	}

	/**
	 * @return Applicant User Name (Responsible User Name)
	 */
	public String getApplicantUserName(){
		if(getProdApplications()!=null && getProdApplications().getApplicantUser()!=null){
			return getApplicantService().getResponsibleUserName(getApplicant());
		}else{
			return "test Applicant User Name";
		}			
	}
	/**
	 * @return Applicant Name
	 */
	public String getApplicantName(){
		if(getProdApplications() != null &&  getApplicant()!=null){
			return getApplicant().getAppName();

		}else{
			return "test applicant";
		}
	}

	/**
	 * @return Applicant Addr1
	 */
	public String getApplicantAddr1(){
		if(getProdApplications() != null &&  getApplicant()!=null &&  getApplicant().getAddress()!=null){
			String s = getApplicant().getAddress().getAddress1();
			if (s!= null){
				return s;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	/**
	 * @return Applicant Addr2
	 */
	public String getApplicantAddr2(){
		if(getProdApplications() != null &&  getApplicant()!=null &&  getApplicant().getAddress()!=null){
			String s = getApplicant().getAddress().getAddress2();
			if (s!= null){
				return s;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	/**
	 * @return Applicant Country
	 */
	public String getApplicantCountry(){
		if(getProdApplications() != null && getApplicant()!=null && getApplicant().getAddress()!=null
				&& getApplicant().getAddress().getCountry() != null){			
			return getApplicant().getAddress().getCountry().getCountryName();
		}else{
			return "";
		}			
	}


	/**
	 * @return Applicant Country, ZipCode
	 */
	public String getApplicantCountryZipCode(){
		String s = getApplicantCountry();
		if(getProdApplications() != null && getApplicant()!=null && getApplicant().getAddress()!=null){
			if(getApplicant().getAddress().getZipcode()!=null && !"".equals(getApplicant().getAddress().getZipcode())){
				s += ", "+getApplicant().getAddress().getZipcode();
				return s;
			}
		}				
		return "";
	}

	/**
	 * @return Agent Applicant
	 */
	public Applicant getAgentApplicant(){
		Applicant agentApplicant = null;
		if(getProdApplications() != null && getProdApplications().getApplicant()!=null){
			if(getProdApplications().getApplicant().getAgents()!=null){
				for(AgentAgreement agentAgreement:getProdApplications().getApplicant().getAgents()){
					if(agentAgreement!=null){
						if(agentAgreement.isValid()){
							return agentAgreement.getApplicant();
						}
					}
				}
			}
		}
		return agentApplicant;		
	}
	/**
	 * @return Agent Applicant Name (Agent Responsible User Name)
	 */
	public String getAgentApplicantName(){
		if(getProdApplications() != null){
			if(getAgentApplicant()!=null){
				return getApplicantService().getResponsibleUserName(getAgentApplicant());// getAgentApplicant().getAppName();
			}			
		}
		return "";
	}
	/**
	 * @return Agent Applicant Addr1
	 */
	public String getAgentApplicantAddr1(){
		if(getProdApplications() != null &&  getAgentApplicant()!=null && getAgentApplicant().getAddress()!=null 
				&& getAgentApplicant().getAddress().getAddress1()!=null){
			String s = getAgentApplicant().getAddress().getAddress1();
			if (s!= null){
				return s;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	/**
	 * @return Agent Applicant Addr2
	 */
	public String getAgentApplicantAddr2(){
		if(getProdApplications() != null &&  getAgentApplicant()!=null && getAgentApplicant().getAddress()!=null 
				&& getAgentApplicant().getAddress().getAddress2()!=null){
			String s = getAgentApplicant().getAddress().getAddress2();
			if (s!= null){
				return s;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}

	/**
	 * @return Agent Applicant Country, ZipCode
	 */
	public String getAgentApplicantCountryZipCode(){
		if(getProdApplications() != null && getAgentApplicant()!=null && getAgentApplicant().getAddress()!=null
				&& getApplicant().getAddress().getCountry() != null){

			String s = getAgentApplicant().getAddress().getCountry().getCountryName();
			if(getAgentApplicant().getAddress().getZipcode()!=null){
				s += ", "+getAgentApplicant().getAddress().getZipcode();
			}
			if (s!= null){
				return s;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}

	/**
	 * @return Product number screening
	 */
	public String getProdSrcNo(){
		if(getProdApplications() != null){
			return getProdApplications().getProdSrcNo()!=null?getProdApplications().getProdSrcNo():"";
		}else{
			return "test ProdApplications ProdSrcNo";
		}
	}
	/**
	 * @return Product applications number 
	 */
	public String getProdAppNo(){
		if(getProdApplications() != null){
			return getProdApplications().getProdAppNo()!=null?getProdApplications().getProdAppNo():"";
		}else{
			return "test ProdApplications ProdAppNo";
		}
	}

	/**
	 * @return Product reference number
	 */
	public String getProductRefNumber(){
		if(getProdApplications()!=null && getProdApplications().getProdSrcNo()!=null/*&& getProdApplications().getProduct()!=null &&  getProdApplications().getCreatedDate()!=null*/){
			/*Product prod = getProductService().findProduct(getProdApplications().getProduct().getId());
			Date dt =  getProdApplications().getCreatedDate();
			for(ProdApplications pa : prod.getProdApplicationses()){
				if(pa.getCreatedDate().compareTo(dt)>0){
					return pa.getProdSrcNo();
				}
			}*/
			return getProdApplications().getProdSrcNo();			
		}
		return "No product reference number";		
	}


	/**
	 * @return Product
	 */
	public Product getProduct(){
		if( getProdApplications()!=null && getProdApplications().getProduct()!=null ){
			return getProductService().findProduct(getProdApplications().getProduct().getId());
		}else{
			return null;
		}		
	}

	/**
	 * @return Product name
	 */
	public String getProdName(){
		Product prod =  getProduct();
		if(prod !=null){
			return prod.getProdName()!=null?prod.getProdName():"";
		}else{
			return "";
		}
	}
	/**
	 * @return Product name, DosStrength, DosForm 
	 */
	public String getProductPropName(){		
		Product prod =  getProduct();
		if(prod !=null){
			String name =  getProdName();				 
			name +=prod.getDosStrength()!=null?prod.getDosStrength()+", ":"";
			name +=prod.getDosForm()!=null?prod.getDosForm():"";
			return name;
		}					
		return "No product proprietary name";	
	}

	/**
	 * @return Date of letter of application
	 */
	public String getDateLetterApp(){
		return "";
	}
	/**
	 * @return Name of manufacturer
	 */
	public String getManufName(){		
		Product product = getProduct();
		if(product!=null){
			List<ProdCompany> companyList = product.getProdCompanies();
			if (companyList != null){
				for(ProdCompany company:companyList){
					if (company.getCompanyType().equals(CompanyType.FIN_PROD_MANUF)){
						return  getNameFullAddres(company);
					}
				}
			}
		}
		return "No Name of manufacturer";
	}
	/**
	 * @return Name of the final product release controller
	 */
	public String getProdControllerName(){		
		Product product = getProduct();
		if(product!=null){
			List<ProdCompany> companyList = product.getProdCompanies();
			if (companyList != null){
				for(ProdCompany company:companyList){
					if (company.getCompanyType().equals(CompanyType.FPRC)){
						return  getNameFullAddres(company);
					}
				}
			}
		}
		return "No Name of the final product release controller";
	}

	/**
	 * @return Name of the final release responsibility
	 */
	public String getReleaseResponsibilityName(){		
		Product product = getProduct();
		if(product!=null){
			List<ProdCompany> companyList = product.getProdCompanies();
			if (companyList != null){
				for(ProdCompany company:companyList){
					if (company.getCompanyType().equals(CompanyType.FPRR)){
						return  getNameFullAddres(company);
					}
				}
			}
		}
		return "No Name of the final release responsibility";
	}

	/**
	 * @param  ProdCompany company
	 * @return company name, Addr1 company, Addr2 company, country company
	 */
	private String getNameFullAddres(ProdCompany company) {
		String res = "";
		if(company.getCompany()!=null)
			if( company.getCompany().getCompanyName()!=null)
				res +=  company.getCompany().getCompanyName();
		if(company.getCompany().getAddress()!=null){
			if(company.getCompany().getAddress().getAddress1()!=null)
				if(!"".equals(company.getCompany().getAddress().getAddress1()))
					res += ", " +company.getCompany().getAddress().getAddress1() ;
			if(company.getCompany().getAddress().getAddress2()!=null)
				if(!"".equals(company.getCompany().getAddress().getAddress2()))
					res +=", " + company.getCompany().getAddress().getAddress2() ;
			if(company.getCompany().getAddress().getCountry()!=null)
				if(!"".equals(company.getCompany().getAddress().getCountry()))
					res +=", " + company.getCompany().getAddress().getCountry();
		}
		return res;
	}

	/**
	 * @return Date of registration
	 */
	public String getRegistrationDate(){
		if(getProdApplications()!=null){
			if(getProdApplications().getRegistrationDate() != null)
				return  utilsByReports.getDateformat().format(getProdApplications().getRegistrationDate());	
		}
		return "test Date of registration";
	}

	/**
	 * @return Dosage form
	 */
	public String getDosageForm(){
		Product prod = getProduct();
		if(prod!=null){
			return (prod.getDosForm() != null && prod.getDosForm().getDosForm() != null) ? prod.getDosForm().getDosForm():"";
		}
		return "test Dosage form";
	}
	/**
	 * @return Active ingredient
	 */
	public String getActiveIngredient (){		
		if( getProduct()!=null && getProduct().getInns()!=null){
			String activeIngr="" , resActiveIngr="";
			List<ProdInn> prodInn = getProduct().getInns();
			if(prodInn!=null){				
				for(ProdInn el:prodInn){							
					if(el!=null){
						String inn = "", dosStrength="", uom ="";
						if(el.getInn()!=null){
							inn = el.getInn().getName()!=null?el.getInn().getName():"";
						}							
						dosStrength = el.getDosStrength()!=null?el.getDosStrength():"";							
						if(el.getDosUnit()!=null){
							uom = el.getDosUnit().getUom()!=null?el.getDosUnit().getUom():"";
						}				
						activeIngr+=", "+inn+" "+dosStrength+" "+uom;
					}							
				}
				if(activeIngr.length()>0){
					resActiveIngr = activeIngr.replaceFirst(", ", "");
					return resActiveIngr;
				}
			}
		}		
		return "test Active ingredient ";
	}

	/**
	 * @return Registration number
	 */
	public String getRegistrationNumber(){
		if(getProdApplications()!=null && getProdApplications().getProdRegNo() != null){
			return getProdApplications().getProdRegNo() ;
		}
		return "test Registration number";
	}
	/**
	 * @return key map : key - ModuleNo (Checklist), value - ModuleNo (Checklist)
	 */
	public  Map<String, String> getkey(){		
		return key;
	}
	/**
	 * @param key-  ModuleNo (Checklist), value - ModuleNo (Checklist)
	 */
	public void setKey(Map<String, String> key) {
		this.key = key;
	}
	/**
	 * @return  key - ModuleNo (Checklist), value - Name(Checklist),
	 */
	public  Map<String, String> getCheckListItem(){		
		return checkListItem;
	}
	/**
	 * @param checkListItem -  key - ModuleNo (Checklist), value - Name(Checklist),
	 */
	public void setCheckListItem(Map<String, String> checkListItem) {
		this.checkListItem = checkListItem;
	}
	/**
	 * @return key - ModuleNo (Checklist), value - StaffValue (ProdAppChecklist)
	 */
	public  Map<String, String> getCheckListValue(){		
		return checkListValue;
	}	
	/**
	 * @param checkListValue - key - ModuleNo (Checklist), value - StaffValue (ProdAppChecklist)
	 */
	public void setCheckListValue(Map<String, String> checkListValue) {
		this.checkListValue = checkListValue;
	}

	/**
	 * reject|approve letter on screening
	 * @param prodAppID - id ProdApplications, 
	 * get list  ProdAppChecklist by id ProdApplications, 
	 * create map: key, checkListItem,  checkListValue
	 * key map : key - ModuleNo (Checklist), value - ModuleNo (Checklist),
	 * checkListItem : key - ModuleNo (Checklist), value - Name(Checklist),
	 * checkListValue: key - ModuleNo (Checklist), value - StaffValue (ProdAppChecklist)
	 */
	public void setParamToCheckListLetter(Long prodAppID ){
		Map<String, String> key = new HashMap<String, String>();
		Map<String, String> checkListItem = new HashMap<String, String>();
		Map<String, String> checkListValue = new HashMap<String, String>();

		List<ProdAppChecklist> checkLists =  getProdAppChecklistService().findProdAppChecklistByProdApp(prodAppID); 
		for(ProdAppChecklist item : checkLists){					

			String result = item.getChecklist().getName()!=null?item.getChecklist().getName().trim():"";
			String num = item.getChecklist().getModuleNo()!=null?item.getChecklist().getModuleNo().trim():"";
			String resAns = "";
			if(item.getStaffValue()!=null){
				if(item.getStaffValue()!=YesNoNA.NA){
					resAns = item.getStaffValue()==YesNoNA.YES?" Yes ":" No ";
				}				
			}else{
				resAns = "N/A";
			}	
			if(num!=null){
				key.put(num, num);
				checkListItem.put(num, result);
				checkListValue.put(num, resAns);
			}																	
		}	
		setKey(key);
		setCheckListItem(checkListItem);
		setCheckListValue(checkListValue);

	}
	/**
	 * @return X- if Screener set Approve on Screening, else "" 
	 */
	public String getScreeningApproved(){
		if(isApproved()){
			return "X";
		}else{
			return "";
		}
	}
	/**
	 * @return X- if Screener set Reject on Screening, else "" 
	 */
	public String getScreeningRejected(){
		if(!isApproved()){
			return "X";
		}else{
			return "";
		}
	}

	public void onItemSelect(SelectEvent event) {	
		event.getObject();
	}

	public void setFullList(List<FileTemplate> fullList) {
		this.fullList = fullList;
	}

	public ProdApplicationsService getProdApplicationsService() {
		return prodApplicationsService;
	}

	public void setProdApplicationsService(ProdApplicationsService prodApplicationsService) {
		this.prodApplicationsService = prodApplicationsService;
	}

	public ProdApplications getProdApplications() {
		return prodApplications;
	}

	public void setProdApplications(ProdApplications prodApplications) {
		this.prodApplications = prodApplications;
	}

	public TemplateType getTemplateType() {
		return templateType;
	}

	public void setTemplateType(TemplateType templateType) {
		this.templateType = templateType;
	}
	/**
	 * Create report by template
	 * @param appId - id ProdApplications
	 * @param templateType - type TemplateType
	 * @return StreamedContent which contain file 
	 */
	public StreamedContent getReport(Long appId, TemplateType templateType ){
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle resourceBundle = context.getApplication().getResourceBundle(context, "msgs");		
		setProdApplications(getProdApplicationsService().findProdApplications(appId));		
		FileTemplate fileTemplate = getTemplService().findByTemplateType(templateType);
		InputStream ist = null;
		if(fileTemplate!=null){
			ist = new ByteArrayInputStream(fileTemplate.getFile());
		}		
		try {
			InputStream downloadStream = getTemplService().generateReport(ist);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,resourceBundle.getString("global.success"),  resourceBundle.getString("global.success"));
			context.addMessage(null, msg);
			StreamedContent download = new DefaultStreamedContent(downloadStream, "docx",getFileName());
			return download;
		} catch (ELException e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,resourceBundle.getString("global_fail")+ " " + e.getLocalizedMessage(),
					resourceBundle.getString("global_fail")+ " " + e.getLocalizedMessage());
			context.addMessage(null, msg);
			return null;
		} catch (IOException e) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,resourceBundle.getString("global_fail")+ " " + e.getLocalizedMessage(),
					resourceBundle.getString("global_fail")+ " " + e.getLocalizedMessage());
			context.addMessage(null, msg);
			return null;
		} finally{
			try {
				ist.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Invoice
	 */
	/**
	 * Create report Invoice letter and calculate invoice total
	 * @param fileName  - name file
	 * @param appId - id applicant
	 * @param templateType - type TemplateType
	 * @return StreamedContent which contain file 
	 */
	public StreamedContent getReportInvoice(String fileName, TemplateType templateType, Invoice invoice){
		setFileName(fileName);
		setInvoiceApplicant(invoice.getApplicant());
		//setInvoceIssueDate(invoice.getCreatedDate());
		setInvoiceNumber(invoice.getInvoiceNumber());
		if(invoice!=null ){					
			if(!"".equals(invoice.getCurYear())){
				int yearCur = Integer.parseInt(invoice.getCurYear());
				int yearBefore = yearCur-1;
				setYear(yearBefore+"/"+yearCur);
			}
			//set size
			Long appId=invoice.getApplicant().getApplcntId();
			int sizeHuman = getProductService().findSizeProductByTypeApplicant(appId, ProdCategory.HUMAN, invoice.getCurYear());
			int sizeVeterinary = getProductService().findSizeProductByTypeApplicant(appId, ProdCategory.VETERINARY, invoice.getCurYear());
			setSizeHuman(sizeHuman+"");
			setSizeVeterinary(sizeVeterinary+"");
			//set cost					
			int costHuman = 0;
			int costVeterinary = 0;
			List<Workspace> list = workspaceDAO.findAll();
			if(list!=null && list.size()>0){
				Workspace ws = list.get(0);
				if(ws !=null){
					costHuman = ws.getCostHuman();
					costVeterinary = ws.getCostVeterinary();
				}
			}				
			setCostHuman(costHuman+"");
			setCostVeterinary(costVeterinary+"");

			//set amount
			int amountHuman = sizeHuman*costHuman;
			int amountVet = sizeVeterinary*costVeterinary;
			setAmountHuman(amountHuman+"");
			setAmountVeterinary(amountVet+"");
			//set all amount
			setAmountAll(amountHuman+amountVet+"");

			if(invoice.getIssueDate()!=null){
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd'th' MMMMM yyyy");
				setInvoceIssueDate(dateFormat.format(invoice.getIssueDate()));
			}											
			setInvoiceNumber(invoice.getInvoiceNumber()!=null?invoice.getInvoiceNumber():"");
			setAmountSumInPayment(invoice , amountHuman+amountVet);			
		}
		return getReport(invoice.getApplicant().getApplcntId(), templateType);
	}

	/**
	 * @param invoice - Invoice
	 * @param amountSum - amount sum
	 */
	private void setAmountSumInPayment(Invoice invoice, double amountSum) {		
		invoice.getPayment().setFeeSum((long) amountSum);
		getInvoiceService().getInvoiceDAO().saveAndFlush(invoice);		
	}

	/**
	 * @return screened user name (User)
	 */
	public String getScreenUser() {
		if(getScreenedUser()!=null){
			return getScreenedUser().getName()!=null?getScreenedUser().getName():"";			
		}else{
			return "";
		}
	}
	/**
	 * @return Date Screened on Screening,
	 * 		   if Screener set Reject on Screening Date Reject
	 */
	public String getScreenDate(){			
		if(dueDate!=null){
			return utilsByReports.getDateformat().format(dueDate);
		}else{
			Date curDate= new Date();
			curDate.getTime();				
			return utilsByReports.getDateformat().format(curDate);
		}		
	}

	/**
	 * @return moderator comment (review deficiency letter)
	 */
	public String getExecSummary() {
		return execSummary;
	}

	public void setExecSummary(String execSummary) {
		this.execSummary = execSummary;
	}

	/**
	 * @return Generic name(s)
	 */
	public String getGenName() {
		if(getProduct()!=null && getProduct().getGenName()!=null){
			return getProduct().getGenName();
		}
		return "test Generic name(s)";
	}

	/**
	 * @return Strength(s) per dosage unit
	 */
	public String getDosageStrength() {
		Product prod = getProduct();
		if(prod!=null){
			String res = "";
			if(prod.getDosStrength() != null){
				res = prod.getDosStrength();
			}
			if(prod.getDosUnit() != null ){
				res += " " + prod.getDosUnit().getUom();
			}
			return res;
		}
		return "test Strength(s) per dosage unit";
	}

	/**
	 * Get name of current user 
	 * @return
	 */
	public String getCurrentUser(){
		User user = getUserSession().getLoggedInUserObj();
		if(user == null){
			return "Anonimous";
		}else{
			return user.getUsername();
		}

	}

	/**
	 * Create review deficiency letter
	 * Set parameter to the report,
	 * Create and download a report for application appId
	 * @param appId - id ProdApplications
	 * @param templateType - DEFICIENCY, type of letter 
	 * @param execSummary - moderator comment
	 * @return StreamedContent where contains file
	 */
	public StreamedContent getReport(Long appId, TemplateType templateType, String execSummary){
		setExecSummary(execSummary);
		return getReport(appId, templateType);
	}
	/**
	 * Create reject|approve letter on screening
	 * Set parameter to the report,
	 * Create and download a report for application appId
	 * @param appId - id ProdApplications
	 * @param templateType - SCREENING_OUTCOME
	 * @param dueDate - 
	 * @param isApproved - if Approved - true, else - false
	 * @return StreamedContent where contains file
	 */
	public StreamedContent getReport(Long appId, TemplateType templateType, Date dueDate,boolean isApproved, Long loggedINUserID){
		setApproved(isApproved);			
		setParamToCheckListLetter(appId);		
		setDueDate(dueDate);
		setScreenedUser(userService.findUser(loggedINUserID));
		return getReport(appId, templateType);
	}
	/**
	 * create Registration Certificate
	 * @param appId - id ProdApplications
	 * @param templateType - CERTIFICATE
	 * @param name - file name
	 * @return StreamedContent where contains file
	 */
	public StreamedContent getReport(String fileName,Long appId, TemplateType templateType){
		setFileName(fileName);
		return getReport(appId, templateType);
	}

	/**
	 * create Rejection
	 * @param appId - id ProdApplications
	 * @param templateType - REJECTION
	 * @param reasonRejection - Reason for rejection
	 * @return StreamedContent where contains file
	 */
	public StreamedContent getReport(String fileName, String reasonRej,Long appId, TemplateType templateType){
		setFileName(fileName);
		//clean httl tags
		String comment = reasonRej.replaceAll("\\<.*?\\>", "");
		setReasonRejection(comment);
		return getReport(appId, templateType);
	}
	/**
	 * @return type of report
	 */
	public FileTemplate getFileTemplate() {
		return fileTemplate;
	}
	/**
	 * @return type of report
	 */
	public void setFileTemplate(FileTemplate fileTemplate) {
		this.fileTemplate = fileTemplate;
		if(fileTemplate.getStaffOnly() != null)
			setStaffOnly(fileTemplate.getStaffOnly());
		else
			setStaffOnly(false);
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public UtilsByReports getUtilsByReports() {
		return utilsByReports;
	}

	public void setUtilsByReports(UtilsByReports utilsByReports) {
		this.utilsByReports = utilsByReports;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	/**
	 * @return true if Screener set Approve on Screening, else - false
	 */
	public boolean isApproved() {
		return isApproved;
	}

	public void setApproved(boolean isApproved) {
		this.isApproved = isApproved;
	}

	public ProdAppChecklistService getProdAppChecklistService() {
		return prodAppChecklistService;
	}

	public void setProdAppChecklistService(ProdAppChecklistService prodAppChecklistService) {
		this.prodAppChecklistService = prodAppChecklistService;
	}

	/**
	 * @param screenedUser - screened user
	 */
	public void setScreenedUser(User screenedUser) {
		this.screenedUser = screenedUser;
	}
	/**
	 * @return screened user
	 */
	public User getScreenedUser() {
		return screenedUser;
	}

	/**
	 * @return Report File Name
	 */
	public String getFileName() {
		if(fileName!=null){
			return fileName;
		}else{
			if(getProdApplications()!=null && getProdApplications().getProdSrcNo()!=null){
				return  getProdApplications().getProdSrcNo()+".docx";
			}			
		}
		return "fileName"+".docx";
	}

	/**
	 * @param fileName - Report File Name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return Reason for rejection
	 */
	public String getReasonRejection() {
		if(reasonRejection!=null){
			return reasonRejection;
		}
		return "test ReasonRejection";
	}
	/** 
	 * @param reasonRejection - Reason for rejection
	 */
	public void setReasonRejection(String reasonRejection) {
		this.reasonRejection = reasonRejection;
	}

	public ApplicantService getApplicantService() {
		return applicantService;
	}

	public void setApplicantService(ApplicantService applicantService) {
		this.applicantService = applicantService;
	}

	/**
	 * We can upload a template only if template type is defined
	 */
	public boolean isUploadTemplate(){
		return getTemplateType() != null;
	}

	/**
	 * @return Retention fees for Year Human (Description)
	 */
	/*public String getYearHuman() {
		return yearHuman;
	}*/

	/**
	 * @param yearHuman - Retention fees for Year Human (Description)
	 */
	/*public void setYearHuman(String yearHuman) {
		this.yearHuman = yearHuman;
	}*/

	/**	
	 * @return count Human type Product (Quantity)
	 */
	public String getSizeHuman() {
		return sizeHuman;
	}

	/**
	 * @param sizeHuman - count Human type Product (Quantity)
	 */
	public void setSizeHuman(String sizeHuman) {
		this.sizeHuman = sizeHuman;
	}

	/**	
	 * @return count Veterinary type Product (Quantity)
	 */
	public String getSizeVeterinary() {
		return sizeVeterinary;
	}

	/**
	 * @param sizeVeterinary -  count Veterinary type Product (Quantity)
	 */
	public void setSizeVeterinary(String sizeVeterinary) {
		this.sizeVeterinary = sizeVeterinary;
	}

	/**
	 * @return Retention fees for Year Veterinary (Description)
	 */
	/*public String getYearVeterinary() {
		return yearVeterinary;
	}

	/**
	 * @param yearVeterinary - Retention fees for Year Veterinary (Description)
	 */
	/*public void setYearVeterinary(String yearVeterinary) {
		this.yearVeterinary = yearVeterinary;
	}
	 */

	/**
	 * @return cost Human type Product (Unit cost(N$))
	 */
	public String getCostHuman() {
		return costHuman;
	}

	/**
	 * @param costHuman - cost Human type Product (Unit cost(N$))
	 */
	public void setCostHuman(String costHuman) {
		this.costHuman = costHuman;
	}

	/**
	 * @return cost Veterinary type Product (Unit cost(N$))
	 */
	public String getCostVeterinary() {
		return costVeterinary;
	}

	/**
	 * 
	 * @param costVeterinary -  cost Veterinary type Product (Unit cost(N$))
	 */
	public void setCostVeterinary(String costVeterinary) {
		this.costVeterinary = costVeterinary;
	}

	/**
	 * @return amount Human type Product (AMOUNT (N$))
	 */
	public String getAmountHuman() {
		return amountHuman;
	}

	/**	 
	 * @param amountHuman - amount Human type Product (AMOUNT (N$))
	 */
	public void setAmountHuman(String amountHuman) {
		this.amountHuman = amountHuman;
	}

	/**
	 * @return amount Veterinary type Product (AMOUNT (N$))
	 */
	public String getAmountVeterinary() {
		return amountVeterinary;
	}

	/**
	 * @param amountVeterinary - amount Veterinary type Product (AMOUNT (N$))
	 */
	public void setAmountVeterinary(String amountVeterinary) {
		this.amountVeterinary = amountVeterinary;
	}

	/**
	 * @return Applicant Name
	 */
	public String getInvoiceApplicantName(){
		if(getInvoiceApplicant()!=null){
			return getInvoiceApplicant().getAppName();

		}else{
			return "test applicant";
		}
	}

	/**
	 * @return Applicant Addr1
	 */
	public String getInvoiceApplicantAddr1(){
		if(getInvoiceApplicant()!=null &&  getInvoiceApplicant().getAddress()!=null){
			String s = getInvoiceApplicant().getAddress().getAddress1();
			if (s!= null){
				return s;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}

	/**
	 * @return Applicant Addr2
	 */
	public String getInvoiceApplicantAddr2(){
		if( getInvoiceApplicant()!=null &&  getInvoiceApplicant().getAddress()!=null){
			String s = getInvoiceApplicant().getAddress().getAddress2();
			if (s!= null){
				return s;
			}else{
				return "";
			}
		}else{
			return "";
		}
	}

	/**
	 * @return Applicant Country, ZipCode
	 */
	public String getInvoiceApplicantCountryZipCode(){
		String s = getApplicantCountry();
		if( getInvoiceApplicant()!=null && getInvoiceApplicant().getAddress()!=null){
			if(getInvoiceApplicant().getAddress().getZipcode()!=null && !"".equals(getInvoiceApplicant().getAddress().getZipcode())){
				s += ", "+getInvoiceApplicant().getAddress().getZipcode();
				return s;
			}
		}				
		return "";
	}

	/**
	 * @return Invoice Applicant 
	 */
	public Applicant getInvoiceApplicant() {
		return invoiceApplicant;
	}

	/**
	 * @param invoiceApplicant - Invoice Applicant 
	 */
	public void setInvoiceApplicant(Applicant invoiceApplicant) {
		this.invoiceApplicant = invoiceApplicant;
	}

	/**
	 * @return Invoce Issue Date
	 */
	public String getInvoceIssueDate() {
		return invoceIssueDate;
	}

	/**
	 * @param invoce Issue Date
	 */
	public void setInvoceIssueDate(String invoceIssueDate) {
		this.invoceIssueDate = invoceIssueDate;
	}

	/**
	 * @return Invoice Number
	 */
	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	/**
	 * @param invoiceNumber - Invoice Number
	 */
	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	/**
	 * @return Human Amount + Veterinary Amount + Complementary Amount
	 */
	public String getAmountAll() {
		return amountAll;
	}

	/**
	 * @param amountAll - Human Amount + Veterinary Amount + Complementary Amount
	 */
	public void setAmountAll(String amountAll) {
		this.amountAll = amountAll;
	}

	public InvoiceService getInvoiceService() {
		return invoiceService;
	}

	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	/**
	 * @return Retention fees for Year (Description)
	 */
	public String getYear() {
		return year;
	}

	/**
	 * 
	 * @param year - Retention fees for Year (Description)
	 */
	public void setYear(String year) {
		this.year = year;
	}

	public WorkspaceDAO getWorkspaceDAO() {
		return workspaceDAO;
	}

	public void setWorkspaceDAO(WorkspaceDAO workspaceDAO) {
		this.workspaceDAO = workspaceDAO;
	}







}
