/*
 * Copyright (c) 2014. Management Sciences for Health. All Rights Reserved.
 */

package org.msh.pharmadex.mbean.product;

import org.hibernate.Hibernate;
import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.ReviewComment;
import org.msh.pharmadex.domain.ReviewInfo;
import org.msh.pharmadex.domain.enums.RecomendType;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.service.GlobalEntityLists;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.service.ReviewService;
import org.msh.pharmadex.service.UserService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Backing bean to capture review of products
 * Author: usrivastava
 */
@ManagedBean
@ViewScoped
public class ExecSummaryBn implements Serializable {

	private FacesContext facesContext = FacesContext.getCurrentInstance();
	private ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");

	@ManagedProperty(value = "#{globalEntityLists}")
	private GlobalEntityLists globalEntityLists;

	@ManagedProperty(value = "#{reviewService}")
	private ReviewService reviewService;

	@ManagedProperty(value = "#{userService}")
	private UserService userService;

	@ManagedProperty(value = "#{userSession}")
	private UserSession userSession;

	@ManagedProperty(value = "#{prodApplicationsService}")
	private ProdApplicationsService prodApplicationsService;

	private ProdApplications prodApplications;
	private Product product;
	private List<ReviewInfo> reviewInfos;
	private boolean readOnly;
	private boolean editExecSumm;
	private List<RegState> nextSteps;
	private String execSummary;
	private String execSummaryState;

	@PostConstruct
	private void init(){
		try {
			Long prodAppID = Long.valueOf(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("prodAppID"));
			if (prodAppID != null) {
				prodApplications = prodApplicationsService.findProdApplications(prodAppID);
				product = prodApplications.getProduct();
//				Hibernate.initialize(prodApplications.getReviewInfos());
				reviewInfos = reviewService.findReviewInfos(prodAppID);

			}
		}catch (Exception ex){

		}
	}

	public String submit(){
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");

			if (prodApplications.getRegState().equals(RegState.RECOMMENDED)) {
				for (ReviewInfo ri : reviewInfos) {
					if(ri.getRecomendType() == null){
						facesContext.addMessage(null, new FacesMessage("Invalid operation!", resourceBundle.getString("Error.moderatorNotRecomended")));
						return "";
					}
					if (!ri.getRecomendType().equals(RecomendType.RECOMENDED)) {
						facesContext.addMessage(null, new FacesMessage("Invalid operation!", "Cannot recommend a product which is not recommended by the assessors. Send feedback to assessor to change there recommendation."));
						return "";
					}
				}
			} else if (prodApplications.getRegState().equals(RegState.NOT_RECOMMENDED)) {
				for (ReviewInfo ri : reviewInfos) {
					if(ri.getRecomendType() == null){
						facesContext.addMessage(null, new FacesMessage("Invalid operation!", resourceBundle.getString("Error.moderatorNotRecomended")));
						return "";
					}
					if (!ri.getRecomendType().equals(RecomendType.NOT_RECOMENDED)) {
						facesContext.addMessage(null, new FacesMessage("Invalid operation!", "Cannot recommend a product which is not recommended by the assessors. Send feedback to assessor to change there recommendation."));
						return "";
					}
				}

			}


			//        prodApplications.setExecSummary(execSummary);
			String result = prodApplicationsService.submitExecSummary(prodApplications, userSession.getLoggedINUserID(), reviewInfos);
			if (result.equals("persist")) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(resourceBundle.getString("global.success")));
				return "processreg";
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

		} catch (Exception ex) {
			ex.printStackTrace();

		}
		return "";
	}

	public GlobalEntityLists getGlobalEntityLists() {
		return globalEntityLists;
	}

	public void setGlobalEntityLists(GlobalEntityLists globalEntityLists) {
		this.globalEntityLists = globalEntityLists;
	}

	public ReviewService getReviewService() {
		return reviewService;
	}

	public void setReviewService(ReviewService reviewService) {
		this.reviewService = reviewService;
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

	public ProdApplications getProdApplications() {
		return prodApplications;
	}

	public void setProdApplications(ProdApplications prodApplications) {
		this.prodApplications = prodApplications;
	}

	public List<ReviewInfo> getReviewInfos() {
		return reviewInfos;
	}

	public void setReviewInfos(List<ReviewInfo> reviewInfos) {
		this.reviewInfos = reviewInfos;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public boolean isReadOnly() {
		if (prodApplications != null && prodApplications.getRegState() != null) {
			if (prodApplications.getRegState().equals(RegState.RECOMMENDED) || prodApplications.getRegState().equals(RegState.NOT_RECOMMENDED))
				readOnly = true;
			else
				readOnly = false;
		}
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	/**
	 * Complex logic!
	 * <ul>
	 * <li>if each review info is recommended, then recommended only
	 * <li>if each review info is not recommended, then not recommended only
	 * <li>if some review infos is recommended and some not recommended, only follow up
	 * <li>follow up should be always
	 * </ul>
	 * @return
	 */
	public List<RegState> getNextSteps() {
		List<RegState> nextSteps = new ArrayList<RegState>();
		boolean rec = true;
		for(ReviewInfo ri :getProdApplications().getReviewInfos()){
			if(ri.getRecomendType() == RecomendType.NOT_RECOMENDED){
				rec = false;
			}
			if(ri.getRecomendType() == RecomendType.RECOMENDED){
				rec = true && rec;
			}
		}
		if(rec){
			nextSteps.add(RegState.RECOMMENDED);
		}else{
			nextSteps.add(RegState.NOT_RECOMMENDED);
		}
		nextSteps.add(RegState.FOLLOW_UP);
		return nextSteps;
	}

	public void setNextSteps(List<RegState> nextSteps) {
		this.nextSteps = nextSteps;
	}

	public String getExecSummary() {
		return execSummary;
	}

	public void setExecSummary(String execSummary) {
		this.execSummary = execSummary;
	}

	/**
	 * If ReviewInfo contains two Review - show two names
	 * @param item
	 * @return
	 */
	public String buildReviewerNames(ReviewInfo item){
		String names = "";
		if(item != null){
			if(item.getReviewer() != null){
				names = item.getReviewer().getName();
				if(item.getSecReviewer() != null)
					names += ", " + item.getSecReviewer().getName();
			}
		}
		return names;
	}
	/**
	 * Build right reviewer's opinion
	 * @param item
	 * @return
	 */
	public String buildReviwerConclusions(ReviewInfo item){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");
		String ret = "";
		if(item != null){
			for(ReviewComment com : item.getReviewComments()){
				if(com.getRecomendType() == RecomendType.RECOMENDED || com.getRecomendType() == RecomendType.NOT_RECOMENDED){
					ret = ret + ", " + resourceBundle.getString(com.getRecomendType().getKey());
				}
			}
		}
		return ret.substring(1).trim();
	}
	/**
	 * Build right reviewer's summaries
	 * @param item
	 * @return
	 */
	public String buildReviewerSummaries(ReviewInfo item){
		String ret = "";
		if(item != null){
			for(ReviewComment com : item.getReviewComments()){
				if(com.getRecomendType() == RecomendType.RECOMENDED || com.getRecomendType() == RecomendType.NOT_RECOMENDED){
					ret = ret + " " + com.getComment();
				}
			}
		}
		return ret.trim();
	}

	public boolean isEditExecSumm() {
		/*
		 * если текущий пользователь с ролью модератор и он назначен модератором
		 * состояние досье не RECOMMENDED или NOT_RECOMMENDED
		 */
		editExecSumm = false;
		if(prodApplications != null && prodApplications.getModerator() != null){
			if(userSession.isModerator() && prodApplications.getModerator().getUserId().intValue() == userSession.getLoggedINUserID().intValue())
				if(prodApplications.getRegState() != null && !(prodApplications.getRegState().equals(RegState.RECOMMENDED)
						|| prodApplications.getRegState().equals(RegState.NOT_RECOMMENDED)))
					editExecSumm = true;
		}
		return editExecSumm;
	}

	public void setEditExecSumm(boolean editExecSumm) {
		this.editExecSumm = editExecSumm;
	}

	public String getExecSummaryState() {
		execSummaryState = "";
		if(prodApplications != null){
			RegState st = prodApplications.getRegState();
			if(st.equals(RegState.RECOMMENDED) || st.equals(RegState.NOT_RECOMMENDED)
					|| st.equals(RegState.FOLLOW_UP))
				execSummaryState = resourceBundle.getString(st.getKey());
		}
		return execSummaryState;
	}

	public void setExecSummaryState(String execSummaryState) {
		this.execSummaryState = execSummaryState;
	}

}
