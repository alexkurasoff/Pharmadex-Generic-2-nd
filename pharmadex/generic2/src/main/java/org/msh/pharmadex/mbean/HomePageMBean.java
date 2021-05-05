package org.msh.pharmadex.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.mbean.amendment.AmdListDTO;
import org.msh.pharmadex.service.HomeDTO;
import org.msh.pharmadex.service.HomePageService;

/**

 */
@ManagedBean
@ViewScoped
public class HomePageMBean implements Serializable {
	
	@ManagedProperty(value = "#{homePageService}")
	private HomePageService homePageService;
	
	private int countOnScreening =0;
	private int countAwaitingReview =0;
	private int countOnReview =0;
	private int countAwaitingApproval =0;
	
	/**
	 * @param prodApps - list ProdApplications
	 * @param type - 1 (on screening), 2 (awaiting review), 
	 * 				 3 (on review),    4 (awaiting approval) 
	 * @return list HomeDTO
	 */
	private List<HomeDTO> createHomeDTO(List<ProdApplications> prodApps, int type) {
		List<HomeDTO> ret = new ArrayList<HomeDTO>();
		if(prodApps!=null && prodApps.size()>0){
			for(ProdApplications pa:prodApps){
				String proprietaryName = "";
				if(pa.getProduct()!=null){
					proprietaryName = pa.getProduct().getProdName();
				}
				ProdAppType prodAppType = pa.getProdAppType();
				String prodSrcNo=null, prodAppNo = null; Long prodAppId = null; Date date= null;
				if(type==1||type==2){
					if(pa.getProdSrcNo()!=null){
						prodSrcNo = pa.getProdSrcNo();
					}
				}
				if(type==2||type==3||type==4){
					if(pa.getProdAppNo()!=null){
						prodAppNo = pa.getProdAppNo();
					}
				}
				
				prodAppId = pa.getId();
				if(type==1){
					date = pa.getSubmitDate();
				}else{
					date = pa.getUpdatedDate();
				}				
				ret.add(new HomeDTO(proprietaryName, prodAppType, prodSrcNo,prodAppNo, prodAppId, date));
			}
		}
		return ret;
	}
	
	public List<HomeDTO> getOnScreening(){
		List<HomeDTO> ret = new ArrayList<HomeDTO>();
		List<ProdApplications> prodApps = homePageService.fetchOnScreening();
		ret = createHomeDTO(prodApps,1);		
		return ret;
	}
		
	public List<HomeDTO> getAwaitingReview(){
		List<HomeDTO> ret = new ArrayList<HomeDTO>();
		List<ProdApplications> prodApps = homePageService.fetchAwaitingReview();
		ret = createHomeDTO(prodApps,2);		
		return ret;
	}
	
	public List<HomeDTO> getOnReview(){
		List<HomeDTO> ret = new ArrayList<HomeDTO>();
		List<ProdApplications> prodApps = homePageService.fetchOnReview();
		ret = createHomeDTO(prodApps,3);		
		return ret;
	}

	public List<HomeDTO> getAwaitingApproval(){
		List<HomeDTO> ret = new ArrayList<HomeDTO>();
		List<ProdApplications> prodApps = homePageService.fetchAwaitingApproval();
		ret = createHomeDTO(prodApps,4);		
		return ret;
	}
	
	public HomePageService getHomePageService() {
		return homePageService;
	}

	public void setHomePageService(HomePageService homePageService) {
		this.homePageService = homePageService;
	}

	public int getCountOnScreening() {
		List<ProdApplications> prodApps = homePageService.fetchOnScreening();
		if(prodApps!=null && prodApps.size()>0){
			countOnScreening = prodApps.size();
		}
		return countOnScreening;
	}

	public void setCountOnScreening(int countOnScreening) {
		this.countOnScreening = countOnScreening;
	}

	public int getCountAwaitingReview() {
		List<ProdApplications> prodApps = homePageService.fetchAwaitingReview();
		if(prodApps!=null && prodApps.size()>0){
			countAwaitingReview = prodApps.size();
		}
		return countAwaitingReview;
	}

	public void setCountAwaitingReview(int countAwaitingReview) {
		this.countAwaitingReview = countAwaitingReview;
	}

	
	public int getCountAwaitingApproval() {
		List<ProdApplications> prodApps = homePageService.fetchAwaitingApproval();
		if(prodApps!=null && prodApps.size()>0){
			countAwaitingApproval = prodApps.size();
		}
		return countAwaitingApproval;
	}

	public void setCountAwaitingApproval(int countAwaitingApproval) {
		this.countAwaitingApproval = countAwaitingApproval;
	}

	public int getCountOnReview() {
		List<ProdApplications> prodApps = homePageService.fetchOnReview();
		if(prodApps!=null && prodApps.size()>0){
			countOnReview = prodApps.size();
		}
		return countOnReview;
	}

	public void setCountOnReview(int countOnReview) {
		this.countOnReview = countOnReview;
	}
	
	
}
