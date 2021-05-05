package org.msh.pharmadex.mbean.amendment;

import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.amendment.AmdPRODUCT_NAME;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This class serves Product Name amendment type
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdPRODUCT_NAMEMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;

	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty(value="#{productService}")
	private ProductService productService;
	
	private AmdPRODUCT_NAME amendment = null;
	
	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdPRODUCT_NAME(id);
		}else{
			amendment = new AmdPRODUCT_NAME();
		}
		return amendment;
	}
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdPRODUCT_NAME)amd;
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
		if(getAmendment().getProduct().getProdName().equals(getAmendment().getPropName()))
			return "noamdchanges";
		getAmendment().setPropName(getAmendment().getPropName().toUpperCase());
		return "";
	}

	@Override
	public String save(String step) {
		AmdPRODUCT_NAME amd = getAmendmentService().saveAndFlush(amendment);
		if(amd != null){
			setAmendment(amd);
			return "";
		}else{
			return "global_fail";
		}
	}

	@Override
	public String submit() {
		String err = save(AmdreghomeMBean.STEP_SUMMARY);
		return err;
	}

	@Override
	public String reject() {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().rejectAmdPRODUCT_NAME(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdPRODUCT_NAME(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}
	
	public Product getProduct(){
		if(getAmendment() != null)
			return getAmendment().getProduct();
		return null;
	}
	
	public String getProductName(){
		if(amendment.getPropName() != null && amendment.getPropName().length() > 0){
			return amendment.getPropName();
		}else{
			amendment.setPropName(getProduct().getProdName());
		}
		return amendment.getPropName();
	}
	
	public void setProductName(String name){
		amendment.setPropName(name);
	}
	
	public String getOldProdName(){
		String prodName = "";
		if(AmdState.APPROVED.equals(getAmendment().getState())){
			prodName = getProductName();
		}else{
			prodName = getProduct().getProdName();
		}
		return prodName;
	}

	public String getNewProdName(){
		String prodName = "";
		if(AmdState.APPROVED.equals(getAmendment().getState())){
			prodName = getProduct().getProdName();
		}else{
			prodName = getProductName();
		}
		return prodName;
	}
	
	public boolean isProductNameChanged(){
		boolean ch = false;
		if(getAmendment().getProduct() != null)
			ch = !getAmendment().getProduct().getProdName().equals(getAmendment().getPropName());
		return ch;
	}

	public boolean hasEffectiveDate(){
		if(getAmendment() != null && getAmendment().getEffectiveDate() != null)
			return true;
		return false;
	}

	public boolean hasApprDate(){
		if(getAmendment() != null && getAmendment().getApprDate() != null)
			return true;
		return false;
	}
	
	public UserSession getUserSession() {
		return userSession;
	}
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
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
}
