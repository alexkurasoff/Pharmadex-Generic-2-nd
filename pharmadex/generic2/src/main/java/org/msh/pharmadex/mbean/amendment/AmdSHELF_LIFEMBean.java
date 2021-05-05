package org.msh.pharmadex.mbean.amendment;

import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.amendment.AmdSHELF_LIFE;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.service.AdminRouteService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This class serves 
 * Change in the route of administration
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdSHELF_LIFEMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;

	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty(value="#{productService}")
	private ProductService productService;
	
	@ManagedProperty(value="#{adminRouteService}")
	private AdminRouteService adminRouteService;

	
	private AmdSHELF_LIFE amendment = null;
	
	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdSHELF_LIFE(id);
		}else{
			amendment = new AmdSHELF_LIFE();
		}
		return amendment;
	}
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdSHELF_LIFE)amd;
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
		if(!isShelfLifeChanged())
			return "noamdchanges";
		return "";
	}

	@Override
	public String save(String step) {
		AmdSHELF_LIFE amd = getAmendmentService().saveAndFlush(amendment);
		if(amd != null){
			findAmendment(amd.getId());
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
		String error = getAmendmentService().rejectAmdSHELF_LIFE(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdSHELF_LIFE(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		if(error != null && error.length() > 0)
			return error;
		// update amendment values
		findAmendment(amendment.getId());
		return "";
	}
	
	public Product getProduct(){
		if(getAmendment() != null)
			return getAmendment().getProduct();
		return null;
	}
	
	public String getShelfLife(){
		if(amendment.getShelfLife() != null && amendment.getShelfLife().length() > 0){
			return amendment.getShelfLife();
		}else{
			amendment.setShelfLife(getProduct().getShelfLife());
		}
		return amendment.getShelfLife();
	}
	
	public void setShelfLife(String val){
		amendment.setShelfLife(val);
	}
	
	public String getOldShelfLife(){
		String value = "";
		if(AmdState.APPROVED.equals(getAmendment().getState())){
			value = getShelfLife();
		}else{
			value = getProduct().getShelfLife();
		}
		return value;
	}

	public String getNewShelfLife(){
		String value = "";
		if(AmdState.APPROVED.equals(getAmendment().getState())){
			value = getProduct().getShelfLife();
		}else{
			value = getShelfLife();
		}
		return value;
	}
	
	public boolean isShelfLifeChanged(){
		boolean ch = false;
		if(getAmendment().getProduct() != null && getAmendment().getProduct().getShelfLife() != null)
			ch = !getAmendment().getProduct().getShelfLife().equals(amendment.getShelfLife());
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
	public AdminRouteService getAdminRouteService() {
		return adminRouteService;
	}
	public void setAdminRouteService(AdminRouteService adminRouteService) {
		this.adminRouteService = adminRouteService;
	}
	
}
