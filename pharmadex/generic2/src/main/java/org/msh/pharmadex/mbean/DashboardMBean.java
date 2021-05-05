package org.msh.pharmadex.mbean;

import static javax.faces.context.FacesContext.getCurrentInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.service.DashboardService;
import org.msh.pharmadex.service.UserService;
import org.msh.pharmadex.util.Scrooge;

/**
 * @author dudchenko
 *
 */
@ManagedBean
@ViewScoped
public class DashboardMBean implements Serializable {

	private static final long serialVersionUID = 4866135420621315047L;
	
	private FacesContext facesContext = FacesContext.getCurrentInstance();
	protected ResourceBundle resourceBundle;
	
	@ManagedProperty(value = "#{userSession}")
	protected UserSession userSession;
	@ManagedProperty(value = "#{userService}")
	public UserService userService;
	@ManagedProperty(value = "#{dashboardService}")
	public DashboardService dashboardService;

	private List<ItemDashboard> list = new ArrayList<ItemDashboard>();
	private boolean showQuart = false;
	public List<ItemDashboard> filteredList;
	
	
	private String numreport = "";
	private String year ="";
	private String quarter = "";
	private int type = 0;
	private String headerDlg = "";
	@PostConstruct
	private void init() {
		facesContext = getCurrentInstance();
		resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");
		
		numreport = Scrooge.beanStrParam("numreport");
	}

	public void loadList(boolean showQuarter){
		if(isReport(1)){
			list = dashboardService.getListTimesProcess(showQuarter);
		}else if(isReport(2)){
			list = dashboardService.getListByPercentNemList(showQuarter);
		}else if(isReport(3)){
			list = dashboardService.getListByPercentApprovedList(showQuarter);
		}
		
		//26/09/17 delete Nemlist
		/*else if(isReport(4)){
			list = dashboardService.getListByApplicant();
		}*/
		else if(isReport(5)){
			list = dashboardService.getListByGenName();
		}else if(isReport(6)){
			list = dashboardService.getListByFinreportList(showQuarter);
		}else if(isReport(7)){
			list = dashboardService.getListTotalDossier(showQuarter);
		}
	}
	
	public String getTitle(){
		if(isReport(1))
			return resourceBundle.getString("title_times");
		if(isReport(2))
			return resourceBundle.getString("title_perNemList");
		if(isReport(3))
			return resourceBundle.getString("title_perAppl");
		//26/09/17 delete Nemlist
		/*if(isReport(4))
			return resourceBundle.getString("title_byApplicant");*/
		if(isReport(5))
			return resourceBundle.getString("title_generic");
		if(isReport(6))
			return resourceBundle.getString("title_financial");
		if(isReport(7))
			return resourceBundle.getString("title_totalDossier");
		return "";
	}
	
	public boolean isReport(int n){
		return numreport.equals(String.valueOf(n));
	}
	
	public boolean renderedColumn(int numCol){
		if(isReport(1) && numCol <= 6) // 1-6
			return true;
		
		if(isReport(2) && numCol <= 5)
			return true;
			
		if(isReport(3) && numCol <= 7)
			return true;
		
		//26/09/17 delete Nemlist
		/*if(isReport(4) && numCol <= 4)
			return true;
		*/
		if(isReport(5) && numCol <= 3)
			return true;
		
		if(isReport(6) && numCol <= 7)
			return true;
		
		if(isReport(7) && numCol <= 7)
			return true;
		
		return false;
	}
	
	public String headerColumn(int numCol){
		if(isReport(1)){
			switch (numCol) {
			case 1:
				return resourceBundle.getString("col_year");
			case 2:
				return resourceBundle.getString("col_quarter");
			case 3:
				return resourceBundle.getString("col_total");
			//26/09/17 delete Nemlist
			/*case 4:
				return resourceBundle.getString("col_avgscreening");*/
			case 5:
				return resourceBundle.getString("col_avgreview");
			case 6:
				return resourceBundle.getString("col_avgtotal");
			}
		}
		if(isReport(2)){
			switch (numCol) {
			case 1:
				return resourceBundle.getString("col_year");
			case 2:
				return resourceBundle.getString("col_quarter");
			case 3:
				return resourceBundle.getString("col_total");
			case 4:
				return resourceBundle.getString("col_count");
			case 5:
				return resourceBundle.getString("col_percent");
			}
		}
		if(isReport(3)){
			switch (numCol) {
			case 1:
				return resourceBundle.getString("col_year");
			case 2:
				return resourceBundle.getString("col_quarter");
			case 3:
				return resourceBundle.getString("col_total");
			case 4:
				return resourceBundle.getString("col_countReg");
			case 5:
				return resourceBundle.getString("col_percReg");
			case 6:
				return resourceBundle.getString("col_countRej");
			case 7:
				return resourceBundle.getString("col_percRej");
			}
		}
		//26/09/17 delete Nemlist
		/*if(isReport(4)){
			switch (numCol) {
			case 1:
				return resourceBundle.getString("col_appName");
			case 2:
				return resourceBundle.getString("col_regInNemList");
			case 3:
				return resourceBundle.getString("col_other");
			case 4:
				return resourceBundle.getString("col_total");
			}
		}*/
		if(isReport(5)){
			switch (numCol) {
			case 1:
				return resourceBundle.getString("col_genname");
			case 2:
				return resourceBundle.getString("col_nemlist");
			case 3:
				return resourceBundle.getString("col_total_1");
			}
		}
		if(isReport(6)){
			switch (numCol) {
			case 1:
				return resourceBundle.getString("col_year");
			case 2:
				return resourceBundle.getString("col_quarter");
			case 3:
				return resourceBundle.getString("col_total");
			case 4:
				return resourceBundle.getString("col_prescreenSum");	
			case 5:
				return resourceBundle.getString("col_screenSum");		
			case 6:
				return resourceBundle.getString("col_amendment");
			case 7:
				return resourceBundle.getString("col_amendmentSum");	
			}
		}
		if(isReport(7)){
			switch (numCol) {
			case 1:
				return resourceBundle.getString("col_year");
			case 2:
				return resourceBundle.getString("col_quarter");
			case 3:
				return resourceBundle.getString("col_received");
			case 4:
				return resourceBundle.getString("col_screened");
			case 5:
				return resourceBundle.getString("col_reviewed");	
			case 6:
				return resourceBundle.getString("col_approved");		
			case 7:
				return resourceBundle.getString("col_rejected");				
			}
		}

		return "";
	}
	
	public String valueColumn(int numCol, ItemDashboard item){
		if(item == null)
			return "";
		
		if(isReport(1)){
			switch (numCol) {
			case 1:
				return String.valueOf(item.getYear());
			case 2:
				return String.valueOf(item.getResQuarter());
			case 3:
				return String.valueOf(item.getTotal());
			case 4:
				return String.valueOf(item.getAvg_screening());
			case 5:
				return String.valueOf(item.getAvg_review());
			case 6:
				return String.valueOf(item.getAvg_total());
			}
		}
		if(isReport(2)){
			switch (numCol) {
			case 1:
				return String.valueOf(item.getYear());
			case 2:
				return String.valueOf(item.getResQuarter());
			case 3:
				return String.valueOf(item.getTotal());
			case 4:
				return String.valueOf(item.getCount());
			case 5:
				return String.valueOf(item.getPercent());
			}
		}
		if(isReport(3)){
			switch (numCol) {
			case 1:
				return String.valueOf(item.getYear());
			case 2:
				return String.valueOf(item.getResQuarter());
			case 3:
				return String.valueOf(item.getTotal());
			case 4:
				return String.valueOf(item.getCount());
			case 5:
				return String.valueOf(item.getPercent());
			case 6:
				return String.valueOf(item.getCount_other());
			case 7:
				return String.valueOf(item.getPercent_other());
			}
		}
		//26/09/17 delete Nemlist
		/*if(isReport(4)){
			switch (numCol) {
			case 1:
				return String.valueOf(item.getName());
			case 2:
				return String.valueOf(item.getCount());
			case 3:
				return String.valueOf(item.getCount_other());
			case 4:
				return String.valueOf(item.getTotal());
			}
		}*/
		if(isReport(5)){
			switch (numCol) {
			case 1:
				return String.valueOf(item.getName());
			case 2:
				return String.valueOf(item.getCount());
			case 3:
				return String.valueOf(item.getCount_other());
			}
		}
		if(isReport(6)){
			switch (numCol) {
			case 1:
				return String.valueOf(item.getYear());
			case 2:
				return String.valueOf(item.getResQuarter());
			case 3:
				return String.valueOf(item.getTotal());
			case 4:	
				return String.valueOf(item.getSum_prescreen());	
			case 5:	
				return String.valueOf(item.getSum());	
			case 6:
				return String.valueOf(item.getTotal_amd());
			case 7:
				return String.valueOf(item.getSum_amd());
			}
		}
		if(isReport(7)){
			switch (numCol) {
			case 1:
				return String.valueOf(item.getYear());
			case 2:
				return String.valueOf(item.getResQuarter());
			case 3:
				return String.valueOf(item.getReceived());
			case 4:	
				return String.valueOf(item.getScreened());	
			case 5:	
				return String.valueOf(item.getReviewed());	
			case 6:
				return String.valueOf(item.getApproved());
			case 7:
				return String.valueOf(item.getRejected());
			}
		}
		return "";
	}
	
	public void changeYearQuartType(ItemDashboard item, int type){
		if(item!=null){
			setYear(item.getYear()+"");
			setQuarter(item.getQuarter()+"");
			setType(type);
			if(type==0){
				setHeaderDlg(resourceBundle.getString("col_total"));
			}else{
				setHeaderDlg(resourceBundle.getString("col_amendment"));
			}
		}
	}
		
	public List<ItemDashboard> getListByAppNameFinreportList(){
		List<ItemDashboard> ret = new ArrayList<ItemDashboard>();
		if(!"".equals(getYear()) && !"".equals(getQuarter())){
			if(isShowQuart()){
				ret = dashboardService.getListByAppNameFinreportList(getYear(), getQuarter(), getType());
			}else{				
				ret = dashboardService.getListByAppNameFinreportList(getYear(), "0", getType());
			}			
		}
		return ret;		
	}
	
	public List<ItemDashboard> loadFinreportList(boolean showQuarter){
		list = dashboardService.getListByFinreportList(showQuarter);
		setShowQuart(showQuarter);
		return list;
	}
	
	public String getFinreportTitle(){
		return resourceBundle.getString("title_times");
	}
		
	public List<ItemDashboard> getList() {
		return list;
	}
	public void setList(List<ItemDashboard> list) {
		this.list = list;
	}

	public DashboardService getDashboardService() {
		return dashboardService;
	}

	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

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

	public List<ItemDashboard> getFilteredList() {
		return filteredList;
	}

	public void setFilteredList(List<ItemDashboard> filteredList) {
		this.filteredList = filteredList;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getQuarter() {
		return quarter;
	}

	public void setQuarter(String quarter) {
		this.quarter = quarter;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getHeaderDlg() {
		return headerDlg;
	}

	public void setHeaderDlg(String headerDlg) {
		this.headerDlg = headerDlg;
	}

	public boolean isShowQuart() {
		return showQuart;
	}

	public void setShowQuart(boolean showQuart) {
		this.showQuart = showQuart;
	}
	
	public boolean renderedBtnYearQuarter(){
		if(isReport(1) || isReport(2) || isReport(3) || isReport(6) || isReport(7)){		
			return true;
		}else{
			return false;
		}
	}
	
	public boolean renderedBtnCreateReport(){
		if(isReport(4) || isReport(5)){
			return true;
		}else{
			return false;
		}
	}
	
}