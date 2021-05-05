package org.msh.pharmadex.mbean.amendment;

import java.util.List;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.AdminRoute;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.amendment.AmdPRODUCT_ADMINROUTE;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.mbean.product.DosFormItem;
import org.msh.pharmadex.service.AdminRouteService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;
import org.msh.pharmadex.util.JsfUtils;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class serves 
 * Change in the route of administration
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdPRODUCT_ADMINROUTEMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;

	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty(value="#{productService}")
	private ProductService productService;
	
	@ManagedProperty(value="#{adminRouteService}")
	private AdminRouteService adminRouteService;

	
	private AmdPRODUCT_ADMINROUTE amendment = null;
	
	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdPRODUCT_ADMINROUTE(id);
		}else{
			amendment = new AmdPRODUCT_ADMINROUTE();
		}
		//if(amendment.getAdminRoute() == null)
		//	amendment.setAdminRoute(new AdminRoute());
		return amendment;
	}
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdPRODUCT_ADMINROUTE)amd;
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
		if(getAdminRoute() != null && getProduct() != null && getProduct().getAdminRoute() != null){
			if(getAdminRoute().getId() == getProduct().getAdminRoute().getId())
				return "noamdchanges";
		}
		return "";
	}

	@Override
	public String save(String step) {
		AmdPRODUCT_ADMINROUTE amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdPRODUCT_ADMINROUTE(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdPRODUCT_ADMINROUTE(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
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
	
	/**
	 * Get old dosage form name
	 * @return
	 */
	public String getOldAdminRouteName(){
		if (getAmendment().getState() == AmdState.APPROVED){
			if(amendment.getAdminRoute() != null)
				return amendment.getAdminRoute().getName();
		}else{
			if(getProduct() != null && getProduct().getDosForm() != null)
				if(getProduct().getAdminRoute() != null)
					return getProduct().getAdminRoute().getName();
		}
		return "";
	}
	
	/**
	 * Get new dosage form name
	 * @return
	 */
	public String getNewAdminRouteName(){
		if (getAmendment().getState() == AmdState.APPROVED){
			if(amendment.getProduct().getDosForm() != null)
				return amendment.getProduct().getAdminRoute().getName();
		}else{
			if(getAdminRoute() != null)
				return getAdminRoute().getName();
		}
		return "";
	}
	
	public AdminRoute getAdminRoute(){
		return amendment.getAdminRoute();
	}
	
	public void setAdminRoute(AdminRoute adminRoute){
		amendment.setAdminRoute(adminRoute);
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

	public List<AdminRoute> completeAdminRoute(String query) {
        return JsfUtils.completeSuggestions(query, getAdminRouteService().getAdminRoutes());
    }
	
	public void onItemSelect(SelectEvent event) {
		/*if(event.getObject() instanceof AdminRoute){
			mainIt = (DosFormItem) event.getObject();
			subIt = null;
			
			List<DosFormItem> list = dosageFormService.findSubDosFormByMain(mainIt);
			if(list != null){
				if(list.size() == 1)
					subIt = list.get(0);
				if(list.size() > 0 && !list.get(0).getName().trim().isEmpty())
					subIt = list.get(0);
			}
		}	*/
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
