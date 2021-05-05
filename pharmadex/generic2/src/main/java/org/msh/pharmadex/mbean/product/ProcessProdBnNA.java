package org.msh.pharmadex.mbean.product;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.context.PartialViewContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdCompany;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.ReviewInfo;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.enums.CompanyType;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.domain.enums.ReviewStatus;
import org.msh.pharmadex.domain.enums.TemplateType;
import org.msh.pharmadex.mbean.amendment.IAmendment;
import org.msh.pharmadex.service.CommentService;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.service.ProdApplicationsServiceMZ;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.ReviewService;
import org.msh.pharmadex.service.amendment.AmendmentService;
import org.msh.pharmadex.util.JsfUtils;
import org.msh.pharmadex.util.Scrooge;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.StreamedContent;

/**
 * @author Admin
 *
 */
@ManagedBean
@ViewScoped
public class ProcessProdBnNA implements Serializable {

	private static final long serialVersionUID = -630551744701773009L;

	@ManagedProperty(value = "#{processProdBn}")
	public ProcessProdBn processProdBn;

	@ManagedProperty(value = "#{processProdBnMZ}")
	public ProcessProdBnMZ processProdBnMZ;

	@ManagedProperty(value = "#{userSession}")
	public UserSession userSession;

	@ManagedProperty(value = "#{prodApplicationsService}")
	public ProdApplicationsService prodApplicationsService;

	@ManagedProperty(value = "#{productService}")
	protected ProductService productService;

	@ManagedProperty(value = "#{reviewService}")
	public ReviewService reviewService;

	@ManagedProperty(value = "#{commentService}")
	private CommentService commentService;

	@ManagedProperty(value = "#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty(value = "#{prodApplicationsServiceMZ}")
	private ProdApplicationsServiceMZ prodApplicationServiceMZ;

	@ManagedProperty(value = "#{templMBean}")
	private TemplMBean templMBean;

	@ManagedProperty(value = "#{reviewInfoBnMZ}")
	private ReviewInfoBnMZ reviewInfoBnMZ;

	@ManagedProperty("#{flash}")
	private Flash flash;

	private String BACK_TO = "backTo";
	private String changedFields;
	private boolean disableVerify = true;
	private boolean prescreened = false;
	private boolean showFeedBackButton;
	private List<ProdApplications> allAncestors;

	private boolean showTabAttach = false;
	private boolean showFeeRecBtn = false;
	private boolean visibleAssignBtn = false;
	private boolean visibleExecSumeryBtn = false;
	private boolean visibleAmdTab = false;

	private Amendment currentAmd = null;
	private IAmendment currentAmdMbean = null;
	private String backTo = null;

	//private ProdApplications prodApplications = null;

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");

	@PostConstruct
	private void init() {
		if(getProcessProdBn().getProdApplications() != null){
			changedFields = getProcessProdBn().getProdApplications().getAppComment();
			currentAmd = getProcessProdBn().getProdApplications().getAmendment();

			if(currentAmd != null){
				currentAmdMbean = (IAmendment)JsfUtils.getManagedBean(currentAmd.getPharmadexDB().getMBeanName());
				currentAmd = currentAmdMbean.findAmendment(currentAmd.getId());
			}
		}
		if (changedFields==null) changedFields="";
		setVisibleAmdTab(currentAmd != null);

		backTo = (String) getFlash().get(BACK_TO);
	}

	public String getAmendmentSummaryTemplate(){
		String template = "/templates/amendment/badamd.xhtml";
		if(currentAmd != null){
			template = currentAmd.getPharmadexDB().getSummaryTemplate();
		}
		return template;
	}



	public boolean isFieldChanged(String fieldname){
		//получим список из review_info.changedFields  если в списке нет, то false
		//   	 String fieldname = (String) UIComponent.getCurrentComponent(FacesContext.getCurrentInstance()).getAttributes().get("fieldvalue");
		if (changedFields.contains(fieldname)) return true;
		return false;
	}

	public boolean findInnChanged(){
		//получим список из review_info.changedFields  если в списке нет, то false
		if (changedFields.contains("inns")) return true;
		return false;
	}

	public boolean findExcipientChanged(){
		//получим список из review_info.changedFields  если в списке нет, то false
		if (changedFields.contains("excipients"))
			return true;
		else
			return false;
	}
	public boolean findAtcChanged(){
		//получим список из review_info.changedFields  если в списке нет, то false
		if (changedFields.contains("Atc")) return true;
		return false;
	}

	public boolean findCompaniesChanged(){
		if (changedFields.contains("ProdCompanies")) return true;
		return false;
	}

	public void setShowFeedBackButton(boolean showFeedBackButton) {
		this.showFeedBackButton = showFeedBackButton;
	}

	public List<ProdApplications> getAllAncestors(){
		if (allAncestors==null){
			ProdApplications prod = processProdBn.getProdApplications();
			allAncestors = getProdApplicationsService().getAllAncestor(prod);
		}
		return allAncestors;
	}


	public void setAllAncestors(List<ProdApplications> allAncestors) {
		this.allAncestors = allAncestors;
	}

	public ProcessProdBn getProcessProdBn() {
		return processProdBn;
	}

	public void setProcessProdBn(ProcessProdBn processProdBn) {
		this.processProdBn = processProdBn;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public ProdApplicationsService getProdApplicationsService() {
		return prodApplicationsService;
	}

	public void setProdApplicationsService(ProdApplicationsService prodApplicationsService) {
		this.prodApplicationsService = prodApplicationsService;
	}
	public ReviewService getReviewService() {
		return reviewService;
	}
	public void setReviewService(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	public CommentService getCommentService() {
		return commentService;
	}
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	/**
	 * Issues #2339
	 * Expiry date should be calculated as registration date + 365*5 (days)
	 * Issues #2557
	 * 31.03 current or next year
	 */
	public void dateChange() {
		/*		Calendar march = Calendar.getInstance();
		march.set(Calendar.MONTH, 2);
		march.set(Calendar.DAY_OF_MONTH, 31);

		Calendar rDate = Calendar.getInstance();
		rDate.setTime(getProcessProdBn().getProdApplications().getRegistrationDate());

		if(rDate.after(march) || equalDates(rDate, march)){
			march.add(Calendar.YEAR, 1);
		}*/
		Date march = calcNextRegExpireDate(getProcessProdBn().getProdApplications().getRegistrationDate());
		if(isApplicationAmendment()){
			if(getProcessProdBn().getProdApplications().getRegExpiryDate() == null)
				getProcessProdBn().getProdApplications().setRegExpiryDate(march);
		}else
			getProcessProdBn().getProdApplications().setRegExpiryDate(march);
	}

	/**
	 * For Neverland registration expire date calculates as closest Mar 31 to the baseDate
	 * Base date is registration date or current registration expire date
	 * When baseDate is null, current date will be base date
	 * @param baseDate
	 * @return
	 */
	public static  Date calcNextRegExpireDate(Date baseDate){
		LocalDate base = LocalDate.now();
		if(baseDate != null){
			base = LocalDate.from(base);
		}
		LocalDate ret = LocalDate.of(base.getYear(), 3, 31);
		if(!ret.isAfter(base)){  
			ret = ret.plusYears(1);
		}
		return  Date.from(ret.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public static Date addDays(Date dt, int countDay){
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DAY_OF_YEAR, countDay);
		return c.getTime();
	}

	public boolean isDisableVerify() {
		disableVerify = true;
		ProdApplications prodApp = getProcessProdBn().getProdApplications();
		if(prodApp == null)
			return disableVerify;

		if(userSession.isStaff()){
			if(prodApp.getRegState().ordinal() < RegState.SCREENING.ordinal()){
				disableVerify = false; // edit tab
			}
		}

		return disableVerify;
	}

	public boolean isPrescreened() {
		prescreened = false;
		ProdApplications prodApp = getProcessProdBn().getProdApplications();
		if(prodApp == null)
			return prescreened;

		if(userSession.isStaff()){
			if (prodApp.getRegState().equals(RegState.FOLLOW_UP) || 
					prodApp.getRegState().equals(RegState.VERIFY))
				prescreened = true;
		}

		return prescreened;
	}

	public void setPrescreened(boolean prescreened) {
		this.prescreened = prescreened;
	}

	/**
	 * Works only for buttons on Verify tab and a button "Appl fee received" on Assesment tab 
	 */
	public void changeStatusListener() {
		try {
			ProdApplications prodApp = getProcessProdBn().getProdApplications();
			// from verified tab
			if (prodApp.getRegState().equals(RegState.NEW_APPL)) {
				if (isReadyToScreening()) {
					getProdApplicationServiceMZ().assignNumbers(prodApp);
					PartialViewContext partialViewCtx = FacesContext.getCurrentInstance().getPartialViewContext();
					partialViewCtx.getRenderIds().add("reghome:banner");					
					StreamedContent streamedContent = getTemplMBean().getReport(prodApp.getId(),
							TemplateType.DOSSIER_RECIEPT);
					getProdApplicationServiceMZ().createDossierReceiptAckLetter(prodApp, streamedContent,
							TemplateType.DOSSIER_RECIEPT);
					getProcessProdBn().getTimeLine().setRegState(RegState.VERIFY);
					getProcessProdBn().addTimeline();
				}
			}
			// from assesment tab
			if (prodApp.getRegState().equals(RegState.SCREENING)) {
				if (prodApp.getPayment() != null && prodApp.getPayment().isPayment_received()) {
					getProdApplicationServiceMZ().assignAppNumbers(prodApp);
					PartialViewContext partialViewCtx = FacesContext.getCurrentInstance().getPartialViewContext();
					partialViewCtx.getRenderIds().add("reghome:banner");					
					getProcessProdBn().getTimeLine().setRegState(RegState.APPL_FEE);
					getProcessProdBn().addTimeline();
				}
			}
			/* 20/09/17 
			  ProdApplications prodApp = getProcessProdBn().getProdApplications();
			   if (prodApp.getRegState().equals(RegState.NEW_APPL)) {
			    if (prodApp.getPayment().isPayment_received()) {//prodApp.isFeeReceived()
			     getProcessProdBn().getTimeLine().setRegState(FEE);
			     getProcessProdBn().addTimeline();
			    }
			   }
			   if (prodApp.getRegState().equals(RegState.FEE)) {
			    if (isReadyToScreening()) {
			     getProdApplicationServiceMZ().assignNumbers(prodApp);
			     StreamedContent streamedContent = getTemplMBean().getReport(prodApp.getId(), TemplateType.DOSSIER_RECIEPT);  
			     getProdApplicationServiceMZ().createDossierReceiptAckLetter(prodApp, streamedContent); 
			     getProcessProdBn().getTimeLine().setRegState(RegState.VERIFY);
			     getProcessProdBn().addTimeline();
			    }
			   }

			   if(prodApp.getRegState().equals(RegState.SCREENING)){
			    getProcessProdBn().getTimeLine().setRegState(RegState.APPL_FEE);
			    getProcessProdBn().addTimeline();
			   }*/

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Is this application ready to screening
	 * @param prodApp
	 * @return
	 */
	public boolean isReadyToScreening() {
		if(userSession.isCompany())
			return false;
		ProdApplications prodApp = getProcessProdBn().getProdApplications();
		if (prodApp != null){
			//prodApp.isPrescreenfeeReceived()
			return prodApp.getPaymentPrescreen().isPayment_received() && prodApp.isApplicantVerified() && prodApp.isProductVerified() && prodApp.isDossierReceived();
		}else{
			return false;
		}
	}

	/**
	 * Dummy setter to obey bean spec
	 * @param dummy
	 */
	public void setReadyToScreening(boolean dummy){

	}

	public boolean getCanNextStep() {
		try {
			RegState curRegState = getProcessProdBn().getProdApplications().getRegState();

			if((userSession.isAdmin() || userSession.isModerator() || userSession.isHead()) 
					&& !curRegState.equals(RegState.REGISTERED) && !curRegState.equals(RegState.REJECTED)
					&& !curRegState.equals(RegState.FOLLOW_UP))
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}


	public boolean isShowTabAttach() {
		showTabAttach = true;
		//userSession.admin||userSession.staff||userSession.moderator||userSession.reviewer||userSession.lab
		return showTabAttach;
	}
	public void setShowTabAttach(boolean showTabAttach) {
		this.showTabAttach = showTabAttach;
	}

	public void onTabChange(TabChangeEvent event) {
		if(processProdBn.getProdApplications() == null){
			Scrooge.goToHome();
			return ;
		}
		//processProdBn.save();
	}
	public boolean isShowFeeRecBtn() {
		try {
			showFeeRecBtn = true;		
			if(processProdBn.getProdApplications().getPayment()!=null){
				if(processProdBn.getProdApplications().getPayment().getPayment_payDate()==null){
					showFeeRecBtn = true;	
					return showFeeRecBtn;
				}				
			}
			if(processProdBn.getProdApplications().getRegState().equals(RegState.SCREENING)){
				if(userSession.isModerator())
					showFeeRecBtn = false;
			}else if(processProdBn.getProdApplications().getRegState().equals(RegState.APPL_FEE)){
				showFeeRecBtn = true;
			}

			return showFeeRecBtn;
		} catch (Exception e) {
			return false;
		}
	}
	public void setShowFeeRecBtn(boolean showFeeRecBtn) {
		this.showFeeRecBtn = showFeeRecBtn;
	}

	public boolean isVisibleAssignBtn() {
		visibleAssignBtn = false;
		if(userSession.isModerator() || userSession.isAdmin()){
			if((processProdBn.getProdApplications().getPayment().isPayment_received())//processProdBn.getProdApplications().isFeeReceived()
					&& !isVisibleExecSumeryBtn())
				visibleAssignBtn = true;
		}
		return visibleAssignBtn;
	}
	public void setVisibleAssignBtn(boolean visibleAssignBtn) {
		this.visibleAssignBtn = visibleAssignBtn;
	}

	public boolean isVisibleExecSumeryBtn() {
		if((userSession.isModerator() || userSession.isAdmin() || userSession.isHead()) 
				&& processProdBn.getUserAccessMBean().isDetailReview()){
			// if All ReviewInfo in state ACCEPTED
			List<ReviewInfo> list = processProdBn.getReviewInfos();
			if(list != null && list.size() > 0){
				for(ReviewInfo rinfo:list){
					if(!rinfo.getReviewStatus().equals(ReviewStatus.ACCEPTED)){
						visibleExecSumeryBtn = false;
						return visibleExecSumeryBtn;
					}
				}
				visibleExecSumeryBtn = true;
			}
		}
		return visibleExecSumeryBtn;
	}
	public void setVisibleExecSumeryBtn(boolean visibleExecSumeryBtn) {
		this.visibleExecSumeryBtn = visibleExecSumeryBtn;
	}

	public ProcessProdBnMZ getProcessProdBnMZ() {
		return processProdBnMZ;
	}
	public void setProcessProdBnMZ(ProcessProdBnMZ processProdBnMZ) {
		this.processProdBnMZ = processProdBnMZ;
	}

	/**
	 * Reject Product
	 * @param prodApplications
	 * @return
	 */
	public String rejectProduct(ProdApplications prodApplications) {
		prodApplications.setRegistrationDate(new Date());

		if (!prodApplications.getRegState().equals(RegState.NOT_RECOMMENDED)) {
			context.addMessage(null, new FacesMessage("Invalid operation!", bundle.getString("Error.headNotReject")));
			return "";
		}

		if(isApplicationAmendment()){
			// need rejected Amendment
			currentAmdMbean.getAmendment().setState(AmdState.REJECTED);
			currentAmdMbean.getAmendment().setRejectDate(new Date());
			currentAmdMbean.getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
			currentAmdMbean.getAmendment().setUpdatedDate(new Date());

			currentAmdMbean.reject();
		}
		return getProcessProdBnMZ().rejectProduct(prodApplications, TemplateType.REJECTION);
	}

	/*public boolean visCreateNew(){
		boolean res = false;
		try {
			if(getProcessProdBn().getProdApplications().getRegState().equals(RegState.REJECTED)){
				if(userSession.isStaff()) // by Screener
					return true;

				Applicant applicant = getProcessProdBn().getProdApplications().getApplicant();
				// by Responsable of Applicant
				if(userSession.isResponsible(applicant))
					return true;

				if(userSession.isAgent(applicant))
					return true;
			}
			return res;
		} catch (Exception e) {
			return false;
		}
	}*/

	/*public String createNewApplicationByRejProduct(){
		ProdApplications prodApp = getProcessProdBn().getProdApplications();
		if(prodApp != null){
			Product prod = prodApp.getProduct();

			ProdApplications newProdApp = new ProdApplications();
			newProdApp.setProduct(prod);
			newProdApp.setApplicant(prodApp.getApplicant());
			newProdApp.setProdAppType(prodApp.getProdAppType());
			newProdApp.setRegState(RegState.SAVED);
			newProdApp.setApplicantUser(prodApp.getApplicantUser());
			Long curUserID = userSession.getLoggedINUserID();
			newProdApp = getProdApplicationsService().saveApplication(newProdApp, curUserID);

			Scrooge.setBeanParam("prodAppID", newProdApp.getId());
			userSession.setProdAppID(newProdApp.getId());

			return "/secure/prodreghome.faces";
		}

		return null;
	}*/

	public boolean visibleSaveBtn(){
		try {
			if(getProcessProdBn().getProdApplications().getRegState().equals(RegState.REGISTERED))
				return false;
			if(userSession.isCompany() || userSession.isStaff() || userSession.isAdmin())
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean visibleAddAttachBtn(){
		RegState rs = getProcessProdBn().getProdApplications().getRegState();
		if(rs.equals(RegState.REGISTERED))
			return false;
		return true;
	}
	/**
	 * Get full list of product manufacturers (Inns included)
	 * @return
	 */
	public List<ProdCompany> getProdCompanies() {
		List<ProdCompany> prodCompanies = new ArrayList<ProdCompany>();

		Product prod = getProcessProdBn().getProduct();
		if(prod==null){
			prod = getProcessProdBn().getProdRegAppMbean().getProduct();
		}
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
		return prodCompanies;
	}

	/**
	 * set default values before Registration
	 */
	public void initRegistration() {
		if (getProcessProdBn().getProdApplications().getProdRegNo() == null || getProcessProdBn().getProdApplications().getProdRegNo().equals("")){
			String prodAppNo  = getProdApplicationsService().generateAppNo(processProdBn.getProdApplications());
			String classificationNo = "";
			if(getProcessProdBn().getProdApplications().getProduct()!=null ){
				Product prod = productService.findProduct(getProcessProdBn().getProdApplications().getProduct().getId());
				if (prod!=null && prod.getPcCodes()!=null 
						&& prod.getPcCodes().size()>0){
					classificationNo = prod.getPcCodes().get(0)!=null ?prod.getPcCodes().get(0).getPcCode():"" ;
				}
			}					
			processProdBn.getProdApplications().setProdRegNo(prodApplicationsService.generateRegNo(prodAppNo, classificationNo));
			//03/10/17
			//getProcessProdBn().getProdApplications().setProdRegNo(RegistrationUtil.generateRegNo("" + (getProductService().getSizeAllRegisteredProduct() + 1), 
			//getProcessProdBn().getProdApplications().getProdAppNo()));
		}
		// copy RegExpiryDate in previous ProdApplication RegExpiryDate
		if(isApplicationAmendment())
			getProcessProdBn().getProdApplications().setRegExpiryDate(getProdApplicationsService().getRegExpiryDatePreviousProdApp(getProcessProdBn().getProdApplications()));
	}

	public String registerProduct() {
		if(isApplicationAmendment()){
			getProdApplicationsService().sendToArchiveAllProdAppByProduct(getProcessProdBn().getProdApplications(), userSession.getLoggedInUserObj());

			if(currentAmdMbean != null){
				//IAmendment amdMbean = (IAmendment)JsfUtils.getManagedBean(currentAmd.getPharmadexDB().getMBeanName());
				//amdMbean.findAmendment(currentAmd.getId());
				currentAmdMbean.getAmendment().setState(AmdState.APPROVED);
				currentAmdMbean.getAmendment().setEffectiveDate(new Date());
				currentAmdMbean.getAmendment().setUpdatedBy(getUserSession().getLoggedInUserObj());
				currentAmdMbean.getAmendment().setUpdatedDate(new Date());

				String err = currentAmdMbean.implement(getProcessProdBn().getProdApplications());
				getProcessProdBn().getProdApplications().setProduct(currentAmdMbean.getAmendment().getProduct());
			}
		}
		getProcessProdBnMZ().registerProduct(getProcessProdBn().getProdApplications(), TemplateType.CERTIFICATE);

		if(backTo != null && !backTo.isEmpty())
			return backTo;
		return null;
	}

	/**
	 * build header Dialog and name button in Dialog
	 * 1) name Action in left menu
	 * 2) header and name button by registration product Dialog
	 * 3) header and name button by created certificate
	 * @return
	 */
	public String getRegisterBtnName(boolean isHeaderDlg){
		String name = bundle.getString("register_product");
		if(getProcessProdBn().getProdApplications() != null){
			if(getProcessProdBn().getProdApplications().getRegState().equals(RegState.REGISTERED)){
				if(isHeaderDlg)
					return bundle.getString("reg_cert_create");
				else
					return bundle.getString("label_create");
			}else{
				if(isApplicationAmendment())
					return bundle.getString("register_application");
			}
		}
		return name;
	}

	public boolean isApplicationAmendment(){
		if(currentAmd != null)
			return true;
		return false;
	}

	public void createFileReviewDetail(){
		ProdApplications prodApplications = getProcessProdBn().getProdApplications();
		if(prodApplications == null)
			Scrooge.goToHome();
		else
			getReviewInfoBnMZ().setFileReviewDetail(getProdApplicationServiceMZ().createReviewDetailsFile(prodApplications));
	}

	public void setBackToInFlash(String backTo){
		getFlash().put(BACK_TO, backTo);
	}

	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}

	public ProdApplicationsServiceMZ getProdApplicationServiceMZ() {
		return prodApplicationServiceMZ;
	}

	public void setProdApplicationServiceMZ(ProdApplicationsServiceMZ prodApplicationServiceMZ) {
		this.prodApplicationServiceMZ = prodApplicationServiceMZ;
	}

	public TemplMBean getTemplMBean() {
		return templMBean;
	}

	public void setTemplMBean(TemplMBean templMBean) {
		this.templMBean = templMBean;
	}

	public boolean isVisibleAmdTab() {
		return visibleAmdTab;
	}

	public void setVisibleAmdTab(boolean visibleAmnTab) {
		this.visibleAmdTab = visibleAmnTab;
	}

	public Flash getFlash() {
		return flash;
	}

	public void setFlash(Flash flash) {
		this.flash = flash;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public ReviewInfoBnMZ getReviewInfoBnMZ() {
		return reviewInfoBnMZ;
	}

	public void setReviewInfoBnMZ(ReviewInfoBnMZ reviewInfoBnMZ) {
		this.reviewInfoBnMZ = reviewInfoBnMZ;
	}


}