package org.msh.pharmadex.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.mbean.ItemDashboard;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class DashboardDAO implements Serializable {

	private static final long serialVersionUID = -7177773420978773061L;
	@PersistenceContext
	EntityManager entityManager;

	public List<ItemDashboard> getListTimesProcess(boolean showQuarter) {
		List<ItemDashboard> items = null;
		
		String sql = "SELECT"
					+ " Year(appEnd.statusDate) as y, "
					+ (showQuarter ? "quarter(appEnd.statusDate) as q,":"0,")
					+ " count(DISTINCT pa.id) as totalApps,"
					+ " avg(business_days(scrStart.statusDate, scrEnd.statusDate)) as 'screening', "
					+ " avg(review_days(pa.id, revStart.statusDate, revEnd.statusDate)) as 'review',"
					+ " avg(business_days(appStart.statusDate, appEnd.statusDate)) as 'total'"
					+ " FROM prodapplications pa"
					+ " JOIN timeline appStart on pa.ID = appStart.PROD_APP_ID  and appStart.regState='" + RegState.MS_START + "'"
					+ " JOIN timeline scrStart on pa.ID = scrStart.PROD_APP_ID  and scrStart.regState='" + RegState.MS_SCR_START + "'"
					+ " JOIN timeline scrEnd on pa.ID = scrEnd.PROD_APP_ID and scrEnd.regState='" + RegState.MS_SCR_END + "'"
					+ " LEFT JOIN timeline revStart on pa.ID = revStart.PROD_APP_ID and revStart.regState='" + RegState.MS_REV_START + "'"
					+ " LEFT JOIN timeline revEnd on pa.ID = revEnd.PROD_APP_ID and revEnd.regState='" + RegState.MS_REV_END + "'"
					+ " JOIN timeline appEnd on pa.ID = appEnd.PROD_APP_ID and appEnd.regState='" + RegState.MS_END + "'"
					+ " where pa.regState='" + RegState.REGISTERED + "' or pa.regState='" + RegState.REJECTED + "'"
					+ " Group by y DESC"
					+ (showQuarter ? ",q DESC":"");

		List<Object[]> list = entityManager.createNativeQuery(sql).getResultList();
		if(list != null && list.size() > 0){
			items = new ArrayList<ItemDashboard>();
			for(Object[] val:list){
				ItemDashboard it = new ItemDashboard();
				it.setYear((Integer)val[0]);
				if(val[1] instanceof BigInteger)
					it.setQuarter(((BigInteger)val[1]).intValue());
				else
					it.setQuarter((Integer)val[1]);
				it.setTotal(((BigInteger)val[2]).intValue());
				it.setAvg_screening(((BigDecimal)val[3]).doubleValue());
				it.setAvg_review(((BigDecimal)val[4]).doubleValue());
				it.setAvg_total(((BigDecimal)val[5]).doubleValue());

				items.add(it);
			}
		}

		return items;
	}
	
	public List<ItemDashboard> getListByPercentNemList(boolean showQuarter) {
		List<ItemDashboard> items = null;

		String prodCat = ProdCategory.HUMAN + "";
		String regState = RegState.REGISTERED + "";

		String sql = "select year(pa.registrationDate), "
				+(showQuarter ? "quarter(pa.registrationDate),":"0,")
				+ " count(pa.id), count(p.id)"
				+ " FROM prodapplications pa"
				+ " left join product p on p.id=pa.PROD_ID and p.fnm='YES' and p.prod_cat like '" + prodCat + "'"
				+ " where pa.regState like '" + regState + "'"+" and pa.registrationDate is not null"
				+ " group by year(pa.registrationDate) DESC"
				+(showQuarter ? ", quarter(pa.registrationDate) DESC":"");

		List<Object[]> list = entityManager.createNativeQuery(sql).getResultList();
		if(list != null && list.size() > 0){
			items = new ArrayList<ItemDashboard>();
			for(Object[] val:list){
				ItemDashboard it = new ItemDashboard();
				it.setYear((Integer)val[0]);
				if(val[1] instanceof BigInteger)
					it.setQuarter(((BigInteger)val[1]).intValue());
				else
					it.setQuarter((Integer)val[1]);
				it.setTotal(((BigInteger)val[2]).intValue());
				it.setCount(((BigInteger)val[3]).intValue());

				items.add(it);
			}
		}

		return items;
	}

	public List<ItemDashboard> getListByPercentApprovedList(boolean showQuarter) {
		List<ItemDashboard> items = null;

		String regStateReg = RegState.REGISTERED + "";
		String regStateRej = RegState.REJECTED + "";

		String sql = "select year(pa.registrationDate),"
				+(showQuarter ? " quarter(pa.registrationDate),":"0,")
				+ " count(pa.id)"
				+ ",  Sum(IF(pa.regState like '" + regStateReg + "', 1, 0))"
				+ ",  Sum(IF(pa.regState like '" + regStateRej + "', 1, 0))"
				+ " FROM prodapplications pa"
				+ " where pa.regState like '" + regStateReg + "'"
				+ " or pa.regState like '" + regStateRej + "'"
				+ " group by year(pa.registrationDate) DESC"
				+(showQuarter ? ", quarter(pa.registrationDate) DESC":"");

		List<Object[]> list = entityManager.createNativeQuery(sql).getResultList();
		if(list != null && list.size() > 0){
			items = new ArrayList<ItemDashboard>();
			for(Object[] val:list){
				ItemDashboard it = new ItemDashboard();
				if(val[0] != null){
					it.setYear((Integer)val[0]);
					if(val[1] instanceof BigInteger)
						it.setQuarter(((BigInteger)val[1]).intValue());
					else
						it.setQuarter((Integer)val[1]);
					it.setTotal(((BigInteger)val[2]).intValue());
					it.setCount(((BigDecimal)val[3]).intValue());
					it.setCount_other(((BigDecimal)val[4]).intValue());
					items.add(it);
				}
			}
		}

		return items;
	}
	//26/09/17 delete Nemlist
	/*public List<ItemDashboard> getListByApplicant() {
		List<ItemDashboard> items = null;

		String regState = RegState.REGISTERED + "";
		
		String sql = "select a.appName, count(p.id),"
				+ " count(pa.id) - count(p.id),"
				+ " count(pa.id)"
				+ " FROM prodapplications pa"
				+ " left join product p on p.id=pa.PROD_ID and (p.fnm is not null and length(trim(p.fnm))>0)"
				+ " left join applicant a on a.applcntId=pa.APP_ID"
				+ " where pa.regState like '" + regState + "'"
				+ " group by a.appName ASC";
		 
		List<Object[]> list = entityManager.createNativeQuery(sql).getResultList();
		if(list != null && list.size() > 0){
			items = new ArrayList<ItemDashboard>();
			for(Object[] val:list){
				ItemDashboard it = new ItemDashboard();
				if(val[0] != null){
					it.setName((String)val[0]);
					it.setCount(((BigInteger)val[1]).intValue());
					it.setCount_other(((BigInteger)val[2]).intValue());
					it.setTotal(((BigInteger)val[3]).intValue());
					items.add(it);
				}
			}
		}

		return items;
	}*/

	public List<ItemDashboard> getListByGenName() {
		List<ItemDashboard> items = null;

		String regState = RegState.REGISTERED + "";
		
		/*String sql = "select p.gen_name, count(p.id), count(distinct(a.applcntId))"
				+ " FROM prodapplications pa"
				+ " left join product p on p.id=pa.PROD_ID and (p.fnm is not null and length(trim(p.fnm))>0)"
				+ " left join applicant a on a.applcntId=pa.APP_ID"
				+ " where pa.regState like '" + regState + "' and not isnull(p.gen_name)"
				+ " group by p.gen_name ASC";*/
		
		String sql = "select active.name, count(p.id), count(distinct(a.applcntId))"
				+ " FROM inn active"
				+ " join prodinn prin on prin.INN_ID = active.id"
				+ " join product p on p.id = prin.prod_id"
				+ " join prodapplications pa on pa.PROD_ID=p.id and (p.fnm is not null and length(trim(p.fnm))>0)"
				+ " join applicant a on a.applcntId=pa.APP_ID"
				+ " where pa.regState like '" + regState + "'"
				+ " group by active.name ASC";
		
		List<Object[]> list = entityManager.createNativeQuery(sql).getResultList();
		if(list != null && list.size() > 0){
			items = new ArrayList<ItemDashboard>();
			for(Object[] val:list){
				ItemDashboard it = new ItemDashboard();
				if(val[0] != null){
					it.setName((String)val[0]);
					it.setCount(((BigInteger)val[1]).intValue());
					it.setCount_other(((BigInteger)val[2]).intValue());
					items.add(it);
				}
			}
		}

		return items;
	}
	
	public List<ItemDashboard> getListByFinreportList(boolean showQuarter) {
		HashMap<String, ItemDashboard> prescreen = new HashMap<String, ItemDashboard>();
		HashMap<String, ItemDashboard> amendment = new HashMap<String, ItemDashboard>();
		
		List<ItemDashboard> items = new ArrayList<ItemDashboard>();
		
        String sql = "select year(pa.feeSubmittedDt), "
        			+(showQuarter ? "quarter(pa.feeSubmittedDt), ":"0,")			
    				+ "count(pa.id), Sum(pa.feeSum), GROUP_CONCAT(pa.id) "
    				+ "FROM prodapplications pa "
    				+ "where pa.feeSum is not null and pa.feeReceived is true "
    				+ "group by year(pa.feeSubmittedDt) DESC"
    				+(showQuarter ? ", quarter(pa.feeSubmittedDt) DESC":"");
        
		String sqlPrescreen = "select year(pa.prescreenfeeSubmittedDt), "
				+(showQuarter ? "quarter(pa.prescreenfeeSubmittedDt), ":"0,")								
				+ "count(pa.id), Sum(pa.prescreenFeeSum), GROUP_CONCAT(pa.id) "				
				+ "FROM prodapplications pa "
				+ "where pa.prescreenFeeSum is not null and pa.prescreenfeeReceived is true "				
				+ "group by year(pa.prescreenfeeSubmittedDt) DESC"
				+(showQuarter ? ", quarter(pa.prescreenfeeSubmittedDt) DESC":"");
		
		String sqlAmendment= "select year(amd.payment_payDate), "
				+(showQuarter ? "quarter(amd.payment_payDate), ":"0,")								
				+ "count(amd.id), Sum(amd.feeSum), GROUP_CONCAT(amd.id) "				
				+ "FROM amendment amd "
				+ "where amd.feeSum is not null and amd.payment_received is true "				
				+ "group by year(amd.payment_payDate) DESC"
				+(showQuarter ? ", quarter(amd.payment_payDate) DESC":"");
				
		List<Object[]> list = entityManager.createNativeQuery(sql).getResultList();
		if(list != null && list.size() > 0){	
			items = new ArrayList<ItemDashboard>();
			for(Object[] val:list){
				ItemDashboard it = new ItemDashboard();
				if(val[0] != null){
					it.setYear(((BigInteger)val[0]).intValue());
					if(val[1] instanceof BigInteger)
						it.setQuarter(((BigInteger)val[1]).intValue());
					else
						it.setQuarter(((BigInteger)val[1]).intValue());								
					it.setTotal(((BigInteger)val[2]).intValue());
					it.setSum(((BigDecimal)val[3]).longValue());					
					items.add(it);
				}
			}
		}
		List<Object[]> listPrescreen = entityManager.createNativeQuery(sqlPrescreen).getResultList();
		if(listPrescreen != null && listPrescreen.size() > 0){				
			for(Object[] val:listPrescreen){
				ItemDashboard it = new ItemDashboard();
				if(val[0] != null){					
					String key  = ((BigInteger)val[1]).intValue()+""+((BigInteger)val[0]).intValue()+""; //квартал+год
					it.setYear(((BigInteger)val[0]).intValue());
					if(val[1] instanceof BigInteger)
						it.setQuarter(((BigInteger)val[1]).intValue());
					else
						it.setQuarter(((BigInteger)val[1]).intValue());				
					it.setTotal(((BigInteger)val[2]).intValue());
					it.setSum_prescreen(((BigDecimal)val[3]).longValue());	//it.setSum(((BigDecimal)val[3]).longValue());	
					prescreen.put(key, it);
					
				}
			}
		}
		List<Object[]> listAmendment = entityManager.createNativeQuery(sqlAmendment).getResultList();
		if(listAmendment != null && listAmendment.size() > 0){			
			for(Object[] val:listAmendment){
				ItemDashboard it = new ItemDashboard();
				if(val[0] != null){					
					String key  = ((BigInteger)val[1]).intValue()+""+((BigInteger)val[0]).intValue()+""; //квартал+год
					it.setYear(((BigInteger)val[0]).intValue());
					if(val[1] instanceof BigInteger)
						it.setQuarter(((BigInteger)val[1]).intValue());
					else
						it.setQuarter(((BigInteger)val[1]).intValue());							
					it.setTotal_amd(((BigInteger)val[2]).intValue());
					it.setSum_amd(((BigDecimal)val[3]).longValue());	
					amendment.put(key, it);					
				}
			}
		}		

		for(ItemDashboard item: items){
			String key = item.getQuarter()+""+item.getYear()+"";
			ItemDashboard prc = prescreen.get(key);
			ItemDashboard amd = amendment.get(key);
			if(prc!=null && amd!=null){
				//1+2+3
				item.setTotal(item.getTotal()+prc.getTotal());
				item.setSum(item.getSum());//prc.getSum()
				item.setSum_prescreen(prc.getSum_prescreen());

				item.setTotal_amd(amd.getTotal_amd());
				item.setSum_amd(amd.getSum_amd());
				
				prescreen.remove(key);
				amendment.remove(key);
				
			}else if (prc!=null){
				//1+2
				item.setTotal(item.getTotal()+prc.getTotal());				
				item.setSum(item.getSum());//+prc.getSum()
				item.setSum_prescreen(prc.getSum_prescreen());
				
				prescreen.remove(key);				
			}else if (amd!=null){
				//1+3
				item.setTotal_amd(amd.getTotal_amd());
				item.setSum_amd(amd.getSum_amd());
				
				amendment.remove(key);
			}
		}
		if(prescreen!=null)
			if(prescreen.size()>0){
				for (String key : prescreen.keySet()) {
					ItemDashboard prc = prescreen.get(key);
					items.add(prc);
				}
			}
		if(amendment!=null)
			if(amendment.size()>0){
				for (String key : amendment.keySet()) {
					ItemDashboard amd = amendment.get(key);
					items.add(amd);
				}
			}		
		
		//sort
		Collections.sort(items, new Comparator<ItemDashboard>() {
			@Override
			public int compare(ItemDashboard o1, ItemDashboard o2) {
				int key1  = Integer.valueOf(o1.getYear()+ ""+ o1.getQuarter());
				int key2  = Integer.valueOf(o2.getYear()+ ""+ o2.getQuarter());
				return key1 > key2 ? -1 : (key1 < key2 ) ? 1 : 0;
			}
		});
		return items;
	}
	
	public List<ItemDashboard> getListByAppNameFinreportList(String year, String quart, int type) {	
		List<ItemDashboard> res = new ArrayList<ItemDashboard>();
		List<ItemDashboard> items = new ArrayList<ItemDashboard>();		
		HashMap<String, ItemDashboard> screen = new HashMap<String, ItemDashboard>();
		
		String sqlPrescreen = "SELECT year(pa.prescreenfeeSubmittedDt) as 'y', "
				+(!"0".equals(quart)?"quarter(pa.prescreenfeeSubmittedDt) as 'q',":"0,")
				+" Sum(pa.prescreenFeeSum), appl.appName, appl.applcntId"
				+" FROM pdx_na.prodapplications pa"
                +" LEFT JOIN pdx_na.applicant appl on appl.applcntId= pa.APP_ID"
				+" where pa.prescreenFeeSum is not null and pa.prescreenfeeReceived is true "
				+" group by y DESC"
				+(!"0".equals(quart)?", q DESC":"")
				+", appl.applcntId"
				+" having y='" + year +"' "
				+(!"0".equals(quart)?"and q='" + quart + "'":"")
				+";";	
		
		String sqlScreen = "SELECT year(pa.feeSubmittedDt) as 'y', "
				+(!"0".equals(quart)?"quarter(pa.feeSubmittedDt) as 'q',":"0,")
				+ " Sum(pa.feeSum), appl.appName, appl.applcntId"
				+ " FROM pdx_na.prodapplications pa"
				+ " LEFT JOIN pdx_na.applicant appl on appl.applcntId= pa.APP_ID"
				+ " where pa.feeSum is not null and pa.feeReceived is true " 
				+ " group by y DESC"
				+(!"0".equals(quart)?", q DESC":"")
				+", appl.applcntId"
				+" having y='" + year +"' "
				+(!"0".equals(quart)?"and q='" + quart + "'":"")
				+";";
		
		String sqlAmendment = "SELECT year(amd.payment_payDate) as 'y', "
				+(!"0".equals(quart)?"quarter(amd.payment_payDate) as 'q',":"0,")
				+ " Sum(amd.feeSum), appl.appName"                
				+ " FROM pdx_na.amendment amd"
                + " LEFT JOIN pdx_na.applicant appl on appl.applcntId= amd.applcntId"
				+ " where amd.feeSum is not null and amd.payment_received is true " 			
				+ " group by year(amd.payment_payDate) DESC"
				+(!"0".equals(quart)?", quarter(amd.payment_payDate) DESC":"")
				+", amd.applcntId"
				+" having y='" + year +"'"
				+(!"0".equals(quart)?" and q='" + quart + "'":"")
				+";";
		
		if(type==0){//ProdApplicationses  prescreenFeeSum
			List<Object[]> list = entityManager.createNativeQuery(sqlPrescreen).getResultList();
			if(list != null && list.size() > 0){					
				for(Object[] val:list){
					ItemDashboard it = new ItemDashboard();
					if(val[0] != null){	
						it.setAplcn_id((BigInteger)val[4]+"");
						it.setSum(((BigDecimal)val[2]).longValue());						
						it.setApp_name((String)val[3]);
						items.add(it);
					}
				}
			}
		}else if (type == 1 ){//ProdApplicationses feeSum
			List<Object[]> listScreen = entityManager.createNativeQuery(sqlScreen).getResultList();
			if(listScreen != null && listScreen.size() > 0){				
				for(Object[] val:listScreen){
					ItemDashboard it = new ItemDashboard();
					if(val[0] != null){						
						it.setAplcn_id((BigInteger)val[4]+"");
						it.setSum(((BigDecimal)val[2]).longValue());	
						it.setApp_name((String)val[3]);						
						items.add(it);
					}
				}
			}
		}else{//Amendmnet
			List<Object[]> listAmendment = entityManager.createNativeQuery(sqlAmendment).getResultList();
			if(listAmendment != null && listAmendment.size() > 0){			
				for(Object[] val:listAmendment){
					ItemDashboard it = new ItemDashboard();
					if(val[0] != null){
						it.setSum(((BigDecimal)val[2]).longValue());	
						it.setApp_name((String)val[3]);
						items.add(it);					
					}
				}
			}
		}		
		return items;		
	}
	/**
	 * year
	 * Quarter/period (dateX to dateY) 
	 * Total dossier received
	 * Total dossiers Screened
	 * Total dosser Reviewed
	 * Total Dossier approved
	 * Total dossier rejected
	 * Total dossier backlog
	 * @param showQuarter
	 * @return
	 */
	public List<ItemDashboard> getListTotalDossier(boolean showQuarter) {
		List<ItemDashboard> items = null;
		HashMap<String, ItemDashboard> regRej = new HashMap<String, ItemDashboard>();
		
		 String sqlTimeLine = "select year(tl.statusDate)"
     			+(showQuarter ? ", quarter(tl.statusDate)":", 0")			
 				+ ", Sum(IF(tl.regState like '" + RegState.MS_START + "', 1, 0))"
     			+", Sum(IF(tl.regState like '" + RegState.MS_SCR_END + "', 1, 0))"
 				+", Sum(IF(tl.regState like '" + RegState.MS_REV_END + "', 1, 0))"
 				+ " FROM timeline tl"
 				+ " where tl.regState='" + RegState.MS_START +  "' or tl.regState='" + RegState.MS_SCR_END +  "' or tl.regState='" + RegState.MS_REV_END + "'"
 				+ " group by year(tl.statusDate) DESC"
 				+(showQuarter ? ", quarter(tl.statusDate) DESC":"");

		 List<Object[]> list = entityManager.createNativeQuery(sqlTimeLine).getResultList();
			if(list != null && list.size() > 0){
				items = new ArrayList<ItemDashboard>();
				for(Object[] val:list){
					ItemDashboard it = new ItemDashboard();
					it.setYear((Integer)val[0]);
					if(val[1] instanceof BigInteger)
						it.setQuarter(((BigInteger)val[1]).intValue());
					else
						it.setQuarter((Integer)val[1]);
					
					it.setReceived(((BigDecimal)val[2]).intValue());
					it.setScreened(((BigDecimal)val[3]).intValue());
					it.setReviewed(((BigDecimal)val[4]).intValue());
									 
					items.add(it);
				}
			}
			
		String regStateReg = RegState.REGISTERED + "";
		String regStateRej = RegState.REJECTED + "";

		String sqlRegRej = "select year(pa.registrationDate) "
					+(showQuarter ? ", quarter(pa.registrationDate)":", 0")
					+ ", Sum(IF(pa.regState like '" + regStateReg + "', 1, 0))"
					+ ", Sum(IF(pa.regState like '" + regStateRej + "', 1, 0))"
					+ " FROM prodapplications pa"
					+ " where pa.registrationDate is not null and (pa.regState like '" + regStateReg + "'"
					+ " or pa.regState like '" + regStateRej + "')"
					+ " group by year(pa.registrationDate) DESC"
					+(showQuarter ? ", quarter(pa.registrationDate) DESC":"");
					
		List<Object[]> listRegRej = entityManager.createNativeQuery(sqlRegRej).getResultList();
		if(listRegRej != null && listRegRej.size() > 0){			
			for(Object[] val:listRegRej){
				String key  = "";
				if(val[1] instanceof BigInteger){
					key = ((BigInteger)val[1]).intValue()+""+((Integer)val[0]).intValue()+""; //квартал+год
				}else{
					key = ((Integer)val[1]).intValue()+""+((Integer)val[0]).intValue()+""; //квартал+год
				}
				
				
				ItemDashboard it = new ItemDashboard();
				it.setYear((Integer)val[0]);
				if(val[1] instanceof BigInteger)
					it.setQuarter(((BigInteger)val[1]).intValue());
				else
					it.setQuarter((Integer)val[1]);
								
				it.setApproved(((BigDecimal)val[2]).intValue());
				it.setRejected(((BigDecimal)val[3]).intValue());	
				regRej.put(key, it);				
			}
		}
		
		for(ItemDashboard item: items){
			String key = item.getQuarter()+""+item.getYear()+"";
			ItemDashboard registerReject = regRej.get(key);
			
			if(registerReject!=null){
				//1+2
				item.setApproved(registerReject.getApproved());
				item.setRejected(registerReject.getRejected());			
								
				regRej.remove(key);	
			}
		}
		if(regRej!=null)
			if(regRej.size()>0){
				for (String key : regRej.keySet()) {
					ItemDashboard rr = regRej.get(key);
					items.add(rr);
				}
			}
		//sort
		Collections.sort(items, new Comparator<ItemDashboard>() {
			@Override
			public int compare(ItemDashboard o1, ItemDashboard o2) {
				int key1  = Integer.valueOf(o1.getYear()+ ""+ o1.getQuarter());
				int key2  = Integer.valueOf(o2.getYear()+ ""+ o2.getQuarter());
				return key1 > key2 ? -1 : (key1 < key2 ) ? 1 : 0;
			}
		});
		return items;
	}
}
