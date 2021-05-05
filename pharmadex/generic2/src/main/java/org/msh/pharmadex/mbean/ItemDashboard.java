package org.msh.pharmadex.mbean;

import static javax.faces.context.FacesContext.getCurrentInstance;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

public class ItemDashboard implements Serializable{

	private static final long serialVersionUID = -435959110097842685L;
	
	private int year = 0;
	private int quarter = 0;
	private String resQuarter ="";
	private int total = 0;
	private int count = 0;
	private double percent = 0.00;
	
	private int count_other = 0;// for report 3 by Reject
	private double percent_other = 0;// for report 3 by Reject
	
	private double avg_screening = 0;
	private double avg_review = 0;
	private double avg_total = 0;
	
	private String name = "";
	
	private Long sum = 0L;
	private Long sum_prescreen = 0L;
	private int total_amd = 0;
	private Long sum_amd = 0L;
	private String app_name = "";
	private String aplcn_id = "";
	
	private int received = 0;
	private int screened = 0;
	private int reviewed = 0;
	private int approved = 0;
	private int rejected = 0;
	
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
	public long getQuarter() {
		return quarter;
	}
	public void setQuarter(int quarter) {
		this.quarter = quarter;
	}
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public double getPercent() {
		percent = 0;
		if(total > 0 && count >= 0){
			double val = count*100.00/total;
			percent = round(val, 2);
		}
		
		return percent;
	}
	public void setPercent(double percent) {
		this.percent = percent;
	}
	
	public int getCount_other() {
		return count_other;
	}
	public void setCount_other(int count_other) {
		this.count_other = count_other;
	}
	
	public double getPercent_other() {
		percent_other = 0;
		if(total > 0 && count_other >= 0){
			double val = count_other*100.00/total;
			percent_other = round(val, 2);
		}
		
		return percent_other;
	}
	public void setPercent_other(double percent_other) {
		this.percent_other = percent_other;
	}
	
	public double getAvg_screening() {
		avg_screening = round(avg_screening, 2);
		return avg_screening;
	}
	public void setAvg_screening(double avg_screening) {
		this.avg_screening = avg_screening;
	}
	
	public double getAvg_review() {
		avg_review = round(avg_review, 2);
		return avg_review;
	}
	public void setAvg_review(double avg_review) {
		this.avg_review = avg_review;
	}
	
	public double getAvg_total() {
		avg_total = round(avg_total, 2);
		return avg_total;
	}
	public void setAvg_total(double avg_total) {
		this.avg_total = avg_total;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String n) {
		this.name = n;
	}
	public double round(double val, int col){
		double value = new BigDecimal(val).setScale(col, RoundingMode.UP).doubleValue();
		return value;
	}
	public Long getSum() {
		return sum;
	}
	public void setSum(Long sum) {
		this.sum = sum;
	}
	public int getTotal_amd() {
		return total_amd;
	}
	public void setTotal_amd(int total_amd) {
		this.total_amd = total_amd;
	}
	public Long getSum_amd() {
		return sum_amd;
	}
	public void setSum_amd(Long sum_amd) {
		this.sum_amd = sum_amd;
	}
	public String getApp_name() {
		return app_name;
	}
	public void setApp_name(String app_name) {
		this.app_name = app_name;
	}
	public String getAplcn_id() {
		return aplcn_id;
	}
	public void setAplcn_id(String aplcn_id) {
		this.aplcn_id = aplcn_id;
	}
	
	public String getResQuarter() {
		FacesContext facesContext = getCurrentInstance();
		ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");
		long q = getQuarter();
		if(q==1){
			resQuarter =  resourceBundle.getString("Quarter.First");			
		}else if (q==2){
			resQuarter = resourceBundle.getString("Quarter.Second");			
		}else if (q==3){
			resQuarter = resourceBundle.getString("Quarter.Third");			
		}else if (q==4){
			resQuarter = resourceBundle.getString("Quarter.Fought");
		}else{
			resQuarter = resourceBundle.getString("AllQuarter");
		}
		return resQuarter;
	}
	
	public void setResQuarter(String resQuarter) {
		this.resQuarter = resQuarter;
	}
	public Long getSum_prescreen() {
		return sum_prescreen;
	}
	public void setSum_prescreen(Long sum_prescreen) {
		this.sum_prescreen = sum_prescreen;
	}
	public int getReceived() {
		return received;
	}
	public void setReceived(int received) {
		this.received = received;
	}
	public int getScreened() {
		return screened;
	}
	public void setScreened(int screened) {
		this.screened = screened;
	}
	public int getReviewed() {
		return reviewed;
	}
	public void setReviewed(int reviewed) {
		this.reviewed = reviewed;
	}
	public int getApproved() {
		return approved;
	}
	public void setApproved(int approved) {
		this.approved = approved;
	}
	public int getRejected() {
		return rejected;
	}
	public void setRejected(int rejected) {
		this.rejected = rejected;
	}	
	
}
