package org.msh.pharmadex.mbean.amendment;

import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This class serves Application related amendment - new documents etc
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdNOT_APPLICABLEMBean implements IAmendment {
	
	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	private ProductService productService;
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	private Amendment amendment;

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmendmentById(id);
		}else{
			amendment = new Amendment();
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = amd;
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
		return "";
	}

	@Override
	public String submit() {
		return save("");	
	}
	
	@Override
	public String reject() {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().rejectAmendment(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmendment(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}
	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		Amendment amd = getAmendmentService().saveAndFlush(getAmendment());
		if(amd != null){
			setAmendment(amd);
			return "";
		}else{
			return "global_fail";
		}
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
