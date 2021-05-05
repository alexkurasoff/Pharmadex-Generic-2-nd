package org.msh.pharmadex.mbean.amendment;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.FeePayment;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.amendment.AmdSHAPE;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This class serves Application related amendment - new description and physical appearance
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdSHAPEMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
		
	private AmdSHAPE amendment = null;

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {	
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdSHAPE(id);
		}else{
			amendment = new AmdSHAPE();
			amendment.setPayment(new FeePayment());
			//add created date and creator for any
			amendment.setCreatedDate(new Date());
			amendment.setCreatedBy(getUserSession().getLoggedInUserObj());
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {		
		return amendment;
	}
	
	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdSHAPE) amendment;	
	}
	
	@Override
	public void cleanUp() {
		setAmendment(null);		
	}

	@Override
	public String validateApplicant() {
		return getAmendmentService().validateSelectApplicant(getAmendment());
	}

	@Override
	public String validateProduct() {
		return getAmendmentService().validateSelectProduct(getAmendment());
	}
	
	@Override
	public String validateDetail() {		
		if(getAmendment().getProduct().getProdDesc().equals(getAmendment().getProdDesc()))
			return "noamdchanges";
		return "";
	}
	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		if(AmdState.SAVED.equals(getAmendment().getState()) || 
				AmdState.SUBMITTED.equals(getAmendment().getState())){		
			getAmendment().setApplicant(getApplicant());
		}
		
		AmdSHAPE amd = getAmendmentService().saveAndFlush(amendment);
		if(amd != null){
		   setAmendment(amd);
		   return "";
		}else{
		   return "global_fail";
		 }
	}
	
	@Override
	public String submit() {
		return save("");			
	}

	@Override
	public String reject() {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().rejectAmdSHAPE(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;			
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdSHAPE(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;		
	}	
			
	/**
	 * Get product from amendment
	 * @return
	 */
	public Product getProduct(){
		return getAmendment().getProduct();
	}
	/**
	 * Set product to amendment
	 * @param applicant
	 */
	public void setProduct(Product product){		
		getAmendmentService().storeProductToAmd(getAmendment(), product);
	}
	
	public ProdApplications getProdApplicationses(){
		if(getProduct()!=null){
			setProduct(getProductService().findProduct(getProduct().getId()));
						
			List<ProdApplications> prodApplications =  getProduct().getProdApplicationses();
			if(prodApplications!=null && prodApplications.size()>0){
				if(prodApplications.get(0)!=null){
					return prodApplications.get(0);					
				}				
			}
		}
		return null;
	}
	
	public Applicant getApplicant(){		
		if(getProdApplicationses()!=null){
			if(getProdApplicationses().getApplicant()!=null){
				Applicant applicant = getAmendmentService().findApplicant(getProdApplicationses().getApplicant().getApplcntId());
				if(applicant!=null){
					return applicant;
				}				
			}
		}			
		return null;
	}

	public String getProductDesc() {		
		if(getAmendment() != null && getAmendment().getProduct() != null){
			if(getAmendment().getProduct().getProdDesc()!=null){
			  return getAmendment().getProduct().getProdDesc();
			}
		}
		return "";
	}

	public String getProdDesc() {
		Amendment amd = getAmendment();
		if(amd!=null){
			if(amd.getProdDesc()!=null && amd.getProdDesc().length()>0){
				
				return amd.getProdDesc();
			}else{
				amd.setProdDesc(getProduct().getProdDesc());
			}
			return amd.getProdDesc();
		}else{
			return null;
		}
	}

	public void setProdDesc(String prodDesc) {
		Amendment amd = getAmendment();
		if(amd!=null){
			amd.setProdDesc(prodDesc);
		}		
	}
	
	/**
	 * Get product data amendment if exits one
	 * @return
	 */
	public AmdSHAPE getAmdSHAPE(){
		if(getAmendment() instanceof AmdSHAPE){
			return (AmdSHAPE) getAmendment();
		}else{
			return null;
		}
	}
	
	public boolean isProdDescChanged(){
		boolean ch = false;
		if(getProduct() != null && getAmdSHAPE() != null)
			ch = !getProduct().getProdDesc().equals(getAmdSHAPE().getProdDesc());
		return ch;
	}
	
	public String getOldProdDesc(){
		String prodDesc = "";
		if(AmdState.APPROVED.equals(getAmendment().getState())){
			prodDesc = getProdDesc();
		}else{
			prodDesc = getProductDesc();
		}
		return prodDesc;
	}

	public String getNewProdDesc(){
		String prodDesc = "";
		if(AmdState.APPROVED.equals(getAmendment().getState())){
			prodDesc = getProductDesc();
		}else{
			prodDesc = getProdDesc();
		}
		return prodDesc;
	}

	public boolean hasEffectiveDate(){
		if(getAmendment() != null && getAmendment().getEffectiveDate() != null)
			return true;
		return false;
	}

	public boolean  hasApprDate(){
		if(getAmendment() != null && getAmendment().getApprDate() != null)
			return true;
		return false;
	}
	
	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}

	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}	
	
}
