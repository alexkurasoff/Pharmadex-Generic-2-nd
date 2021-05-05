package org.msh.pharmadex.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.msh.pharmadex.dao.ApplicantDAO;
import org.msh.pharmadex.dao.InnDAO;
import org.msh.pharmadex.dao.ProductDAO;
import org.msh.pharmadex.dao.iface.AtcDAO;
import org.msh.pharmadex.dao.iface.DosUomDAO;
import org.msh.pharmadex.dao.iface.DrugPriceDAO;
import org.msh.pharmadex.dao.iface.PricingDAO;
import org.msh.pharmadex.dao.iface.WorkspaceDAO;
import org.msh.pharmadex.domain.Atc;
import org.msh.pharmadex.domain.DosageForm;
import org.msh.pharmadex.domain.DrugPrice;
import org.msh.pharmadex.domain.PcCode;
import org.msh.pharmadex.domain.Pricing;
import org.msh.pharmadex.domain.ProdAppChecklist;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdCompany;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.Workspace;
import org.msh.pharmadex.domain.enums.CompanyType;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.mbean.product.ProdTable;
import org.msh.pharmadex.util.RetObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Author: usrivastava
 */
@Service
@Transactional
public class ProductService implements Serializable {

    private static final long serialVersionUID = -5511467617579154680L;
    @Autowired
    ApplicantDAO applicantDAO;
    @Autowired
    UserService userService;
    @Autowired
    private ProductDAO productDAO;
    @Autowired
    private InnDAO innDAO;
    @Autowired
    private AtcDAO atcDAO;
    @Autowired
    private ProdApplicationsService prodApplicationsService;
    @Autowired
    private WorkspaceDAO workspaceDAO;
    @Autowired
    private PricingDAO pricingDAO;
    @Autowired
    private DrugPriceDAO drugPriceDAO;

    @Autowired
    private DosUomDAO dosUomDAO;

    public int getSizeAllRegisteredProduct(){
    	List<ProdTable> list = productDAO.fetchProductsByFilter(RegState.REGISTERED, null, null, null);
    	if(list != null)
    		return list.size();
    	return 0;
    }
    
    public List<ProdTable> findAllRegisteredProduct() {
        return productDAO.fetchProductsByFilter(RegState.REGISTERED, null, null, null);
    }
    
    public List<Product> findAllRegisterProduct() {
        return productDAO.findRegAllProduct();
    }
    
    public String getMinRegistrationYearInDB() {
    	return productDAO.getMinRegistrationYearInDB();
    }
    
    public List<ProdTable> findRegisteredProduct(Long innId, Date start, Date end) {
        return productDAO.fetchProductsByFilter(RegState.REGISTERED, innId, start, end);
    }

    @Transactional
    public Product updateProduct(Product prod) {
//        prod.getProdApplications().setApplicant(applicantDAO.findApplicant(prod.getApplicant().getApplcntId()));
//        prod.getProdApplications().setUser(userService.findUser(prod.getProdApplications().getUser().getUserId()));
        String manufName = prod.getManufName();
        if (manufName==null){
            List<ProdCompany> companyList = prod.getProdCompanies();
            if (companyList!=null){
                for(ProdCompany company:companyList){
                    if (company.getCompanyType().equals(CompanyType.FIN_PROD_MANUF)){
                        manufName = company.getCompany().getContactName();
                        prod.setManufName(manufName);
                        break;
                    }

                }
            }
        }
        return productDAO.updateProduct(prod);
    }

   public List<Product> findProductByFilter(ProductFilter filter) {
        HashMap<String, Object> params = filter.getFilters();
        return productDAO.findProductByFilter(params);
    }

    public Product findOneByName(String name){
        List<Product> found = productDAO.findByName(name);
        if (found!=null && found.size()>0)
            return  found.get(0);
        return null;
    }

    public List<Atc> findAtcsByProduct(Long id) {
        return productDAO.findAtcsByProduct(id);
    }
    
    public String setPcCodes(Long id, List<PcCode> pcCodes) {
    	Product prod = findProduct(id);
    	prod.setPcCodes(pcCodes);
        return productDAO.saveProduct(prod);
    }

    /* Eager fetches the product details
    *  @Param long id product id
    *
    * */
    @Transactional
    public Product findProduct(Long prodId) {
        Product prod = productDAO.findProductEager(prodId);
        return prod;
    }

    /**
     * varification double Inn
     * unique key in Table ProdInn = innId&prodId&companyId
     * @return
     */
    public String varificationInnProduct(List<ProdInn> prodInns){
    	String error = "";
    	if(prodInns != null && prodInns.size() > 0){
    		Set<String> keys = new HashSet<String>();
    		for(ProdInn pinn:prodInns){
    			String key = pinn.getInn() + "&&" + (pinn.getCompany() != null ? pinn.getCompany().getId() : "");
    			if(!keys.add(key)){
    				error = "doubleManuf";
    				break;
    			}
    		}
    		//error = "doubleManuf";
    	}else
    		error = "no_inns";
    	
    	return error;
    }
    
    /**
     * validate Product by click Submit
     * @param prodApplications
     * @return
     */
    public RetObject validateProduct(ProdApplications prodApplications) {
        RetObject retObject = new RetObject();
        List<String> issues = new ArrayList<String>();
        Product product = prodApplications.getProduct();
        boolean issue = false;
        try {
            Workspace workspace = workspaceDAO.findAll().get(0);
            if(workspace.getName().equals("Ethiopia")){
            	//prodApplications.getPrescreenfeeSubmittedDt()
                if (prodApplications.getPrescreenBankName().equalsIgnoreCase("") || prodApplications.getPaymentPrescreen().getPayment_payDate() == null) {
                    issues.add("no_fee");
                    issue = true;
                }
            }else {
                if (!checkFee(prodApplications)) {
                    issues.add("no_fee");
                    issue = true;
                }
            }

            if (prodApplications.getApplicant() == null) {
                issues.add("no_applicant");
                issue = true;
            }
            if (product.getInns() == null || product.getInns().size() < 1) {
                issues.add("no_inns");
                issue = true;
            }
            if (product.getShelfLife() == null || product.getShelfLife().equals("")) {
                issues.add("no_shelflife");
                issue = true;
            }
            if (product.getPosology() == null || product.getPosology().equals("")) {
                issues.add("no_posology");
                issue = true;
            }
            if (product.getIndications() == null || product.getIndications().equals("")) {
                issues.add("no_indications");
                issue = true;
            }
            if (product.getProdCompanies() == null || product.getProdCompanies().size() < 1) {
                issues.add("no_manufacturer");
                issue = true;
            }else{
                List<ProdCompany> prodCompanies = product.getProdCompanies();
                boolean finProdManuf = false;
                for(ProdCompany pc : prodCompanies){
                    if(pc.getCompanyType().equals(CompanyType.FIN_PROD_MANUF)) {
                        finProdManuf = true;
                        break;
                    }else {
                        finProdManuf = false;
                    }
                }
                if(!finProdManuf) {
                    issues.add("no_fin_prod_manuf");
                    issue = true;
                }

            }


            List<ProdAppChecklist> prodAppChkLst = prodApplicationsService.findAllProdChecklist(prodApplications.getId());
            if (prodAppChkLst != null) {
                for (ProdAppChecklist prodAppChecklist : prodAppChkLst) {
                     if (prodAppChecklist.getChecklist().isHeader()){
                        if (prodAppChecklist.getValue()==null){
                            issues.add("checklist_incomplete");
                            issue = true;
                            break;
                        }
                    }
                }
            } else {
                issues.add("checklist_incomplete");
                issue = true;
            }

            if (issue) {
                retObject.setObj(issues);
                retObject.setMsg("error");
            } else {
                retObject.setMsg("persist");
                retObject.setObj(null);
            }
            return retObject;
        } catch (Exception ex) {
            ex.printStackTrace();
            retObject.setMsg("error");
            issues.add(ex.getMessage());
            retObject.setObj(issues);
            return retObject;

        }
    }
    /**
     * Check fee
     * @param prodApplications
     * @return
     */
	public boolean checkFee(ProdApplications prodApplications) {
/*		String bankName = "";
		Date feeSubmited = null;
		if(prodApplications.getBankName() != null){
			bankName = prodApplications.getBankName();
			feeSubmited = prodApplications.getFeeSubmittedDt();
		}else{
			if(prodApplications.getPrescreenBankName() != null){
				bankName = prodApplications.getPrescreenBankName();
				feeSubmited = prodApplications.getPrescreenfeeSubmittedDt();
			}
		}
		return (bankName.length()>0) && (feeSubmited != null);*/
		return true;
	}

    public RetObject findDrugPriceByProd(Long prodID) {
        RetObject retObject;

        try {
            retObject = new RetObject("persist", pricingDAO.findByProduct_Id(prodID));
        }catch(Exception ex){
            ex.printStackTrace();
            retObject = new RetObject(ex.getMessage(), null);
        }
        return retObject;
    }

    public DrugPrice saveDrugPrice(DrugPrice selectedDrugPrice) {
         return drugPriceDAO.save(selectedDrugPrice);

    }

    public void removeDrugPricing(DrugPrice drprice){
    	drugPriceDAO.delete(drprice);
    }
    
    public Pricing savePricing(Pricing pricing) {
        return pricingDAO.save(pricing);
    }

    public List<ProdTable> findSuspendedProducts() {
        return productDAO.fetchProductsByFilter(RegState.SUSPEND, null, null, null);
    }

    public List<ProdTable> findCanceledProduct() {
        return productDAO.fetchProductsByFilter(RegState.CANCEL, null, null, null);
    }
    
    /**
     * get All Manufacturings
     * @param amendment
     * @return
     */
    public List<ProdCompany> fetchProdCompanies(Long prodId) {
		List<ProdCompany> prodCompanies = new ArrayList<ProdCompany>();
		Product prod = findProduct(prodId);
		
		if(prod != null){
			List<ProdInn> inns = prod.getInns();
			if(inns != null && inns.size() > 0){
				for(ProdInn inn:inns){
					if(inn.getCompany() != null){
						ProdCompany pcom = new ProdCompany(prod, inn.getCompany(), CompanyType.API_MANUF);
						prodCompanies.add(pcom);
					}
				}
			}
			if(prod.getProdCompanies() != null && prod.getProdCompanies().size() > 0)
				prodCompanies.addAll(prod.getProdCompanies());
		}
		return prodCompanies;
	}
    
    public ProdAppType getProdAppTypeByProduct(Long prod_id){
    	ProdAppType type = null;
    	Product prod = productDAO.findProduct(prod_id);
    	if(prod != null){
    		List<ProdApplications> listProdApp = prod.getProdApplicationses();
    		if(listProdApp != null && listProdApp.size() > 0){
    			for(int i = 0 ; i < listProdApp.size(); i++){
					ProdApplications prodApp = listProdApp.get(i);
					if(prodApp != null && prodApp.getRegState() != null 
							&& prodApp.getRegState().equals(RegState.REGISTERED)){
						type = prodApp.getProdAppType();
						break;					
					}
    			}
    		}
    	}
    	return type;
    }

	public DosUomDAO getDosUomDAO() {
		return dosUomDAO;
	}

	public void setDosUomDAO(DosUomDAO dosUomDAO) {
		this.dosUomDAO = dosUomDAO;
	}
    /**
     * @param aplicant_id - Applicant id
     * @param category - HUMAN|VETERINARY|UNKNOWN
     * @return all REGISTERED product this applicant which have select Product Category
     */
	public List<Product> findProductByTypeApplicant(Long aplicant_id, ProdCategory category,String year){
		List<Long> apcntId = new ArrayList<Long>();
		apcntId.add(aplicant_id);
		List<Product> products =  productDAO.findRegProductByApplicants(apcntId, year);
		List<Product> productsByType = new ArrayList<Product>();
		if(products!=null && products.size()>0){
			for(Product prod:products){
				ProdCategory prodCategory = prod.getProdCategory();
				if(prodCategory!=null){
					if(prodCategory.equals(category)){
						productsByType.add(prod);
					}
				}
			}
		}
		return productsByType;
	}
	
	 /**
     * @param aplicant_id - Applicant id
     * @param category - HUMAN|VETERINARY|UNKNOWN
     * @return List<ProdTable> all REGISTERED product this applicant which have select Product Category
     */
	public List<ProdTable> findProdTableByApplicants(Long aplicant_id, ProdCategory category, String year){
		return productDAO.findProdTableByApplicants(aplicant_id, category, year);
	}
	 /**
     * @param aplicant_id - Applicant id
     * @param category - HUMAN|VETERINARY|UNKNOWN
     * @return size list of all REGISTERED product this applicant which have select Product Category
     */
	public int findSizeProductByTypeApplicant(Long aplicant_id, ProdCategory category,String year){
		 List<Product> listProduct = findProductByTypeApplicant(aplicant_id, category, year);
		 if(listProduct!=null && listProduct.size()>0)
			 return listProduct.size();
		 else
			 return 0;
	}
	
}
