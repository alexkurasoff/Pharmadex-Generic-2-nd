/*
 * Copyright (c) 2014. Management Sciences for Health. All Rights Reserved.
 */

package org.msh.pharmadex.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.msh.pharmadex.dao.CustomReviewDAO;
import org.msh.pharmadex.dao.ProdApplicationsDAO;
import org.msh.pharmadex.dao.ReviewQDAO;
import org.msh.pharmadex.dao.iface.RevDeficiencyDAO;
import org.msh.pharmadex.dao.iface.ReviewDAO;
import org.msh.pharmadex.dao.iface.ReviewDetailDAO;
import org.msh.pharmadex.dao.iface.ReviewInfoDAO;
import org.msh.pharmadex.domain.ProdAppLetter;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.RevDeficiency;
import org.msh.pharmadex.domain.Review;
import org.msh.pharmadex.domain.ReviewComment;
import org.msh.pharmadex.domain.ReviewDetail;
import org.msh.pharmadex.domain.ReviewInfo;
import org.msh.pharmadex.domain.ReviewQuestion;
import org.msh.pharmadex.domain.TimeLine;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.msh.pharmadex.domain.enums.RecomendType;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.domain.enums.ReviewStatus;
import org.msh.pharmadex.mbean.product.ReviewInfoTable;
import org.msh.pharmadex.util.RetObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 * Author: usrivastava
 */
@Service
public class ReviewService implements Serializable {

	@Autowired
	ProdApplicationsDAO prodApplicationsDAO;
	@Autowired
	TimelineService timelineService;
	@Autowired
	private ReviewDAO reviewDAO;
	@Autowired
	private GlobalEntityLists globalEntityLists;
	@Autowired
	private ChecklistService checklistService;
	@Autowired
	private ReviewInfoDAO reviewInfoDAO;
	@Autowired
	private ReviewQDAO reviewQDAO;
	@Autowired
	private ReviewDetailDAO reviewDetailDAO;
	@Autowired
	private ProdApplicationsService prodApplicationsService;
	@Autowired
	private RevDeficiencyDAO revDeficiencyDAO;
	@Autowired
	private CustomReviewDAO customReviewDAO;
	@Autowired
	private UserAccessService userAccessService;

	@Autowired
	private UserService userService;

	@Autowired
	private UtilsByReports utilsByReports;

	public List<ReviewInfoTable> findRevInfoTableByReviewer(Long reviewerID) {
		if (reviewerID == null)
			return null;
		return customReviewDAO.findReviewInfoByReview(reviewerID);

	}

	public List<ReviewInfoTable> findReviewByReviewer(Long reviewerID) {
		if (reviewerID == null)
			return null;
		return customReviewDAO.findReviewByReviewer(reviewerID);

	}

	public List<ReviewInfoTable> findAllPriSecReview() {
		return customReviewDAO.findAllPriSecReview();
	}


	public Review findReview(Long reviewID) {
		Review review = reviewDAO.findOne(reviewID);
		//        List<ReviewChecklist> reviewChecklists = review.getReviewChecklists();
		//        if (reviewChecklists.size() < 1) {
		//            reviewChecklists = new ArrayList<ReviewChecklist>();
		//            review.setReviewChecklists(reviewChecklists);
		//            List<Checklist> allChecklist = checklistService.getChecklists(review.getProdApplications().getProdAppType(), true);
		//            ReviewChecklist eachReviewChecklist;
		//            for (int i = 0; allChecklist.size() > i; i++) {
		//                eachReviewChecklist = new ReviewChecklist();
		//                eachReviewChecklist.setChecklist(allChecklist.get(i));
		//                eachReviewChecklist.setReview(review);
		//                reviewChecklists.add(eachReviewChecklist);
		//            }
		//            review.setReviewChecklists(reviewChecklists);
		//        }
		return review;
	}

	public Review findReviewByProdApp(Long prodAppID) {
		List<Review> list = reviewDAO.findByProdApplications_Id(prodAppID);
		if(list != null && list.size() > 0)
			return list.get(0);
		//Review review = reviewDAO.findOne(reviewID);

		return null;
	}

	public RetObject saveReviewers(Review review) {
		RetObject retObject = new RetObject();
		Review returnvalue = reviewDAO.findByUser_UserIdAndProdApplications_Id(review.getUser().getUserId(), review.getProdApplications().getId());
		if (returnvalue == null) {
			retObject.setObj(reviewDAO.saveAndFlush(review));
			retObject.setMsg("success");
		} else {
			retObject.setMsg("exist");
		}
		return retObject;
	}

	public Review findReviewByUserAndProdApp(Long userId, Long prodAppID) {
		return reviewDAO.findByUser_UserIdAndProdApplications_Id(userId, prodAppID);
	}

	public ReviewInfo findReviewInfoByUserAndProdApp(Long userId, Long prodAppID) {
		ReviewInfo reviewInfo = null;
		List<ReviewInfo> li = reviewInfoDAO.findByProdApplications_IdAndReviewer_UserIdOrSecReviewer_UserId(prodAppID, userId, userId);
		if(li != null && li.size() > 0){
			reviewInfo = li.get(0);
			Hibernate.initialize(reviewInfo.getReviewDetails());
			Hibernate.initialize(reviewInfo.getReviewer());
			Hibernate.initialize(reviewInfo.getSecReviewer());
			Hibernate.initialize(reviewInfo.getReviewComments());
		}
		
		return reviewInfo;
	}

	public void delete(Review review) {
		reviewDAO.delete(review);
	}

	public ReviewInfo findReviewInfo(Long reviewInfoID) {
		if (reviewInfoID == null)
			return null;
		ReviewInfo reviewInfo = reviewInfoDAO.findById(reviewInfoID);
		return reviewInfo;
	}


	//    public List<DisplayReviewQ> getDisplayReviewQ() {
	//        List<ReviewQuestion> reviewQuestions = reviewQDAO.findAll();
	//        List<DisplayReviewQ> header1 = new ArrayList<DisplayReviewQ>();
	//        List<DisplayReviewQ.Header2> header2 = new ArrayList<DisplayReviewQ.Header2>();
	//
	//        String header1Name = "";
	//        String header2Name = "";
	//
	//        List<DisplayReviewInfo> questions = null;
	//        DisplayReviewQ dispHeader1 = null;
	//        DisplayReviewQ.Header2 dispHeader2;
	//        int size = reviewQuestions.size();
	//        ReviewQuestion rq;
	//        for (int i = 0; i < reviewQuestions.size(); i++) {
	//            rq = reviewQuestions.get(i);
	//            if (header1Name.equals("")) {
	//                header1Name = rq.getHeader1();
	//                header2Name = rq.getHeader2();
	//                questions = new ArrayList<DisplayReviewInfo>();
	//                questions.add(new DisplayReviewInfo(rq.getId(), null, rq.getQuestion(), false, null));
	//                dispHeader1 = new DisplayReviewQ(header1Name, header2);
	//                dispHeader2 = dispHeader1.new Header2(header2Name, questions);
	//                header2.add(dispHeader2);
	//                header1.add(dispHeader1);
	//
	////                    header2 = header1.new Header2(header2Name, questions);
	//
	////                    header2.put(header2Name, questions);
	////                    header1.put(header1Name, header2);
	//            } else {
	//                if (header1Name.equals(rq.getHeader1())) {
	//                    if (header2Name.equals(rq.getHeader2())) {
	//                        questions.add(new DisplayReviewInfo(rq.getId(), null, rq.getQuestion(), false));
	//                    } else {
	//                        questions = new ArrayList<DisplayReviewInfo>();
	//                        questions.add(new DisplayReviewInfo(rq.getId(), null, rq.getQuestion(), false));
	//                        header2Name = rq.getHeader2();
	//                        dispHeader2 = dispHeader1.new Header2(header2Name, questions);
	//                        header2.add(dispHeader2);
	////                            header2.put(header2Name, questions);
	//                    }
	//                } else {
	//                    header2 = new ArrayList<DisplayReviewQ.Header2>();
	//                    header1Name = rq.getHeader1();
	//                    header2Name = rq.getHeader2();
	//                    questions = new ArrayList<DisplayReviewInfo>();
	//                    questions.add(new DisplayReviewInfo(rq.getId(), null, rq.getQuestion(), false));
	////                        header1 = new DisplayReviewQ(header1Name, header2);
	//                    dispHeader1 = new DisplayReviewQ(header1Name, header2);
	//                    dispHeader2 = dispHeader1.new Header2(header2Name, questions);
	//                    header2.add(dispHeader2);
	//                    header1.add(dispHeader1);
	//
	//
	////                        header2.put(header2Name, questions);
	////                        header1.put(header1Name, header2);
	//
	//                }
	//            }
	//
	//
	//        }
	//
	//        return header1;
	//    }

	@Transactional
	public List<ReviewDetail> findReviewDetails(Long userID, Long reviewInfoID) {
		return reviewQDAO.findReviewSummary(userID, reviewInfoID);

	}

	public List<ReviewDetail> initReviewDetail(ReviewInfo reviewInfo, Long loggedInUser) {
		ProdApplications prodApplications = reviewInfo.getProdApplications();
		List<ReviewDetail> reviewDetails = new ArrayList<ReviewDetail>();
		List<ReviewQuestion> reviewQuestions = null;

		if (prodApplications.getProdAppType().equals(ProdAppType.RENEW)) {
			reviewQuestions = reviewQDAO.findByRenewal();
		} else if (prodApplications.isSra()) {
			reviewQuestions = reviewQDAO.findBySRA();
		} else if (prodApplications.getProdAppType().equals(ProdAppType.NEW_CHEMICAL_ENTITY)) {
			reviewQuestions = reviewQDAO.findByNewMolecule();
		} else {
			reviewQuestions = reviewQDAO.findByGenMed();
		}

		ReviewDetail reviewDetail;
		User curUser = userService.findUser(loggedInUser);
		for (ReviewQuestion reviewQuestion : reviewQuestions) {
			reviewDetail = new ReviewDetail(reviewQuestion, reviewInfo, false, curUser);
			reviewDetails.add(reviewDetail);
		}

		reviewDetails = reviewDetailDAO.save(reviewDetails);
		return reviewDetails;
	}

	public List<DisplayReviewQ> getDisplayReviewSum(ReviewInfo ri, Long loggedInUser) {
		List<DisplayReviewQ> header1 = new ArrayList<DisplayReviewQ>();
		if(ri != null && ri.getReviewer() != null){
			List<ReviewDetail> reviewDetails = reviewQDAO.findReviewSummary(ri.getReviewer().getUserId(), ri.getId());

			if (reviewDetails == null)
				reviewDetails = initReviewDetail(ri, loggedInUser);

			if(reviewDetails.size() < 1)
				reviewDetails = initReviewDetail(ri, loggedInUser);

			if (reviewDetails == null)
				return header1;
			if(reviewDetails.size() == 0)
				return header1;

			List<DisplayReviewQ.Header2> header2 = new ArrayList<DisplayReviewQ.Header2>();

			String header1Name = "";
			String header2Name = "";

			List<DisplayReviewInfo> questions = null;
			DisplayReviewQ dispHeader1 = null;
			DisplayReviewQ.Header2 dispHeader2;

			ReviewQuestion rq;
			ReviewDetail rd;
			for (int i = 0; i < reviewDetails.size(); i++) {
				rd = reviewDetails.get(i);
				rq = rd.getReviewQuestions();

				if (header1Name.equals("")) {
					header1Name = rq.getHeader1();
					header2Name = rq.getHeader2();
					questions = new ArrayList<DisplayReviewInfo>();
					dispHeader1 = new DisplayReviewQ(header1Name, header2);
					dispHeader2 = dispHeader1.new Header2(header2Name, questions);
					header2.add(dispHeader2);
					header1.add(dispHeader1);
				} else {
					if (header1Name.equals(rq.getHeader1())) {
						if (!header2Name.equals(rq.getHeader2())) {
							questions = new ArrayList<DisplayReviewInfo>();
							header2Name = rq.getHeader2();
							dispHeader2 = dispHeader1.new Header2(header2Name, questions);
							header2.add(dispHeader2);
						}
					} else {
						header2 = new ArrayList<DisplayReviewQ.Header2>();
						header1Name = rq.getHeader1();
						header2Name = rq.getHeader2();
						questions = new ArrayList<DisplayReviewInfo>();
						dispHeader1 = new DisplayReviewQ(header1Name, header2);
						dispHeader2 = dispHeader1.new Header2(header2Name, questions);
						header2.add(dispHeader2);
						header1.add(dispHeader1);
					}
				}
				questions.add(new DisplayReviewInfo(rq.getId(), rd.getId(), rq.getQuestion(), rd.isAnswered(), ri.getId()));
			}
		}
		return header1;
	}

	public JasperPrint initRegCert(ProdApplications prodApplications, RevDeficiency reviewComment) throws JRException {
		String emailBody = reviewComment.getSentComment().getComment();
		Product product = prodApplications.getProduct();
		URL resource = getClass().getResource("/reports/rev_def_letter.jasper");

		HashMap<String, Object> param = new HashMap<String, Object>();
		utilsByReports.init(param, prodApplications, product);
		utilsByReports.putNotNull(UtilsByReports.KEY_APPNAME, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_PRODNAME, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_PRODSTRENGTH, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_DOSFORM, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_MANUFNAME, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_APPTYPE, "New Medicine Registration", true);
		utilsByReports.putNotNull(UtilsByReports.KEY_SUBJECT, "Product application further information letter for  ", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_BODY, emailBody, true);
		utilsByReports.putNotNull(UtilsByReports.KEY_ADDRESS1, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_ADDRESS2, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_COUNTRY, "", false);
		utilsByReports.putNotNull(UtilsByReports.KEY_APPNUMBER, "", false);

		if(reviewComment.getDueDate() != null){
			SimpleDateFormat sdf = new SimpleDateFormat();
			String dueDate = sdf.format(reviewComment.getDueDate());
			param.put("DueDate", dueDate);
		}

		param.put("date", new Date());

		return JasperFillManager.fillReport(resource.getFile(), param);
	}

	@Transactional
	public RetObject createDefLetter(RevDeficiency revDeficiency) {
		ProdApplications prodApp = prodApplicationsService.findProdApplications(revDeficiency.getReviewInfo().getProdApplications().getId());
		Product product = prodApp.getProduct();
		try {
			ReviewInfo ri = reviewInfoDAO.findOne(revDeficiency.getReviewInfo().getId());
			ri.setReviewStatus  (ReviewStatus.FIR_SUBMIT);
			//            invoice.setPaymentStatus(PaymentStatus.INVOICE_ISSUED);
			File invoicePDF = File.createTempFile("" + product.getProdName() + "_fir", ".pdf");
			JasperPrint jasperPrint = initRegCert(prodApp, revDeficiency);
			net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jasperPrint, new FileOutputStream(invoicePDF));
			byte[] file = IOUtils.toByteArray(new FileInputStream(invoicePDF));
			ProdAppLetter attachment = new ProdAppLetter();
			attachment.setRegState(prodApp.getRegState());
			attachment.setComment(revDeficiency.getSentComment().getComment());
			attachment.setFile(file);
			attachment.setProdApplications(prodApp);
			attachment.setFileName(invoicePDF.getName());
			attachment.setTitle("Further Information Request");
			attachment.setUploadedBy(revDeficiency.getUser());
			attachment.setComment("Automatically generated Letter");
			attachment.setContentType("application/pdf");
			attachment.setReviewInfo(ri);
			revDeficiency.setProdAppLetter(attachment);
			revDeficiency.setReviewInfo(ri);
			revDeficiencyDAO.saveAndFlush(revDeficiency);

			TimeLine timeLine = new TimeLine();
			timeLine.setComment("Status changes due to further information request");
			timeLine.setRegState(RegState.FOLLOW_UP);
			timeLine.setProdApplications(prodApp);
			timeLine.setStatusDate(new Date());
			timeLine.setUser(revDeficiency.getUser());
			RetObject retObject = timelineService.saveTimeLine(timeLine);
			if (retObject.getMsg().equals("persist")) {
				timeLine = (TimeLine) retObject.getObj();
				prodApp = timeLine.getProdApplications();
				revDeficiency.getReviewInfo().setProdApplications(prodApp);
			}
			return saveReviewInfo(revDeficiency.getReviewInfo());
		} catch (JRException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return new RetObject("error");
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return new RetObject("error");
		}
	}

	@Transactional
	public RetObject saveReviewInfo(ReviewInfo reviewInfo) {
		RetObject retObject = new RetObject();
		ReviewInfo ri = reviewInfoDAO.saveAndFlush(reviewInfo);
		Hibernate.initialize(ri.getReviewComments());
		Hibernate.initialize(ri.getReviewDetails());
		retObject.setObj(ri);
		retObject.setMsg("success");
		return retObject;
	}

	public RetObject saveReview(Review review) {
		RetObject retObject = new RetObject();
		retObject.setObj(reviewDAO.saveAndFlush(review));
		retObject.setMsg("success");
		return retObject;
	}

	public RetObject addReviewInfo(ReviewInfo reviewInfo) {
		RetObject retObject = new RetObject();
		ReviewInfo returnvalue = reviewInfoDAO.findByReviewer_UserIdAndProdApplications_Id(reviewInfo.getReviewer().getUserId(), reviewInfo.getProdApplications().getId());
		if (returnvalue == null) {
			retObject.setObj(reviewInfoDAO.saveAndFlush(reviewInfo));
			retObject.setMsg("success");
		} else {
			retObject.setMsg("exist");
		}
		return retObject;
	}

	public RetObject updateReviewInfo(ReviewInfo reviewInfo) {
		/*RetObject retObject = new RetObject();
		retObject.setObj(reviewInfoDAO.save(reviewInfo));
		retObject.setMsg("success");
		return retObject;*/
		
		RetObject retObject = new RetObject();
		ReviewInfo returnvalue = reviewInfoDAO.findByReviewer_UserIdAndProdApplications_Id(reviewInfo.getReviewer().getUserId(), reviewInfo.getProdApplications().getId());
		if (returnvalue == null) {
			retObject.setObj(reviewInfoDAO.saveAndFlush(reviewInfo));
			retObject.setMsg("success");
		} else if(returnvalue.getId().intValue() == reviewInfo.getId().intValue()){
			retObject.setObj(reviewInfoDAO.saveAndFlush(reviewInfo));
			retObject.setMsg("success");
		}else
			retObject.setMsg("exist");
		
		return retObject;
	}

	public ReviewDetail findReviewDetails(DisplayReviewInfo displayReview) {
		if (displayReview.getReviewDetailID() != null) {
			return reviewDetailDAO.findOne(displayReview.getReviewDetailID());
		} else {
			ReviewDetail reviewDetail = new ReviewDetail();
			reviewDetail.setReviewQuestions(reviewQDAO.findOne(displayReview.getId()));
			reviewDetail.setReviewInfo(reviewInfoDAO.findOne(displayReview.getReviewInfoID()));
			return reviewDetail;
		}
	}

	/**
	 * changes review status It depends of current status and user role
	 * Odissey 09/04/16
	 */
	public void updateReviewStatus(ReviewDetail reviewDetail){
		//Statuses:NOT_ASSIGNED,ASSIGNED,IN_PROGRESS,SEC_REVIEW,SUBMITTED,FEEDBACK,ACCEPTED;
		ReviewInfo ri = reviewDetail.getReviewInfo();
		User user = reviewDetail.getUpdatedBy();
		if (userService.userHasRole(user,"Reviewer")){
			if (ri.getReviewStatus().equals(ReviewStatus.ASSIGNED))
				ri.setReviewStatus(ReviewStatus.IN_PROGRESS);
			if (ri.getReviewStatus().equals(ReviewStatus.IN_PROGRESS))
				ri.setReviewStatus(ReviewStatus.SUBMITTED);
		}
		if (userService.userHasRole(user,"HEAD")){
			if (ri.getReviewStatus().equals(ReviewStatus.SUBMITTED))
				ri.setReviewStatus(ReviewStatus.ACCEPTED);
		}
	}

	public ReviewDetail saveReviewDetail(ReviewDetail reviewDetail) {
		updateReviewStatus(reviewDetail);
		return reviewDetailDAO.saveAndFlush(reviewDetail);
	}

	public ReviewDetail makeReviewNA(Long reviewDetailID, Long loggedINUserId) {
		ReviewDetail rd = reviewDetailDAO.findOne(reviewDetailID);
		rd.setAnswered(true);
		rd.setUpdatedBy(userService.findUser(loggedINUserId));
		
		rd = reviewDetailDAO.saveAndFlush(rd);
		return rd;
	}

	public String submitReview(ReviewInfo reviewInfo) {
		reviewInfo.setReviewStatus(ReviewStatus.SUBMITTED);
		reviewInfo.setSubmitDate(new Date());

		//        List<ReviewDetail> reviewDetails = findReviewDetails(reviewInfo.getReviewer().getUserId(), reviewInfo.getId());
		//        for(ReviewDetail reviewDetail : reviewDetails){
		//            if(!reviewDetail.isAnswered()){
		//                return "NOT_ANSWERED";
		//            }
		//        }

		//        if(reviewInfo.getRecomendType()==null)
		//            return "NO_RECOMMEND_TYPE";

		reviewInfoDAO.saveAndFlush(reviewInfo);
		return "SAVE";
	}

	public List<ReviewInfo> findReviewInfos(Long id) {
		return reviewInfoDAO.findByProdApplications_IdOrderByAssignDateAsc(id);
	}

	public byte[] findReviewDetailImage(Long reviewDetailID) {
		if (reviewDetailID == null)
			return null;
		ReviewDetail reviewDetail = reviewDetailDAO.findOne(reviewDetailID);
		if (reviewDetail != null)
			return reviewDetail.getFile();
		else
			return null;
	}

	public List<Review> findReviews(Long prodAppId) {
		return reviewDAO.findByProdApplications_Id(prodAppId);
	}

	public void deleteReviewInfo(ReviewInfo reviewInfo) {
		reviewInfoDAO.delete(reviewInfo);
	}

	public RetObject saveRevDeficiency(RevDeficiency revDeficiency) {
		revDeficiency = revDeficiencyDAO.saveAndFlush(revDeficiency);

		TimeLine timeLine = new TimeLine();
		timeLine.setComment("FIR recieved and status updated back to Under Review");
		timeLine.setProdApplications(revDeficiency.getReviewInfo().getProdApplications());
		timeLine.setUser(revDeficiency.getUser());
		timeLine.setStatusDate(new Date());
		timeLine.setRegState(RegState.REVIEW_BOARD);
		RetObject retObject = timelineService.saveTimeLine(timeLine);
		if (retObject.getMsg().equals("persist")) {
			timeLine = (TimeLine) retObject.getObj();
			revDeficiency.getReviewInfo().setProdApplications(timeLine.getProdApplications());
		}

		RetObject retObject2 = saveReviewInfo(revDeficiency.getReviewInfo());
		if (retObject2.getMsg().equals("success")) {
			ReviewInfo ri = (ReviewInfo) retObject2.getObj();
			revDeficiency.setReviewInfo(ri);
		}
		return new RetObject("success", revDeficiency);
	}


	/**
	 * Procedure calculates and saves review status in depends of current status and user role
	 * Then saves review info into database
	 * @param reviewInfo
	 * @param reviewComment
	 * @param userID
	 * @return
	 */
	public RetObject submitReviewInfo(ReviewInfo reviewInfo, ReviewComment reviewComment, Long userID) {
		if (reviewComment.getRecomendType() == null) {
			reviewComment.setFinalSummary(false);
		} else {
			if (reviewComment.getRecomendType().equals(RecomendType.RECOMENDED) || reviewComment.getRecomendType().equals(RecomendType.NOT_RECOMENDED)
					|| reviewComment.getRecomendType().equals(RecomendType.FIR)) {
				if (userAccessService.getWorkspace().isSecReview()) {
					if (userID.equals(reviewInfo.getReviewer().getUserId()))
						if (reviewInfo.isSecreview()) {
							reviewInfo.setReviewStatus(ReviewStatus.SEC_REVIEW);
						} else {
							reviewInfo.setReviewStatus(ReviewStatus.SUBMITTED);
						}
					else if (userID.equals(reviewInfo.getSecReviewer().getUserId()))
						reviewInfo.setReviewStatus(ReviewStatus.SUBMITTED);
				} else {
					reviewInfo.setReviewStatus(ReviewStatus.SUBMITTED);
				}
				reviewComment.setFinalSummary(true);
			}else if (reviewComment.getRecomendType().equals(RecomendType.REGISTER)||reviewComment.getRecomendType().equals(RecomendType.SUSPEND)||reviewComment.getRecomendType().equals(RecomendType.CANCEL)){
				reviewInfo.setReviewStatus(ReviewStatus.SUBMITTED);
			}
		}
		reviewInfo.setSubmitDate(new Date());
		reviewInfo.getReviewComments().add(reviewComment);

		List<RevDeficiency> revDeficiencies = revDeficiencyDAO.findByReviewInfo_Id(reviewInfo.getId());
		for (RevDeficiency revDeficiency : revDeficiencies) {
			if (!revDeficiency.isResolved()) {
				return new RetObject("close_def");
			}
		}
		List<ReviewComment> reviewComments = reviewInfo.getReviewComments();
		for (ReviewComment rc : reviewComments) {
			if (rc.getRecomendType() != null) {
				reviewInfo.setRecomendType(rc.getRecomendType());
				reviewInfo.setExecSummary(rc.getComment());
			}
		}
		return saveReviewInfo(reviewInfo);
	}


	public JasperPrint getReviewReport(Long id) throws Exception {
		JasperPrint jasperPrint;

		jasperPrint = customReviewDAO.getReviewReport(id);
		return jasperPrint;


	}

	public List<ReviewComment> findReviewComments(Long id) {
		try {
			if (id == null)
				return null;

			return reviewInfoDAO.findReviewComments(id);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	public RevDeficiency findRevDef(Long revDefID) {
		if(revDefID==null)
			return null;
		return revDeficiencyDAO.findOne(revDefID);

	}

	
	@Transactional
	public String submitFir(RevDeficiency revDeficiency, ReviewComment reviewComment, Long loggedInUser) {
		if(revDeficiency==null)
			return "empty";

		ReviewInfo reviewInfo = reviewInfoDAO.findById(revDeficiency.getReviewInfo().getId());
		reviewInfo.setReviewStatus(ReviewStatus.FIR_APP_RESPONSE);
		reviewInfo.setUpdatedDate(new Date());
		if(reviewInfo.getReviewComments()==null){
			reviewInfo.setReviewComments(new ArrayList<ReviewComment>());
		}
		reviewComment.setReviewInfo(reviewInfo);
		reviewComment.setUser(userService.findUser(loggedInUser));
		reviewComment.setRecomendType(RecomendType.FIR);
		reviewComment.setDate(new Date());
		reviewInfo.getReviewComments().add(reviewComment);
		revDeficiency.setReviewInfo(reviewInfo);

		reviewInfo = reviewInfoDAO.save(reviewInfo);
		return "persist";
	}

	public ReviewComment findSuspendReviewComment(ReviewInfo reviewInfo,User user){
		if (reviewInfo==null) return null;
		List<ReviewComment> comments = reviewInfo.getReviewComments();
		if (comments!=null&&comments.size()>0){
			for(ReviewComment comment:comments){
				if (comment.getUser().getUserId()==user.getUserId()){
					return comment;
				}
			}
		}
		return null;
	}

	public ReviewComment createSuspendReviewComment(ReviewInfo reviewInfo,User user){
		if (reviewInfo==null) return null;
		ReviewComment comment = new ReviewComment();
		List<ReviewComment> comments = reviewInfo.getReviewComments();
		comment.setReviewInfo(reviewInfo);
		comment.setUser(user);
		comment.setDate(new Date());
		comments.add(comment);
		saveReviewInfo(reviewInfo);
		return comment;
	}
}
