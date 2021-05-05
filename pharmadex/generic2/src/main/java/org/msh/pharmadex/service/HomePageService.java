package org.msh.pharmadex.service;

import java.io.Serializable;
import java.util.List;

import org.msh.pharmadex.dao.ProdApplicationsDAO;
import org.msh.pharmadex.domain.Holidays;
import org.msh.pharmadex.domain.ProdApplications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class HomePageService implements Serializable {

	@Autowired
	ProdApplicationsDAO prodApplicationsDAO;
	
	public List<ProdApplications> fetchOnScreening(){
		List<ProdApplications> prodApps = prodApplicationsDAO.fetchOnScreening();
		return prodApps;
	}
	
	public List<ProdApplications> fetchAwaitingReview() {
		List<ProdApplications> prodApps = prodApplicationsDAO.fetchAwaitingReview();
		return prodApps;
	}
	
	public List<ProdApplications> fetchOnReview() {
		List<ProdApplications> prodApps = prodApplicationsDAO.fetchOnReview();
		return prodApps;
	}
	
	public List<ProdApplications> fetchAwaitingApproval() {
		List<ProdApplications> prodApps = prodApplicationsDAO.fetchAwaitingApproval();
		return prodApps;
	}
		
	
	public ProdApplicationsDAO getProdApplicationsDAO() {
		return prodApplicationsDAO;
	}

	public void setProdApplicationsDAO(ProdApplicationsDAO prodApplicationsDAO) {
		this.prodApplicationsDAO = prodApplicationsDAO;
	}

	
}
