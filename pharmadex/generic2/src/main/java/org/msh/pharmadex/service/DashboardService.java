package org.msh.pharmadex.service;

import java.io.Serializable;
import java.util.List;

import org.msh.pharmadex.dao.DashboardDAO;
import org.msh.pharmadex.mbean.ItemDashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Author: dudchenko
 */
@Service
public class DashboardService implements Serializable {

	private static final long serialVersionUID = 7163044735330114713L;

	@Autowired
	DashboardDAO dashboardDAO;
	
	public List<ItemDashboard> getListTimesProcess(boolean showQuarter){
		return dashboardDAO.getListTimesProcess(showQuarter);
	}
	
	public List<ItemDashboard> getListByPercentNemList(boolean showQuarter){
		return dashboardDAO.getListByPercentNemList(showQuarter);
	}
	
	public List<ItemDashboard> getListByPercentApprovedList(boolean showQuarter){
		return dashboardDAO.getListByPercentApprovedList(showQuarter);
	}
	
	//26/09/17 delete Nemlist
	/*
	 public List<ItemDashboard> getListByApplicant(){
		return dashboardDAO.getListByApplicant();
	}*/
	
	public List<ItemDashboard> getListByGenName(){
		return dashboardDAO.getListByGenName();
	}
	
	public List<ItemDashboard> getListByFinreportList(boolean showQuarter){
		return dashboardDAO.getListByFinreportList(showQuarter);
	}

	public List<ItemDashboard> getListByAppNameFinreportList(String year, String quarter, int type) {		
		return dashboardDAO.getListByAppNameFinreportList(year, quarter, type);
	}
	
	public List<ItemDashboard> getListTotalDossier(boolean showQuarter) {		
		return dashboardDAO.getListTotalDossier(showQuarter);
	}
}
