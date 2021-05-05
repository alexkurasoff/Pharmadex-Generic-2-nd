package org.msh.pharmadex.mbean.product;

import static javax.faces.context.FacesContext.getCurrentInstance;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdAppLetter;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ReviewInfo;
import org.msh.pharmadex.domain.SuspDetail;
import org.msh.pharmadex.domain.TimeLine;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.enums.RecomendType;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.domain.enums.ReviewStatus;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.service.ProdApplicationsServiceMZ;
import org.msh.pharmadex.service.ReviewService;
import org.msh.pharmadex.service.UserService;
import org.msh.pharmadex.util.RegistrationUtil;
import org.msh.pharmadex.util.RetObject;
import org.msh.pharmadex.util.Scrooge;
import org.primefaces.event.TabChangeEvent;
import org.springframework.web.util.WebUtils;

import net.sf.jasperreports.engine.JRException;

/**
 * Backing bean to process the application made for registration
 */
@ManagedBean
@ViewScoped
public class ProcessProdBnMZ implements Serializable {

	private static final long serialVersionUID = 3698548480414883122L;

	protected FacesContext facesContext = getCurrentInstance();
	protected java.util.ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");

	@ManagedProperty(value = "#{prodApplicationsServiceMZ}")
	protected ProdApplicationsServiceMZ prodApplicationsServiceMZ;

	@ManagedProperty(value = "#{prodApplicationsService}")
	protected ProdApplicationsService prodApplicationsService;

	@ManagedProperty(value = "#{reviewService}")
	protected ReviewService reviewService;
	
	@ManagedProperty(value = "#{userService}")
	private UserService userService;
	@ManagedProperty(value = "#{userSession}")
	protected UserSession userSession;

	@ManagedProperty(value = "#{processProdBn}")
	private ProcessProdBn processProdBn;

	public User loggedInUser;
	private String gestorDeCTRM = resourceBundle.getString("gestorDeCTRM_value");
	private String rejectSumm = "";
	private boolean generic = false;

	private boolean visibleExecSumeryBtn = false;
	private boolean disableCheckSample = false;
	private boolean showAssessment = false;
	
	private List<SuspDetail> suspDetails;
	private String sourcePage = "/public/registrationhome.faces";
	
	@PostConstruct
	private void init() {
		try {
			loggedInUser = userService.findUser(userSession.getLoggedINUserID());
			String str = Scrooge.beanStrParam("sourcePage");
			if(str != null && !str.equals(""))
				sourcePage = str;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public String cancel() {
		facesContext = getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		WebUtils.setSessionAttribute(request, "processProdBn", null);
		
		return sourcePage + "?faces-redirect=true";
	}
	
	public List<RegState> getRegSate() {
		if (getProcessProdBn().getProdApplications() != null)
			return prodApplicationsServiceMZ.nextStepOptions();
		return null;
	}

	public void addTimeline() {
		facesContext = getCurrentInstance();
		try {
			getProcessProdBn().getTimeLine().setStatusDate(new Date());
			getProcessProdBn().getTimeLine().setUser(loggedInUser);
			RetObject paObject = getProcessProdBn().getProdApplicationsService().updateProdApp(getProcessProdBn().getProdApplications(), loggedInUser.getUserId());
			getProcessProdBn().setProdApplications((ProdApplications) paObject.getObj());
			getProcessProdBn().getTimeLine().setProdApplications(getProcessProdBn().getProdApplications());

			//String retValue = getProcessProdBn().getTimelineService().validateStatusChange(getProcessProdBn().getTimeLine());

			//if (retValue.equalsIgnoreCase("success")) {
			getProcessProdBn().getProdApplications().setRegState(getProcessProdBn().getTimeLine().getRegState());
			RetObject retObject = getProcessProdBn().getProdApplicationsService().updateProdApp(getProcessProdBn().getProdApplications(), loggedInUser.getUserId());
			if (retObject.getMsg().equals("persist")) {
				getProcessProdBn().setProdApplications((ProdApplications) retObject.getObj());
				//changeStateReviewInfo();
				getProcessProdBn().setFieldValues();
				getProcessProdBn().getTimeLine().setProdApplications(getProcessProdBn().getProdApplications());
				getProcessProdBn().getTimelineService().saveTimeLine(getProcessProdBn().getTimeLine());
				getProcessProdBn().getTimeLineList().add(getProcessProdBn().getTimeLine());
				facesContext.addMessage(null, new FacesMessage(resourceBundle.getString("status_change_success")));
			} else {
				facesContext.addMessage(null, new FacesMessage(retObject.getMsg()));
			}
			/*} else if (retValue.equalsIgnoreCase("fee_not_recieved")) {
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("global_fail"), resourceBundle.getString("fee_not_recieved")));
			} else if (retValue.equalsIgnoreCase("app_not_verified")) {
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("global_fail"), resourceBundle.getString("fee_not_recieved")));
			} else if (retValue.equalsIgnoreCase("prod_not_verified")) {
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global_fail"), resourceBundle.getString("prod_not_verified")));
			} else if (retValue.equalsIgnoreCase("valid_assign_moderator")) {
				facesContext.addMessage(null, new FacesMessage(resourceBundle.getString("valid_assign_moderator")));
			} else if (retValue.equalsIgnoreCase("valid_assign_reviewer")) {
				facesContext.addMessage(null, new FacesMessage(resourceBundle.getString("valid_assign_reviewer")));
			}*/

		} catch (Exception e) {
			e.printStackTrace();
			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("global_fail"), resourceBundle.getString("global_fail")));
		}
		getProcessProdBn().setTimeLine(new TimeLine());
		//return "/internal/processprodlist";  //To change body of created methods use File | Settings | File Templates.
	}

	public String nextStep(){
		ProdApplications prodApp = getProcessProdBn().getProdApplications();
		if(prodApp != null){
			RegState curRegState = prodApp.getRegState();
			switch (curRegState) {
			case SCREENING:
				backToSCREENING(prodApp);
				break;
			case REVIEW_BOARD:
				prodApplicationsServiceMZ.changeStateReviewInfo(getProcessProdBn().getProdApplications());
				break;
			}

			addTimeline();
		}
		return "/internal/processprodlist";
	}

	private void backToSCREENING(ProdApplications prodApp){
		prodApp.setModerator(null);
		prodApplicationsService.updateProdApp(prodApp, userSession.getLoggedINUserID());
	}

	public boolean getCanChangeModerator() {
		if(getProcessProdBn().getProdApplications() != null){
			RegState curRegState = getProcessProdBn().getProdApplications().getRegState();
			if(userSession.isAdmin() && !curRegState.equals(RegState.REGISTERED)
					&& !curRegState.equals(RegState.REJECTED)
					 && getProcessProdBn().getProdApplications().getModerator() != null)
				return true;

			if(userSession.isStaff() && !curRegState.equals(RegState.REGISTERED)
					&& !curRegState.equals(RegState.REJECTED)
					&& getProcessProdBn().getProdApplications().getModerator() != null){
				return true;
			}
		}
		return false;
	}

	public boolean getCanNextStep() {
		try {
			RegState curRegState = getProcessProdBn().getProdApplications().getRegState();

			if((userSession.isAdmin() || userSession.isModerator()) 
					&& !curRegState.equals(RegState.REGISTERED) && !curRegState.equals(RegState.REJECTED)
					&& !curRegState.equals(RegState.FOLLOW_UP))
				return true;
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public List<ProdAppLetter> getLetters() {
		return processProdBn.getLetters();
	}

	public String registerProduct(ProdApplications prodApplications) {
		facesContext = getCurrentInstance();
		try {
			if(prodApplications.getRegState().equals(RegState.REGISTERED))
				prodApplicationsServiceMZ.createRegCert(prodApplications, getGestorDeCTRM(), isGeneric());
			else{
				if (!prodApplications.getRegState().equals(RegState.RECOMMENDED)) {
					facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("global_fail"), resourceBundle.getString("register_fail")));
					return "";
				}

				if(prodApplications.getProdRegNo() == null || prodApplications.getProdRegNo().equals(""))
					prodApplications.setProdRegNo(RegistrationUtil.generateRegNo("" + 0, prodApplications.getProdAppNo()));

				prodApplications.setActive(true);
				prodApplications.setUpdatedBy(loggedInUser);

				String retValue = prodApplicationsServiceMZ.registerProd(prodApplications);
				if(retValue.equals("created")) {
					getProcessProdBn().setTimeLineList(null);
					prodApplicationsServiceMZ.createRegCert(prodApplications, getGestorDeCTRM(), isGeneric());
					facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global.success"), resourceBundle.getString("status_change_success")));
				}else{
					facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global_fail"), "Error registering the product"));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global_fail"), "Product registered but error generating certificate."));
		} catch (JRException e) {
			e.printStackTrace();
			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global_fail"), "Product registered but error generating certificate."));
		} catch (SQLException e) {
			e.printStackTrace();
			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global_fail"), "Product registered but error generating certificate."));
		} catch (Exception ex){
			ex.printStackTrace();
			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global_fail"), "Error registering the product"));
		}
		return null;
	}
	
	public String registerProductNA(ProdApplications prodApplications, String backTo) {
		this.registerProduct(prodApplications);
		
		if(backTo != null && !backTo.isEmpty())
			return backTo;
		return null;
		//return "/public/productlist";
	}

	public String rejectProduct(ProdApplications prodApplications) {
		prodApplications = getProdApplicationsService().findProdApplications(prodApplications.getId());
		facesContext = getCurrentInstance();
		if(prodApplications.getRegState().equals(RegState.REJECTED)){
			prodApplicationsServiceMZ.createRejectCert(prodApplications, getRejectSumm(), userSession.getLoggedINUserID());
		}else{
			if (!prodApplications.getRegState().equals(RegState.NOT_RECOMMENDED)) {
				facesContext.addMessage(null, new FacesMessage("Invalid operation!", resourceBundle.getString("Error.headNotReject")));
				return "";
			}

			prodApplications.setUpdatedBy(loggedInUser);

			String retValue = prodApplicationsServiceMZ.rejectedProd(prodApplications, getRejectSumm());
			if(retValue.equals("created")) {
				getProcessProdBn().setTimeLineList(null);
				prodApplicationsServiceMZ.createRejectCert(prodApplications, getRejectSumm(), userSession.getLoggedINUserID());
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global.success"), resourceBundle.getString("status_change_success")));
			}else{
				facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global_fail"), "Error rejecting the product"));
			}
		}
		return null;
	}

	public boolean isVisibleExecSumeryBtn() {
		// (userSession.moderator||userSession.admin||userSession.head)and userAccessMBean.detailReview
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

	public String executiveSummary(){
		String result = prodApplicationsServiceMZ.verificationBeforeComplete(processProdBn.getProdApplications(), userSession.getLoggedINUserID(), processProdBn.getReviewInfos());
		if (result.equals("ok")) {
			return "/internal/execsumm.faces";
		} else if (result.equals("state_error")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please accept the reviews before submitting the executive summary", ""));
			return null;
		} else if (result.equals("clinical_review")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Clinical review not received or verified.", ""));
			return null;
		} else if (result.equals("lab_status")) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lab result not verified.", ""));
			return null;
		}
		return "";
	}

	public String deleteLetter(ProdAppLetter let) {
		getProcessProdBn().getLetters().remove(let);
		facesContext = FacesContext.getCurrentInstance();
		try {
			prodApplicationsServiceMZ.deleteProdAppLetter(let);
			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, resourceBundle.getString("global.success"), resourceBundle.getString("del_letter_success")));
		} catch (Exception e) {
			facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resourceBundle.getString("global_fail"), resourceBundle.getString("del_letter_success")));
			e.printStackTrace();
		}
		return null;
	}

	public void setVisibleExecSumeryBtn(boolean visibleExecSumeryBtn) {
		this.visibleExecSumeryBtn = visibleExecSumeryBtn;
	}

	public ProdApplications findProdApplications() {
		return processProdBn.getProdApplications();
	}

	public ProdApplicationsServiceMZ getProdApplicationsServiceMZ(){
		return prodApplicationsServiceMZ;
	}

	public void setProdApplicationsServiceMZ(ProdApplicationsServiceMZ service){
		this.prodApplicationsServiceMZ = service;
	}

	public ProdApplicationsService getProdApplicationsService(){
		return prodApplicationsService;
	}

	public void setProdApplicationsService(ProdApplicationsService service){
		this.prodApplicationsService = service;
	}

	public ReviewService getReviewService() {
		return reviewService;
	}

	public void setReviewService(ReviewService reviewService) {
		this.reviewService = reviewService;
	}

	/*public SuspendServiceMZ getSuspendServiceMZ() {
		return suspendServiceMZ;
	}

	public void setSuspendService(SuspendServiceMZ suspendService) {
		this.suspendServiceMZ = suspendService;
	}*/

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ProcessProdBn getProcessProdBn() {
		return processProdBn;
	}

	public void setProcessProdBn(ProcessProdBn process) {
		this.processProdBn = process;
	}

	public String getGestorDeCTRM() {
		return gestorDeCTRM;
	}

	public void setGestorDeCTRM(String gestorDeCTRM) {
		this.gestorDeCTRM = gestorDeCTRM;
	}

	public String getRejectSumm() {
		return rejectSumm;
	}

	public void setRejectSumm(String rejectSumm) {
		this.rejectSumm = rejectSumm;
	}

	public boolean isGeneric() {
		return generic;
	}

	public void setGeneric(boolean generic) {
		this.generic = generic;
	}

	public boolean isDisableCheckSample() {
		disableCheckSample = false;
		if(processProdBn.getProdApplications() != null)
			if(processProdBn.getProdApplications().getRegState().ordinal() > 6)
				disableCheckSample = true;
		return disableCheckSample;
	}

	public void setDisableCheckSample(boolean checkSample) {
		this.disableCheckSample = checkSample;
	}

	public boolean isShowAssessment() {
		// userSession.moderator||userSession.head||userSession.admin
		ProdApplications prodApp = processProdBn.getProdApplications();
		if(userSession.isReviewer()){
			ReviewInfo revinfo = reviewService.findReviewInfoByUserAndProdApp(userSession.getLoggedINUserID(), prodApp.getId());
			showAssessment = (revinfo != null);
		}else if(userSession.isModerator() || userSession.isHead() || userSession.isAdmin())
			showAssessment = true;
		return showAssessment;
	}

	public void setShowAssessment(boolean showAssessment) {
		this.showAssessment = showAssessment;
	}
	
	public void onTabChange(TabChangeEvent event) {
		if(processProdBn.getProdApplications() == null)
			Scrooge.goToHome();
	}
	
	public List<SuspDetail> getSuspDetails() {
		suspDetails = getProcessProdBn().getSuspendServiceMZ().findSuspendByProd(getProcessProdBn().getProdApplications().getId());
		return suspDetails;
	}

	public void setSuspDetails(List<SuspDetail> list){
		suspDetails = list;
	}
	
	public boolean visibleSuspCancelMenu(){
		ProdApplications pApp = getProcessProdBn().getProdApplications();
		if (pApp != null) {
			if(userSession.isHead() || userSession.isAdmin()){
				if(pApp.getRegState().equals(RegState.SUSPEND) ||
						pApp.getRegState().equals(RegState.CANCEL))
					return true;
			}
		}
		return false;
	}
	
	public String revertToRegistered(){
		ProdApplications pApp = getProcessProdBn().getProdApplications();
		if (pApp != null) {
			RecomendType recType = RecomendType.SUSPEND;
			if(pApp.getRegState().equals(RegState.SUSPEND))
				recType = RecomendType.SUSPEND;
			else if(pApp.getRegState().equals(RegState.CANCEL))
				recType = RecomendType.CANCEL;
			
			pApp.setRegState(RegState.REGISTERED);
			getProcessProdBn().getSuspendServiceMZ().setCompletedPrevSuspDetail(pApp.getId(), recType);
			prodApplicationsService.createTimeLine(RegState.REGISTERED, pApp);
			prodApplicationsService.updateProdApp(pApp, userSession.getLoggedINUserID());
		}
		return "/internal/processcancellist.faces" + "?faces-redirect=true";
	}
}
