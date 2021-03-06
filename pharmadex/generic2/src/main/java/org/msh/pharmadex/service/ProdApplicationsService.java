package org.msh.pharmadex.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.dao.ApplicantDAO;
import org.msh.pharmadex.dao.CountryDAO;
import org.msh.pharmadex.dao.ProdApplicationsDAO;
import org.msh.pharmadex.dao.ProductDAO;
import org.msh.pharmadex.dao.iface.AppointmentDAO;
import org.msh.pharmadex.dao.iface.AtcDAO;
import org.msh.pharmadex.dao.iface.ChecklistDAO;
import org.msh.pharmadex.dao.iface.FileTemplateDAO;
import org.msh.pharmadex.dao.iface.ForeignAppStatusDAO;
import org.msh.pharmadex.dao.iface.ProdAppChecklistDAO;
import org.msh.pharmadex.dao.iface.ProdAppLetterDAO;
import org.msh.pharmadex.dao.iface.RevDeficiencyDAO;
import org.msh.pharmadex.dao.iface.ReviewInfoDAO;
import org.msh.pharmadex.dao.iface.StatusUserDAO;
import org.msh.pharmadex.dao.iface.WorkspaceDAO;
import org.msh.pharmadex.domain.AgentAgreement;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.Checklist;
import org.msh.pharmadex.domain.Country;
import org.msh.pharmadex.domain.FileTemplate;
import org.msh.pharmadex.domain.ForeignAppStatus;
import org.msh.pharmadex.domain.ProdAppChecklist;
import org.msh.pharmadex.domain.ProdAppLetter;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.RevDeficiency;
import org.msh.pharmadex.domain.ReviewInfo;
import org.msh.pharmadex.domain.StatusUser;
import org.msh.pharmadex.domain.TimeLine;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.Workspace;
import org.msh.pharmadex.domain.enums.LetterType;
import org.msh.pharmadex.domain.enums.PaymentStatus;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.domain.enums.ReviewStatus;
import org.msh.pharmadex.domain.enums.UseCategory;
import org.msh.pharmadex.mbean.product.ProdTable;
import org.msh.pharmadex.util.RetObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * Created by IntelliJ IDEA.
 * User: usrivastava
 * Date: 1/11/12
 * Time: 11:48 PM
 * To change this template use File | Settings | File Templates.
 */
@Service
@Transactional
public class ProdApplicationsService implements Serializable {

	private static final long serialVersionUID = 5061629028904167436L;
	@Resource
	ProdApplicationsDAO prodApplicationsDAO;

	@Autowired
	UserService userService;
	@Autowired
	ApplicantDAO applicantDAO;
	@Autowired
	ProductDAO productDAO;
	@Autowired
	AtcDAO atcDAO;
	@Autowired
	AppointmentDAO appointmentDAO;
	@Autowired
	WorkspaceDAO workspaceDAO;
	@Autowired
	ProdAppChecklistDAO prodAppChecklistDAO;
	@Autowired
	ChecklistDAO checklistDAO;
	@Autowired
	ForeignAppStatusDAO foreignAppStatusDAO;
	ProdApplications prodApp;
	Product product;
	@Autowired
	private RevDeficiencyDAO revDeficiencyDAO;
	@Autowired
	private CountryDAO countryDAO;
	@Autowired
	private ProdAppLetterDAO prodAppLetterDAO;
	private List<ProdApplications> prodApplications;
	@Autowired
	private StatusUserDAO statusUserDAO;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private TimelineService timelineService;
	@Autowired
	private ReviewInfoDAO reviewInfoDAO;

	@Autowired
	private UtilsByReports utilsByReports;

	@Autowired
	ApplicantService applicantService;
	
	@Autowired
	FileTemplateDAO fileTemplateDAO;

	public ApplicantService getApplicantService() {
		return applicantService;
	}

	public void setApplicantService(ApplicantService applicantService) {
		this.applicantService = applicantService;
	}

	@Transactional
	public ProdApplications findProdApplications(Long id) {
		if (id == null) {
			return null;
		}
		ProdApplications prodApp = prodApplicationsDAO.findProdApplications(id);
		if(prodApp != null){
			Hibernate.initialize(prodApp.getProduct());
			Hibernate.initialize(prodApp.getApplicant());
			Hibernate.initialize(prodApp.getApplicantUser());
			
			Hibernate.initialize(prodApp.getAmendment());
			Hibernate.initialize(prodApp.getReviewInfos());
		}
		return prodApp;
	}

	public List<RegState> nextStepOptions(RegState regState, UserSession userSession, boolean reviewStatus) {
		RegState[] options = null;
		switch (regState) {
		case NEW_APPL:
			options = new RegState[2];
			options[0] = RegState.FOLLOW_UP;
			options[1] = RegState.FEE;
			break;
		case FEE:
			options = new RegState[2];
			options[0] = RegState.FOLLOW_UP;
			options[1] = RegState.VERIFY;
			break;
		case VERIFY:
			options = new RegState[2];
			options[0] = RegState.FOLLOW_UP;
			options[1] = RegState.SCREENING;
			break;
		case SCREENING:
			if (!userSession.isStaff()) {
				options = new RegState[2];
				options[1] = RegState.REVIEW_BOARD;
			} else {
				options = new RegState[1];
			}
			options[0] = RegState.FOLLOW_UP;
			break;
		case REVIEW_BOARD:
			if (userSession.isAdmin() || userSession.isModerator()) {
				if (reviewStatus) {
					options = new RegState[3];
					options[0] = RegState.FOLLOW_UP;
					options[1] = RegState.RECOMMENDED;
					options[2] = RegState.NOT_RECOMMENDED;
				} else {
					options = new RegState[1];
					options[0] = RegState.FOLLOW_UP;
				}
			} else {
				options = new RegState[1];
				options[0] = RegState.FOLLOW_UP;
			}
			break;
		case RECOMMENDED:
			if (userSession.isAdmin() || userSession.isModerator() || userSession.isHead()) {
				options = new RegState[1];
				options[0] = RegState.FOLLOW_UP;
				options[0] = RegState.REJECTED;
			}
			break;
		case REGISTERED:
			options = new RegState[3];
			options[0] = RegState.DISCONTINUED;
			options[1] = RegState.XFER_APPLICANCY;
			break;
		case FOLLOW_UP:
			options = new RegState[7];
			options[0] = RegState.FEE;
			options[1] = RegState.VERIFY;
			options[2] = RegState.SCREENING;
			options[3] = RegState.REVIEW_BOARD;
			options[4] = RegState.SCREENING;
			options[5] = RegState.REVIEW_BOARD;
			options[6] = RegState.DEFAULTED;
			break;
		case NOT_RECOMMENDED:
			options = new RegState[1];
			options[0] = RegState.REJECTED;
			break;
		}
		return Arrays.asList(options);


	}

	public List<ProdApplications> getApplications() {
		if (prodApplications == null)
			prodApplications = prodApplicationsDAO.allProdApplications();
		return prodApplications;
	}

	public void refresh() {
		prodApplications = null;
	}

	public List<ProdApplications> getSavedApplications(Long userId) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<RegState> regState = new ArrayList<RegState>();
		regState.add(RegState.SAVED);
		params.put("regState", regState);
		params.put("userId", userId);
		params.put("createdBy", userId);
		return prodApplicationsDAO.getProdAppByParams(params);
	}

	public ArrayList<ProdApplications> findExpiringProd() {
		Calendar currDate = Calendar.getInstance();
		currDate.add(Calendar.MONTH, 1);
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("startDt", currDate.getTime());
		currDate.add(Calendar.MONTH, 1);
		params.put("endDt", currDate.getTime());

		ArrayList<ProdApplications> prodApps = prodApplicationsDAO.findProdExpiring(params);
		return prodApps;
	}

	public ArrayList<ProdApplications> findExpiredProd() {
		Calendar currDate = Calendar.getInstance();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("regExpDate", currDate.getTime());
		ArrayList<ProdApplications> prodApps = (ArrayList<ProdApplications>) prodApplicationsDAO.getProdAppByParams(params);


		return prodApps;
	}

	/**
	 * Applications are in states by role users
	 * They do not have other conditionals
	 */
	public List<ProdApplications> getSubmittedApplications(UserSession userSession) {
		List<ProdApplications> prodApplicationses = null;
		HashMap<String, Object> params = new HashMap<String, Object>();

		List<RegState> regState = new ArrayList<RegState>();
		if (userSession.isAdmin()) {
			regState.add(RegState.FEE);
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.DEFAULTED);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.SCREENING);
			regState.add(RegState.VERIFY);
		}
		if (userSession.isModerator()) {
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.SCREENING);
			regState.add(RegState.VERIFY);
			regState.add(RegState.REVIEW_BOARD);
			//params.put("moderatorId", userSession.getLoggedINUserID());
		}
		if (userSession.isReviewer()) {
			regState.add(RegState.REVIEW_BOARD);
			/*if (workspaceDAO.findAll().get(0).isDetailReview()) {
				params.put("reviewer", userSession.getLoggedINUserID());
				return prodApplicationsDAO.findProdAppByReviewer(params);
			} else
				params.put("reviewerId", userSession.getLoggedINUserID());*/
		}
		if (userSession.isHead()) {
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.FEE);
			regState.add(RegState.VERIFY);
			regState.add(RegState.SCREENING);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.NOT_RECOMMENDED);
			regState.add(RegState.REJECTED);
		}
		if (userSession.isStaff()) {
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.SCREENING);
			regState.add(RegState.FEE);
			regState.add(RegState.FOLLOW_UP);
		}
		if (userSession.isCompany()) {
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.FEE);
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.DEFAULTED);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.SCREENING);
			regState.add(RegState.VERIFY);
			regState.add(RegState.REGISTERED);
			regState.add(RegState.CANCEL);
			regState.add(RegState.SUSPEND);
			regState.add(RegState.DEFAULTED);
			regState.add(RegState.NOT_RECOMMENDED);
			regState.add(RegState.REJECTED);
			params.put("userId", userSession.getLoggedINUserID());
		}
		if (userSession.isLab()) {
			regState.add(RegState.VERIFY);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.NOT_RECOMMENDED);
		}
		if (userSession.isClinical()) {
			regState.add(RegState.VERIFY);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.FOLLOW_UP);
			params.put("prodAppType", ProdAppType.NEW_CHEMICAL_ENTITY);
		}

		params.put("regState", regState);
		prodApplicationses = prodApplicationsDAO.getProdAppByParams(params);

		if (userSession.isModerator() || userSession.isReviewer() || userSession.isHead() || userSession.isLab()) {
			Collections.sort(prodApplicationses, new Comparator<ProdApplications>() {
				@Override
				public int compare(ProdApplications o1, ProdApplications o2) {
					Date d1 = o1.getPriorityDate();
					Date d2 = o2.getPriorityDate();
					if(d1 != null && d2 != null)
						return d1.compareTo(d2);
					return 0;
				}
			});
		}

		return prodApplicationses;
	}

	@Transactional
	public ProdApplications saveApplication(ProdApplications prodApplications, Long loggedInUserID) {
		if (prodApplications == null || loggedInUserID == null)
			return null;
		User loggedInUserObj = userService.findUser(loggedInUserID);

		if (prodApplications.getProduct().getId() == null) {
			prodApplications.getProduct().setCreatedBy(loggedInUserObj);
			prodApplications.setCreatedBy(loggedInUserObj);
			prodApplications.setSubmitDate(new Date());
		} else {
			prodApplications.setUpdatedDate(new Date());
		}

		if (prodApplications.getAppointment() != null)
			appointmentDAO.save(prodApplications.getAppointment());

		prodApplications = prodApplicationsDAO.updateApplication(prodApplications);
		return prodApplications;
	}

	@Transactional
	public RetObject updateProdApp(ProdApplications prodApplications, Long loggedInUserID) {
		RetObject retObject;
		User loggedInUser = userService.findUser(loggedInUserID);
		if (prodApplications == null) {
			retObject = new RetObject("empty_prodApp", null);
		}
		if (prodApplications.getProduct() == null) {
			retObject = new RetObject("empty_product", null);
		}

		try {
			prodApplications.setUpdatedDate(new Date());
			prodApplications.setUpdatedBy(loggedInUser);
			if (prodApplications.getProduct().getId() == null) {
				productDAO.saveProduct(prodApplications.getProduct());
			}
			if (prodApplications.getId() == null) {
				Applicant appl = prodApplications.getApplicant();
				prodApplications.setApplicant(applicantDAO.findApplicant(appl.getApplcntId()));

				// change responsible in Applicant
				User applUs = prodApplications.getApplicantUser();
				if(appl != null && applUs != null){
					if(applUs.getUsername() != null){
						appl.setContactName(prodApplications.getApplicantUser().getUsername());
						//applicantDAO.saveApplicant(appl);
						applicantDAO.updateApplicantResp(appl);
					}
				}
				prodApplicationsDAO.saveApplication(prodApplications);
			} else {
				prodApplications = prodApplicationsDAO.updateApplication(prodApplications);
			}
			retObject = new RetObject("persist", prodApplications);
			return retObject;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new RetObject(ex.getMessage(), null);
		}
	}

	/**
	 * It changes product in renew and variation application (data from temporary product in
	 * renew and var changes data in primary application)
	 */
	public boolean replaceProduct(ProdApplications prodApp,Long loggedInUserID){
		if (!(prodApp.getProdAppType()==ProdAppType.RENEW)||(prodApp.getProdAppType()==ProdAppType.VARIATION)) return false;
		if (prodApp.getProdAppNo()==null) return false;
		ProdApplications parentApp = findProdApplications(prodApp.getParentApplication().getId());
		Product product = prodApp.getProduct();
		parentApp.setProduct(product);
		updateProdApp(parentApp,loggedInUserID);
		return true;
	}



	@Transactional
	public List<ProdAppChecklist> findAllProdChecklist(Long prodAppId) {
		return prodAppChecklistDAO.findByProdApplications_IdOrderByIdAsc(prodAppId);
	}

	@Transactional
	public List<Checklist> findAllChecklist() {
		return (List<Checklist>) checklistDAO.findAll();
	}

	@Transactional
	public List<ProdApplications> findProdApplicationByProduct(Long id) {
		return prodApplicationsDAO.findProdApplicationByProduct(id);
	}

	@Transactional
	public ProdApplications findActiveProdAppByProd(Long prodID){
		return prodApplicationsDAO.findActiveProdAppByProd(prodID);
	}

	public StatusUser findStatusUser(Long prodAppId) {
		return statusUserDAO.findByProdApplications_Id(prodAppId);
	}

	public String saveProcessors(StatusUser module) {
		statusUserDAO.saveAndFlush(module);
		return "success";
	}

	public List<ProdApplications> findPayNotified() {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("paymentStatus", PaymentStatus.PAID);
		return prodApplicationsDAO.findPendingRenew(params);
	}

	public JasperPrint initRegCert(){
		product = productDAO.findProduct(prodApp.getProduct().getId());

		System.out.println("product found");
		String regDt = DateFormat.getDateInstance().format(prodApp.getRegistrationDate());
		String expDt = DateFormat.getDateInstance().format(prodApp.getRegExpiryDate());

		URL resource = getClass().getResource("/reports/reg_letter.jasper");
		System.out.println("resource found");
		HashMap param = new HashMap();
		param.put("prodappid", prodApp.getId());
		String fullNo = prodApp.getProdRegNo();
		if (fullNo==null) {
			String certNo = "0000000000" + String.valueOf(prodApp.getId()) + String.valueOf(prodApp.getProduct().getId());
			certNo = certNo.substring(certNo.length() - 10, certNo.length());
			fullNo = prodApp.getProdAppType() + "/" + certNo;
		}
		//param.put("containerType",prodApp.getProduct().getContType());
		param.put("cert_no",fullNo);
		param.put("productDescription",prodApp.getProduct().getProdDesc());
		List<UseCategory> cats = product.getUseCategories();
		String catStr="";
		for(UseCategory cat:cats){
			if (!"".equals(cat)){
				catStr = cat.name();
			}else{
				catStr = catStr +"," + cat.name();
			}
		}
		param.put("prescription",catStr.toLowerCase());
		System.out.println("params filled");
		JasperPrint result = null;
		try {
			Connection conn = entityManager.unwrap(Session.class).connection();
			System.out.println("connection ready");
			result = JasperFillManager.fillReport(resource.getFile(), param, conn);
			//result = JasperFillManager.fillReport(resource.getFile(), param, new JREmptyDataSource());
			System.out.println("filled report");
		} catch (JRException e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * @deprecated
	 * @param summary
	 * @return
	 * @throws JRException
	 */
	@Deprecated
	public JasperPrint initRejCert(String summary) throws JRException {
		URL resource = getClass().getResource("/reports/rejection_letter.jasper");

		HashMap<String, Object> param = new HashMap<String, Object>();
		utilsByReports.init(param, prodApp, product);
		utilsByReports.putNotNull(UtilsByReports.KEY_APPNAME, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_PRODNAME, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_PRODSTRENGTH, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_DOSFORM, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_MANUFNAME, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_APPTYPE, "New Medicine Registration", true);
		utilsByReports.putNotNull(UtilsByReports.KEY_SUBJECT, "Rejection Letter  ", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_ADDRESS1, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_ADDRESS2, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_COUNTRY, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_APPNUMBER, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_BODY, summary, true);

		param.put("date", new Date());

		return JasperFillManager.fillReport(resource.getFile(), param);
	}

	
	 public String createRejectCert(ProdApplications prodApp, String summary) {
		this.prodApp = prodApp;
		this.product = prodApp.getProduct();
		try {
			//            invoice.setPaymentStatus(PaymentStatus.INVOICE_ISSUED);???
			File invoicePDF = File.createTempFile("" + product.getProdName() + "_invoice", ".pdf");
			JasperPrint jasperPrint = initRejCert(summary);
			net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(invoicePDF));
			prodApp.setRejCert(IOUtils.toByteArray(new FileInputStream(invoicePDF)));
			prodApplicationsDAO.updateApplication(prodApp);
		} catch (JRException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.???
			return "error";
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.???
			// return "error";???
		}
		return "created";
	}
	 

	@Transactional
	public String registerProd(ProdApplications prodApp) {
		this.prodApp = prodApp;
		this.product = prodApp.getProduct();


		List<ProdApplications> prodApps;
		ProdApplications proda = null;
		try {
			TimeLine timeLine = new TimeLine();
			if (prodApp.getProdAppType().equals(ProdAppType.RENEW) || prodApp.getProdAppType().equals(ProdAppType.VARIATION)) {
				proda=prodApp.getParentApplication();
				if (proda!=null)
					prodApplicationsDAO.moveToArchive(proda,prodApp.getRegistrationDate());

			}
			timeLine.setRegState(RegState.REGISTERED);
			timeLine.setProdApplications(prodApp);
			timeLine.setStatusDate(new Date());
			timeLine.setUser(prodApp.getUpdatedBy());
			prodApp.setRegState(timeLine.getRegState());

			prodApplicationsDAO.updateApplication(prodApp);
			timelineService.saveTimeLine(timeLine);

			return "created";
		}catch  (Exception ex){
			ex.printStackTrace();
			return "error";
		}
	}

	public void createTimeLine(RegState regSt, ProdApplications prApp){
		TimeLine timeLine = new TimeLine();
		timeLine.setRegState(regSt);
		timeLine.setProdApplications(prApp);
		timeLine.setStatusDate(new Date());
		timeLine.setUser(prApp.getUpdatedBy());
		timelineService.saveTimeLine(timeLine);
	}
	/**
	 * create TimeLine - RegState REJECTED, 
	 * @param _prodApp -ProdApplications 
	 * @param com -  head comment
	 * @return "created" if success, else - "error"
	 */
	@Transactional
	public String rejectProd(ProdApplications _prodApp, String com) {
		this.prodApp = _prodApp;
		this.product = prodApp.getProduct();

		try {
			TimeLine timeLine = new TimeLine();
			timeLine.setRegState(RegState.REJECTED);
			timeLine.setProdApplications(prodApp);
			timeLine.setStatusDate(new Date());
			timeLine.setUser(prodApp.getUpdatedBy());
			timeLine.setComment(com);
			prodApp.setRegState(timeLine.getRegState());

			prodApplicationsDAO.updateApplication(prodApp);
			timelineService.saveTimeLine(timeLine);

			return "created";
		}catch  (Exception ex){
			ex.printStackTrace();
			return "error";
		}
	}

	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String createRegCert(ProdApplications prodApp) {
		this.prodApp = prodApp;
		this.product = prodApp.getProduct();
		File invoicePDF = null;
		try {
			invoicePDF = File.createTempFile("" + product.getProdName() + "_registration", ".pdf");
			JasperPrint jasperPrint = initRegCert();
			if (jasperPrint==null)
				throw new JRException("Error during creation of certificate");
			net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(invoicePDF));
			prodApp.setRegCert(IOUtils.toByteArray(new FileInputStream(invoicePDF)));
			prodApplicationsDAO.updateApplication(prodApp);
			return "created";
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JRException e) {
			e.printStackTrace();
		}
		return "";
	}


	public ProdApplications getProdApp() {
		return prodApp;
	}

	public void setProdApp(ProdApplications prodApp) {
		this.prodApp = prodApp;
	}

	public String removeForeignAppStatus(ForeignAppStatus foreignAppStatus) {
		foreignAppStatusDAO.delete(foreignAppStatus);
		return "removed";
	}

	public List<ProdApplications> findSavedApps(Long loggedInUserID) {
		return prodApplicationsDAO.findSavedProdApp(loggedInUserID);
	}

	public Long findApplicationCount() {
		return prodApplicationsDAO.findApplicationCount();

	}

	public RetObject saveForeignAppStatus(ForeignAppStatus selForeignAppStatus) {
		RetObject retObject = new RetObject();
		try {
			Long id = selForeignAppStatus.getCountry().getId();
			Country country = countryDAO.find(id);
			selForeignAppStatus.setCountry(country);
			selForeignAppStatus = foreignAppStatusDAO.save(selForeignAppStatus);
			retObject.setObj(selForeignAppStatus);
			retObject.setMsg("persist");
		} catch (Exception ex) {
			ex.printStackTrace();
			retObject.setObj(null);
			retObject.setMsg(ex.getMessage());
		}
		return retObject;
	}

	public List<ForeignAppStatus> findForeignAppStatus(Long prodAppID) {
		if (prodAppID==null) return null;
		return foreignAppStatusDAO.findByProdApplications_Id(prodAppID);


	}

	public String saveProdAppChecklists(List<ProdAppChecklist> prodAppChecklists) {
		try {
			prodAppChecklistDAO.save(prodAppChecklists);
			return "persist";
		} catch (Exception ex) {
			ex.printStackTrace();
			return "error";
		}

	}

	@Transactional
	public String submitExecSummary(ProdApplications prodApplications, Long loggedInUser, List<ReviewInfo> reviewInfos) {
		try {
			if (reviewInfos == null || prodApplications == null || loggedInUser == null)
				return "empty";

			boolean complete = false;
			for (ReviewInfo reviewInfo : reviewInfos) {
				if (!reviewInfo.getReviewStatus().equals(ReviewStatus.ACCEPTED)) {
					complete = false;
					return "state_error";
				} else {
					complete = true;
				}
			}

			if (prodApplications.getProdAppType().equals(ProdAppType.NEW_CHEMICAL_ENTITY)) {
				if (!prodApplications.isClinicalRevReceived() || !prodApplications.isClinicalRevVerified() || prodApplications.getcRevAttach() == null) {
					complete = false;
					return "clinical_review";
				}
			}

			/*28.09.2017 #2708 
				if (prodApplications.getProdAppType()!=ProdAppType.RENEW) {
				if (prodApplications.getSampleTestRecieved() == null || !prodApplications.getSampleTestRecieved()) {
					return "lab_status";
				}
			}*/

			if (complete) {
				saveApplication(prodApplications, loggedInUser);
				return "persist";
			} else {
				return "state_error";
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "error";
		}
	}

	public List<RevDeficiency> findRevDefByRI(ReviewInfo reviewInfo) {
		return revDeficiencyDAO.findByReviewInfo_Id(reviewInfo.getId());
	}

	public ArrayList<RevDeficiency> findOpenRevDefByPA(Long prodAppID){
		List<ReviewInfo> revInfos =  reviewInfoDAO.findByProdApplications_IdOrderByAssignDateAsc(prodAppID);
		ArrayList<RevDeficiency> activeDefs = new ArrayList<RevDeficiency>();

		for(ReviewInfo ri : revInfos){
			if(ri.getReviewStatus().equals(ReviewStatus.FIR_SUBMIT)){
				for(RevDeficiency rd : ri.getRevDeficiencies()){
					if(rd.getAckDate()==null)
						activeDefs.add(rd);
				}
			}
		}
		return activeDefs;
	}

	public List<ProdAppLetter> findAllLettersByProdApp(Long id, boolean isCompany) {
		List<ProdAppLetter> result = new ArrayList<ProdAppLetter>();
		if(isCompany){
			// get letter, template.staffOnly != true
			List<ProdAppLetter> listAll = prodAppLetterDAO.findByProdApplications_Id(id);
			if(listAll != null && listAll.size() > 0){
				for(ProdAppLetter letter:listAll){
					if(letter.getTemplateType() != null){
						List<FileTemplate> list = fileTemplateDAO.findByTemplateType(letter.getTemplateType());
						if(list != null && list.size() > 0){
							FileTemplate tmpl = list.get(0);
							if(tmpl.getStaffOnly() != null && tmpl.getStaffOnly()){
								
							}else// NULL or false
								result.add(letter);
						}
					}
				}
			}
		}else{
			// get All by this prodApp
			result = prodAppLetterDAO.findByProdApplications_Id(id);
		}
		return result;
	}

	public RetObject submitProdApp(ProdApplications prodApplications, Long loggedINUserID) {
		RetObject retObject;
		try {
			retObject = updateProdApp(prodApplications, loggedINUserID);
			this.prodApp = (ProdApplications) retObject.getObj();
			return retObject;
		} catch (Exception ex) {
			ex.printStackTrace();
			retObject = new RetObject("error", ex.getMessage());
			return retObject;
		}
	}
	 /** Create application acknowledgement letter and store it to letters!
	 * @param prodApp current prodapplication
	 * @return persist or error
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String createAckLetter(ProdApplications prodApp) {
		Product prod = productDAO.findProduct(prodApp.getProduct().getId());
		Workspace workspace = workspaceDAO.findAll().get(0);
		try {
			File invoicePDF = File.createTempFile("" + prod.getProdName() + "_ack", ".pdf");

			JasperPrint jasperPrint;
			Connection conn = entityManager.unwrap(Session.class).connection();

			HashMap<String, Object> param = new HashMap<String, Object>();
			utilsByReports.init(param, prodApp, prod);
			utilsByReports.putNotNull(UtilsByReports.KEY_ID, "", false);

			String subj1 = "Product Registration application for  " + (prod != null?prod.getProdName():"") + " recieved";
			utilsByReports.putNotNull(UtilsByReports.KEY_SUBJECT_1, subj1, true);

			utilsByReports.putNotNull(UtilsByReports.KEY_MANUFNAME, "", false);
			utilsByReports.putNotNull(UtilsByReports.KEY_SUBJECT, "Product application deficiency letter for  ", false);
			utilsByReports.putNotNull(UtilsByReports.KEY_REGISTRAR, workspace.getRegistrarName(), true);
			
			URL resource = getClass().getClassLoader().getResource("/reports/letter.jasper");
			if(resource != null){
				jasperPrint = JasperFillManager.fillReport(resource.getFile(), param, conn);
				jasperPrint.removePage(1);
				net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(invoicePDF));
				byte[] file = IOUtils.toByteArray(new FileInputStream(invoicePDF));

				createProdAppLetter(prodApp, file, invoicePDF.getName(), "Acknowledgement Letter", "Automatically generated Letter", LetterType.ACK_SUBMITTED);
				conn.close();
				return "persist";
			}
			return "error";
		} catch (JRException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return "error";
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return "error";
		} catch (SQLException e) {
			e.printStackTrace();
			return "error";
		}
	}
	
	/*@Transactional(propagation = Propagation.REQUIRES_NEW)	
	public String createAckLetter(ProdApplications prodApp) {
		Product prod = productDAO.findProduct(prodApp.getProduct().getId());
		Workspace workspace = workspaceDAO.findAll().get(0);
		try {
			File invoicePDF = File.createTempFile("" + prod.getProdName() + "_ack", ".pdf");

			JasperPrint jasperPrint;
			Connection conn = entityManager.unwrap(Session.class).connection();

			HashMap<String, Object> param = new HashMap<String, Object>();
			utilsByReports.init(param, prodApp, prod);
			utilsByReports.putNotNull(UtilsByReports.KEY_ID, "", false);

			String subj1 = "Product Registration application for  " + (prod != null?prod.getProdName():"") + " recieved";
			utilsByReports.putNotNull(UtilsByReports.KEY_SUBJECT_1, subj1, true);

			utilsByReports.putNotNull(UtilsByReports.KEY_MANUFNAME, "", false);
			utilsByReports.putNotNull(UtilsByReports.KEY_SUBJECT, "Product application deficiency letter for  ", false);
			utilsByReports.putNotNull(UtilsByReports.KEY_REGISTRAR, workspace.getRegistrarName(), true);
			
			URL resource = getClass().getClassLoader().getResource("/reports/letter.jasper");
			if(resource != null){
				jasperPrint = JasperFillManager.fillReport(resource.getFile(), param, conn);
				jasperPrint.removePage(1);
				net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(invoicePDF));
				byte[] file = IOUtils.toByteArray(new FileInputStream(invoicePDF));

				createProdAppLetter(prodApp, file, invoicePDF.getName(), "Acknowledgement Letter", "Automatically generated Letter", LetterType.ACK_SUBMITTED);
				conn.close();
				return "persist";
			}
			return "error";
		} catch (JRException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return "error";
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return "error";
		} catch (SQLException e) {
			e.printStackTrace();
			return "error";
		}
	}
*/
	public void createProdAppLetter(ProdApplications prApps, byte[] file, String fName, String title, String comment, LetterType ltype){
		ProdAppLetter attachment = new ProdAppLetter();
		attachment.setRegState(prApps.getRegState());
		attachment.setFile(file);
		attachment.setProdApplications(prApps);
		attachment.setFileName(fName);
		attachment.setTitle(title);
		attachment.setUploadedBy(prApps.getCreatedBy());
		attachment.setComment(comment);
		attachment.setContentType("application/pdf");
		attachment.setLetterType(ltype);
		prodAppLetterDAO.save(attachment);
	}

	public ProdAppLetter findAllLettersByProdAppAndType(ProdApplications prodApplications, LetterType ackSubmitted) {
		List<ProdAppLetter> prodAppLetters = prodAppLetterDAO.findByProdApplications_IdAndLetterType(prodApplications.getId(), ackSubmitted);
		if (prodAppLetters != null && prodAppLetters.size() > 0)
			return prodAppLetters.get(0);
		else
			return null;


	}

	public List<ProdApplications> findProdAppByAppNo(String prodAppNo) {
		if(prodAppNo==null||prodAppNo.equals(""))
			return null;

		return prodApplicationsDAO.findProdAppByNo(prodAppNo);
	}

	public List<ProdApplications> getProcessProdAppList(UserSession userSession) {
		List<ProdApplications> prodApplicationses = null;
		HashMap<String, Object> params = new HashMap<String, Object>();

		List<RegState> regState = new ArrayList<RegState>();
		if (userSession.isAdmin()) {
			regState.add(RegState.FEE);
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.DEFAULTED);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.SCREENING);
			regState.add(RegState.VERIFY);
		}
		if (userSession.isModerator()) {
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.SCREENING);
			regState.add(RegState.VERIFY);
			regState.add(RegState.REVIEW_BOARD);
		}
		if (userSession.isReviewer()) {
			regState.add(RegState.REVIEW_BOARD);
			if (workspaceDAO.findAll().get(0).isDetailReview()) {
				params.put("regState", regState);
				params.put("reviewer", userSession.getLoggedINUserID());
				return prodApplicationsDAO.findProdAppByReviewer(params);
			} else
				params.put("reviewerId", userSession.getLoggedINUserID());
		}
		if (userSession.isHead()) {
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.FEE);
			regState.add(RegState.VERIFY);
			regState.add(RegState.SCREENING);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.NOT_RECOMMENDED);
			regState.add(RegState.REJECTED);
		}
		if (userSession.isStaff()) {
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.SCREENING);
			regState.add(RegState.FEE);
			regState.add(RegState.FOLLOW_UP);
		}
		if (userSession.isCompany()) {
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.FEE);
			regState.add(RegState.NEW_APPL);
			regState.add(RegState.DEFAULTED);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.SCREENING);
			regState.add(RegState.VERIFY);
			regState.add(RegState.REGISTERED);
			regState.add(RegState.CANCEL);
			regState.add(RegState.SUSPEND);
			regState.add(RegState.DEFAULTED);
			regState.add(RegState.NOT_RECOMMENDED);
			regState.add(RegState.REJECTED);
			params.put("userId", userSession.getLoggedINUserID());
		}
		if (userSession.isLab()) {
			regState.add(RegState.VERIFY);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.FOLLOW_UP);
			regState.add(RegState.RECOMMENDED);
			regState.add(RegState.NOT_RECOMMENDED);
		}
		if (userSession.isClinical()) {
			regState.add(RegState.VERIFY);
			regState.add(RegState.REVIEW_BOARD);
			regState.add(RegState.FOLLOW_UP);
			params.put("prodAppType", ProdAppType.NEW_CHEMICAL_ENTITY);
		}

		params.put("regState", regState);
		prodApplicationses = prodApplicationsDAO.getProdAppByParams(params);

		if (userSession.isModerator() || userSession.isReviewer() || userSession.isHead() || userSession.isLab()) {
			Collections.sort(prodApplicationses, new Comparator<ProdApplications>() {
				@Override
				public int compare(ProdApplications o1, ProdApplications o2) {
					Date d1 = o1.getPriorityDate();
					Date d2 = o2.getPriorityDate();
					if(d1 != null && d2 != null)
						return d1.compareTo(d2);
					return 0;
				}
			});
		}

		return prodApplicationses;
	}

	@Transactional   
	public List<ProdApplications> getAllAncestor(ProdApplications proda) {
		ProdApplications an=proda;
		List<ProdApplications> ans = null;
		if (an.getParentApplication()!=null){
			ans=new ArrayList<ProdApplications> ();
			while (an!=null) {
				if (an.getParentApplication()==null)  return ans;
				an=	prodApplicationsDAO.findProdApplications(an.getParentApplication().getId());
				if (an!=null){
					Hibernate.initialize(an);
					ans.add((ProdApplications) an);
					an=an.getParentApplication();
				}
			}
		}
		return ans;
	}
	/**
	 * Really Screening number should be generated before app number, but historically.. don't mind
	 * @param prodAppNo
	 * @return
	 */
	public String getSrcNumber(String prodAppNo) {
		String number = "";
		if(!"".equals(prodAppNo)){		
			if(!"".equals(prodAppNo)){	
				if(prodAppNo.contains("/")){
					String [] n =   prodAppNo.split("/");
					if(n.length>2){
						number ="S"+n[3]+"/"+n[0];
					}	
				}else{
					number = prodAppNo.substring(2,prodAppNo.length());
					if(number.contains("-"))
						number = "S"+number.replace("-", "/");
				}

			}
		}
		return number;
	}
	/**
	 * Nverland specific app num gen, only if number is absent!
	 * @param prodApp
	 * @return
	 */
	public String generateAppNo(ProdApplications prodApp) {
		if(prodApp.getProdAppNo() == null){
			Calendar cal = GregorianCalendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			Long serial = prodApplicationsDAO.findFirstIdForYear(year);
			Long num =  new Long(1);
			if(!serial.equals(new Long(0))){
				num=prodApp.getId() - serial+1;
			}
			String numStr = "0000"+num.toString();
			numStr = numStr.substring(numStr.length()-4);
			return year+"-"+ numStr;
		}else{
			return prodApp.getProdAppNo();
		}

	}
	/**
	 * Year/pharmacological classification number/serial(0000)
	 */
	public static String generateRegNo(String prodAppNo, String classificationNo) {
		String number = "";
		if(!"".equals(prodAppNo)){
			if(prodAppNo.contains("/")){
				String [] n =   prodAppNo.split("/");
				if(n.length>2){
					number = n[3]+"/"+classificationNo+"/"+n[0];
				}	
			}else{
				if(prodAppNo.contains("-")){
					String [] n = prodAppNo.split("-");
					number =  n[0]+"/"+classificationNo+"/"+n[1];
				}
			}		
		}       
        return number;
    }


	/**
	 * Current user can perform actions on behalf of applicants from this list
	 * @param userSession 
	 * @return
	 */
	public List<Applicant> fetchApplicants(UserSession userSession) {
		User curUser = userSession.getLoggedInUserObj();
		List<Applicant> applicants = new ArrayList<Applicant>();
		Applicant curApplicant = curUser.getApplicant();
		if(userSession.isResponsible(curApplicant)){
			List<AgentAgreement> agreements = new ArrayList<AgentAgreement>();

			if(curApplicant != null && curApplicant.getApplcntId() != null && curApplicant.getApplcntId() > 0)
				agreements = getApplicantService().fetchMyApplicants(curApplicant);

			applicants.add(curApplicant);
			if(agreements != null && agreements.size() > 0){
				Date today = new Date();
				for(AgentAgreement aa:agreements){
					if(aa.getActive() && today.after(aa.getStart()) && today.before(aa.getFinish()))
						applicants.add(aa.getApplicant());
				}
			}
		}else{
			if(userSession.isStaff()){
				applicants.addAll(applicantService.getRegApplicants());
			}else if(userSession.isCompany() && curApplicant != null && curApplicant.getApplcntId() > 0){
				applicants.add(curApplicant);
			}
		}
		return applicants;
	}

	public ProdApplicationsDAO getProdApplicationsDAO() {
		return prodApplicationsDAO;
	}

	public void setProdApplicationsDAO(ProdApplicationsDAO prodApplicationsDAO) {
		this.prodApplicationsDAO = prodApplicationsDAO;
	}

	/**
	 * get getRegExpiryDate in last ProdApplications by Product
	 * @param prodId
	 * @return
	 */
	public Date getRegExpiryDatePreviousProdApp(ProdApplications curProdApp){
		Date expDate = new Date();
		if(curProdApp != null && curProdApp.getProduct() != null){
			Product product = productDAO.findProduct(curProdApp.getProduct().getId());
			List<ProdApplications> prodApps = product.getProdApplicationses();
			if(prodApps != null && prodApps.size() > 0){
				for(int i = 0 ; i < prodApps.size(); i++){
					ProdApplications prodApp = prodApps.get(i);
					if(prodApp != null && prodApp.getRegState() != null 
							&& prodApp.getRegState().equals(RegState.REGISTERED)){
						expDate = prodApp.getRegExpiryDate();
						break;					
					}
				}
			}
		}
		return expDate;
	}

	/**
	 * previous all ProdApplications (in state REGISTERED) by Product send to Archive
	 * @param prodId
	 */
	public void sendToArchiveAllProdAppByProduct(ProdApplications curProdApp, User curUser){
		if(curProdApp != null && curProdApp.getProduct() != null){
			Product product = productDAO.findProduct(curProdApp.getProduct().getId());
			List<ProdApplications> prodApps = product.getProdApplicationses();
			if(prodApps != null && prodApps.size() > 0){
				for(int i = 0; i < prodApps.size(); i++){
					ProdApplications appl = prodApps.get(i);
					if(appl.getId().intValue() != curProdApp.getId().intValue()
							&& appl.getRegState().equals(RegState.REGISTERED)){
						appl.setUpdatedBy(curUser);
						createTimeLine(RegState.ARCHIVE, appl);
						appl.setRegState(RegState.ARCHIVE);
						appl.setArchivingDate(new Date());
						prodApplicationsDAO.saveApplication(appl);
					}
				}
			}
		}
	}
	
	/**
	 * send to Archive curProdApp
	 * @param prodId
	 */
	public void sendToArchive(ProdApplications curProdApp, User curUser){
		if(curProdApp != null && curProdApp.getProduct() != null){
			curProdApp = findProdApplications(curProdApp.getId());
			curProdApp.setUpdatedBy(curUser);
			createTimeLine(RegState.ARCHIVE, curProdApp);
			curProdApp.setRegState(RegState.ARCHIVE);
			curProdApp.setArchivingDate(new Date());
			prodApplicationsDAO.saveApplication(curProdApp);
		}
	}
	
	public ProdApplications createNewProdApplications(ProdApplications prodApp, Long curUserID){
		Product prod = prodApp.getProduct();
		ProdApplications newProdApplications = new ProdApplications();
		newProdApplications.setProduct(prod);
		newProdApplications.setApplicant(prodApp.getApplicant());
		newProdApplications.setProdAppType(prodApp.getProdAppType());
		newProdApplications.setRegState(RegState.SAVED);
		newProdApplications.setApplicantUser(prodApp.getApplicantUser());
		newProdApplications = saveApplication(newProdApplications, curUserID);
		
		return newProdApplications;
	}
	
	 /**
     * @param aplicant_id - Applicant id
     * @param category - HUMAN|VETERINARY|UNKNOWN
     * @return List<ProdApplications> all REGISTERED Product Applications this applicant which have select Product Category
     */
	public List<ProdApplications> findProdApplByApplicants(Long aplicant_id, String year){
		return prodApplicationsDAO.findProdApplByApplicants(aplicant_id, year);		
	}
	
	/**
	 * 
	 * @param aplicant_id - Applicant id
	 * @param category - HUMAN|VETERINARY|UNKNOWN
	 * @param year - year choose in Invoice
	 * @param regExpDate -new Expiry Date (choose in Invoice)
	 * @return
	 */
	public void changeRegExpiryDate(Long aplicant_id, String year, String regExpDate){		
		Date regExpiryDate = null;
		if(!"".equals(regExpDate)){
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			try {
				regExpiryDate = dateFormat.parse(regExpDate);
				if(regExpiryDate!=null){
					List<ProdApplications> prodAppls = findProdApplByApplicants(aplicant_id, year);
					if(prodAppls!=null && prodAppls.size()>0){
						for(ProdApplications pa:prodAppls){
							pa.setRegExpiryDate(regExpiryDate);
							prodApplicationsDAO.saveApplication(pa);
						}
					}
				}
			} catch (ParseException e) {				
				e.printStackTrace();
			}
		}		
	}
}
