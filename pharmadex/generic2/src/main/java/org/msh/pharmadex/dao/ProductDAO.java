package org.msh.pharmadex.dao;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.msh.pharmadex.dao.iface.WorkspaceDAO;
import org.msh.pharmadex.domain.Atc;
import org.msh.pharmadex.domain.DosUom;
import org.msh.pharmadex.domain.Inn;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.mbean.product.ProdTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by IntelliJ IDEA.
 * User: usrivastava
 * Date: 1/11/12
 * Time: 11:25 PM
 * To change this template use File | Settings | File Templates.
 */
@Repository
@Transactional
public class ProductDAO implements Serializable {

	private static final long serialVersionUID = 6366730721078424594L;
	@PersistenceContext
	EntityManager entityManager;
	@Autowired
	private WorkspaceDAO workspaceDAO;
	@Autowired
	private InnDAO innDAO;

	@Transactional
	public Product findProduct(long id) {
		return (Product) entityManager.createQuery("select p from Product p where p.id = :prodId")
				.setParameter("prodId", id)
				.getSingleResult();
	}

	@Transactional
	public String saveProduct(Product product) {
		if(product != null){
			if(product.getDosUnit() != null){
				if(product.getDosUnit().getId() == 0){
					DosUom du = product.getDosUnit();
					entityManager.persist(du);
					product.setDosUnit(du);
				}
			}
		}
		entityManager.persist(product);
		return "persisted";
	}

	@Transactional
	public Product updateProduct(Product product) {
		try {
			Product prod = entityManager.merge(product);
			return prod;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	@Transactional
	public int removeProduct(Product product){
		if (product==null) return 0;
		if (product.getId()==null) return 0;
		Long id = product.getId();
		int deleted = entityManager.createQuery("select p from Product p where p.id = :prodId")
				.setParameter("prodId", id)
				.executeUpdate();
		return deleted;
	}


	public List<Product> findByName(String name){
		String q = "select p.id as id, p.prod_name as prodName, p.gen_name as genName "
				+ "from product as p "
				+ "where p.gen_name = :prodName or p.prod_name = :prodName";
		List<Product> products = entityManager
				.createNativeQuery(q)
				.setParameter("prodName", name)
				.getResultList();
		return products;
	}

	/**
	 * Make a record for this table
	 * @param prodTables
	 * @param objArr
	 */
	/* 27.09.2017
	public void makeRecord(List<ProdTable> prodTables, Object[] objArr) {
		ProdTable prodTable;
		prodTable = new ProdTable();
		prodTable.setId(Long.valueOf("" + objArr[0]));
		prodTable.setProdName((String) objArr[1]);
		prodTable.setGenName((String) objArr[2]);
		if(objArr[3] == null){
			prodTable.setProdCategory(ProdCategory.UNKNOWN);
		}else{
			if((objArr[3] instanceof ProdCategory)){
				prodTable.setProdCategory((ProdCategory) objArr[3]);
			}else{
				String s = (String) objArr[3];
				prodTable.setProdCategory(ProdCategory.valueOf(s));
			}
		}
		prodTable.setAppName((String) objArr[4]);
		prodTable.setRegDate((Date) objArr[5]);
		prodTable.setRegExpiryDate((Date) objArr[6]);
		prodTable.setManufName((String) objArr[7]);
		prodTable.setRegNo((String) objArr[8]);
		prodTable.setProdDesc((String) objArr[9]);
		prodTable.setProdAppID(Long.valueOf("" + objArr[10]));
		if(objArr[11] == null){
			prodTable.setFnm(YesNoNA.NO);
		}else{
			if(objArr[11] instanceof YesNoNA){
			prodTable.setFnm((YesNoNA)objArr[11]);
			}else{
				String s = (String) objArr[11];
				prodTable.setFnm(YesNoNA.valueOf(s));
			}
		}
		prodTables.add(prodTable);
	}
	 */
	/**
	 * Make a record for this table
	 * @param prodTables
	 * @param objArr
	 */
	public ProdTable createProdTable(Object[] objArr) {
		ProdTable prodTable = new ProdTable();

		prodTable.setAppName((String) objArr[0]);
		prodTable.setProdName((String) objArr[1]);
		prodTable.setInnNames((String) objArr[2]);
		prodTable.setDosForm((String) objArr[3]);
		prodTable.setDosStrength((String) objArr[4]);
		if(objArr[10] != null){
			prodTable.setDosStrength(prodTable.getDosStrength() + "/" + objArr[10]);
		}
		prodTable.setRegNo((String) objArr[5]);
		prodTable.setRegDate((Date) objArr[6]);
		prodTable.setShcedul((String) objArr[7]);
		prodTable.setId(Long.valueOf("" + objArr[8]));
		prodTable.setProdAppID(Long.valueOf("" + objArr[9]));
		return prodTable;
	}



	public String getMinRegistrationYearInDB() {
		String year = "";
		String q = "select min(pa.registrationDate)" +
				" from prodApplications pa" +
				" where pa.regState = :regState ";

		Object y = entityManager
				.createNativeQuery(q)
				.setParameter("regState", "" + RegState.REGISTERED)
				.getSingleResult();

		Date d = (Date) y;
		year = (d.getYear() + 1900) + "";
		return year;
	}

	/**
	 * Register products by filter
	 * @return
	 */
	public List<ProdTable> fetchProductsByFilter(RegState regState, Long innId, Date start, Date end) {
		String innAnd = "";
		if(innId != null && innId > 0){
			Inn inn = innDAO.findInnById(innId);
			innAnd = " and (inn.id = " + innId + " or p.dosage_strength like '%" + inn.getName() + "%'" + ")";
		}

		String dateAnd = "";
		SimpleDateFormat dt = new SimpleDateFormat("yyyyy-MM-dd");
		if(start != null && end != null){
			String st = dt.format(start);
			String en = dt.format(end);
			dateAnd = " and (pa.registrationDate between '" + st + "' and '" + en + "')";
		}else if(start != null && end == null){
			String st = dt.format(start);
			dateAnd = " and pa.registrationDate >= '" + st + "'";
		}else if(start == null && end != null){
			String en = dt.format(end);
			dateAnd = " and pa.registrationDate <= '" + en + "'";
		}

		String rstate = regState + "";

		String sql = "select a.appName, p.prod_name, " +
				"GROUP_CONCAT(DISTINCT inn.`name` ORDER BY inn.`name` ASC SEPARATOR '; ') as 'inns' , " +
				"df.dosageform, p.dosage_strength, pa.prodRegNo, pa.registrationDate, " +
				"GROUP_CONCAT(DISTINCT useCat.useCategory ORDER BY useCat.useCategory ASC SEPARATOR '; ') as 'useCat', " +
				"p.id, pa.id as 'paid', unit.UOM as 'unit'" +
				"from product as p " +
				"join prodapplications as pa on pa.PROD_ID=p.id " +
				"join applicant as a on a.applcntId=pa.APP_ID " +
				"left join dosform as df on p.DOSFORM_ID=df.uid " +
				"left join prodinn as prInn on prInn.prod_id=p.id " +
				"left join inn as inn on prInn.INN_ID=inn.id " +
				"left join tblusecategories as useCat on useCat.prodID=p.id " +
				"left join dosuom as unit on p.DOSUNIT_ID=unit.id "+
				"where pa.active = 1  " +
				"and pa.regState like '" + rstate + "' " +
				innAnd + dateAnd +
				"group by p.id, pa.id " +
				"order by a.appName asc ";

		List<Object[]> products = entityManager.createNativeQuery(sql).getResultList();
		List<ProdTable> prodTables = new ArrayList<ProdTable>();
		ProdTable prodTable;
		for (Object[] objArr : products) {
			prodTable = createProdTable(objArr);
			if(prodTable != null)
				prodTables.add(prodTable);
		}

		return prodTables;
	}

	public List<Product> findProductByFilter(HashMap<String, Object> params) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Product> query = cb.createQuery(Product.class);
		Root<Product> root = query.from(Product.class);

		Predicate p = cb.conjunction();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			p = cb.and(p, cb.equal(root.get(param.getKey()), param.getValue()));
		}

		if (params.size() > 0)
			query.select(root).where(p);
		else
			query.select(root);
		return entityManager.createQuery(query).getResultList();
	}

	public List<Atc> findAtcsByProduct(Long id) {
		return (List<Atc>) entityManager.createQuery("select atc from Atc atc join atc.products p where p.id = :prodId ")
				.setParameter("prodId", id)
				.getResultList();
	}

	public List<Product> findRegProductByApp(Long appID) {
		return (List<Product>) entityManager.createQuery(" select p from Product p left join p.prodApplicationses pa left join pa.applicant a where a.applcntId = :appID")
				.setParameter("appID", appID)
				.getResultList();
	}

	public List<Product> findRegProductByApplicants(List<Long> appIDs) {		
		return (List<Product>) entityManager.createQuery("select p from Product p left join p.prodApplicationses pa left join pa.applicant a where a.applcntId in(:appIDs) and pa.regState=:regState")
				.setParameter("appIDs", appIDs)
				.setParameter("regState", RegState.REGISTERED)
				.getResultList();
	}

	public List<Product> findRegProductByApplicants(List<Long> appIDs, String year) {		
		return (List<Product>) entityManager.createQuery("select p from Product p left join p.prodApplicationses pa left join pa.applicant a where a.applcntId in(:appIDs) and pa.regState=:regState"
				+ " and year(pa.regExpiryDate)=:year")
				.setParameter("appIDs", appIDs)
				.setParameter("regState", RegState.REGISTERED)
				.setParameter("year", Integer.parseInt(year))
				.getResultList();
	}

	/**
	 * Register products by Applicants
	 * @return List<ProdTable> all REGISTERED product this applicant which have select Product Category
	 */
	public List<ProdTable> findProdTableByApplicants(Long appIDs, ProdCategory category, String year) {

		String sql = "select p.prod_name, " +
				"GROUP_CONCAT(DISTINCT inn.`name` ORDER BY inn.`name` ASC SEPARATOR '; ') as 'inns' , " +
				"df.dosageform, p.dosage_strength, pa.prodRegNo, pa.id, " +
				"GROUP_CONCAT(DISTINCT useCat.useCategory ORDER BY useCat.useCategory ASC SEPARATOR '; ') as 'useCat', " +
				"p.id, pa.id as 'paid', unit.UOM as 'unit'" +
				"from product as p " +
				"join prodapplications as pa on pa.PROD_ID=p.id " +
				"join applicant as a on a.applcntId=pa.APP_ID " +
				"left join dosform as df on p.DOSFORM_ID=df.uid " +
				"left join prodinn as prInn on prInn.prod_id=p.id " +
				"left join inn as inn on prInn.INN_ID=inn.id " +
				"left join tblusecategories as useCat on useCat.prodID=p.id " +
				"left join dosuom as unit on p.DOSUNIT_ID=unit.id "+
				"where (year(pa.regExpiryDate)<=" +year +" or pa.regExpiryDate is null) "+
				"and pa.regState like '" + RegState.REGISTERED + "' " +	
				"and (p.prod_cat like '" + category.name() + "' or p.prod_cat like '%UNKNOWN%') " +	
				"and a.applcntId =" +appIDs +" "+
				"group by p.id, pa.id " +
				"order by p.prod_name asc ";

		List<Object[]> products = entityManager.createNativeQuery(sql).getResultList();
		List<ProdTable> prodTables = new ArrayList<ProdTable>();
		ProdTable prodTable;
		for (Object[] objArr : products) {
			prodTable = createRegProdByApplicantIdTable(objArr);
			if(prodTable != null)
				prodTables.add(prodTable);
		}

		return prodTables;
	}

	/**
	 * Make a record for this table
	 * @param prodTables
	 * @param objArr
	 */
	public ProdTable createRegProdByApplicantIdTable(Object[] objArr) {
		ProdTable prodTable = new ProdTable();

		prodTable.setProdName((String) objArr[0]);
		prodTable.setInnNames((String) objArr[1]);
		prodTable.setDosForm((String) objArr[2]);
		prodTable.setDosStrength((String) objArr[3]);
		prodTable.setRegNo((String) objArr[4]);			
		prodTable.setProdAppID(Long.valueOf("" + objArr[5]));
		return prodTable;
	}

	public List<Product> findRegAllProduct() {
		String sql = "select p " +
				"from product as p " +
				"join prodapplications as pa on pa.PROD_ID=p.id " +
				"join applicant as a on a.applcntId=pa.APP_ID " +
				"left join dosform as df on p.DOSFORM_ID=df.uid " +
				"left join prodinn as prInn on prInn.prod_id=p.id " +
				"left join inn as inn on prInn.INN_ID=inn.id " +
				"left join tblusecategories as useCat on useCat.prodID=p.id " +
				"where pa.active = 1  " +
				"and pa.regState like '" + RegState.REGISTERED + "' " +
				"group by p.id, pa.id " +
				"order by a.appName asc ";

		List<Product> products = entityManager.createNativeQuery(sql).getResultList();
		return products;
	}

	@Transactional
	public Product findProductEager(Long prodId) {
		/*
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicateList = new ArrayList<Predicate>();
        Predicate p = cb.equal(root.get("id"), prodId);
        predicateList.add(p);

        query.where(p);
        Product prod = entityManager.createQuery(query).getSingleResult();
		 */
		Product prod = findProduct(prodId);
		Hibernate.initialize(prod.getInns());
		Hibernate.initialize(prod.getAtcs());
		Hibernate.initialize(prod.getExcipients());
		Hibernate.initialize(prod.getProdCompanies());
		Hibernate.initialize(prod.getDosUnit());
		Hibernate.initialize(prod.getDosForm());
		Hibernate.initialize(prod.getPricing());
		Hibernate.initialize(prod.getUseCategories());

		if (prod.getPricing() != null) {
			Hibernate.initialize(prod.getPricing().getDrugPrices());
		}
		return prod;
	}

	/*27.09.2017
	 * public int findCountRegProduct() {
		List<RegState> regStates = new ArrayList<RegState>();
		regStates.add(RegState.REGISTERED);
		regStates.add(RegState.DISCONTINUED);
		regStates.add(RegState.XFER_APPLICANCY);

		Long count = (Long) entityManager.createQuery("select count(p.id) from Product p left join p.prodApplicationses pa where pa.regState in :regStates and pa.active = true")
				.setParameter("regStates", regStates).getSingleResult();

		return count.intValue();
	}*/

}

