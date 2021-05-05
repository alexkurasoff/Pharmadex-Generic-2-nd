package org.msh.pharmadex.mbean.amendment;

import java.util.List;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.DosageForm;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.domain.amendment.AmdPRODUCT_DOSFORM;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.mbean.product.DosFormItem;
import org.msh.pharmadex.service.DosageFormService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;
import org.msh.pharmadex.util.JsfUtils;
import org.primefaces.event.SelectEvent;

/**
 * This class serves 
 * Change in the dosage form including, change from an immediate release product to modified-release 
 * dosage form or vice versa and change from liquid to powder for reconstitution, or vice versa
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdPRODUCT_DOSFORMMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;

	@ManagedProperty(value="#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty(value="#{productService}")
	private ProductService productService;
	
	@ManagedProperty(value = "#{dosageFormService}")
	private DosageFormService dosageFormService;
	
	private AmdPRODUCT_DOSFORM amendment = null;
	
	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	private DosFormItem mainIt;
	private DosFormItem subIt;
	
	@Override
	public Amendment findAmendment(Long id) {
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdPRODUCT_DOSFORM(id);
		}else{
			amendment = new AmdPRODUCT_DOSFORM();
		}
		return amendment;
	}
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdPRODUCT_DOSFORM)amd;
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
		DosageForm df = findDosageFormByItem();
		if(df != null){
			if(getProduct().getDosForm() != null)
				if(df.getUid().intValue() == getProduct().getDosForm().getUid().intValue())
					return "noamdchanges";
			amendment.setDosageForm(df);
		}else
			return "selectdosform";
		return "";
	}

	@Override
	public String save(String step) {
		AmdPRODUCT_DOSFORM amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdPRODUCT_DOSFORM(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdPRODUCT_DOSFORM(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
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
	public String getOldDosFormName(){
		if (getAmendment().getState() == AmdState.APPROVED){
			if(amendment.getDosageForm() != null)
				return amendment.getDosageForm().getDosForm();
		}else{
			if(getProduct() != null && getProduct().getDosForm() != null)
				return getProduct().getDosForm().getDosForm();
		}
		return "";
	}
	
	/**
	 * Get new dosage form name
	 * @return
	 */
	public String getNewDosFormName(){
		if (getAmendment().getState() == AmdState.APPROVED){
			if(amendment.getProduct().getDosForm() != null)
				return amendment.getProduct().getDosForm().getDosForm();
		}else{
			if(amendment.getDosageForm() != null)
				return amendment.getDosageForm().getDosForm();
		}
		return "";
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
	
	public List<DosFormItem> completeDosforms(String query) {
        return JsfUtils.completeSuggestions(query, dosageFormService.findMainDosForms());
    }
	
	public List<DosFormItem> completeDosformsSub(String query) {
        return JsfUtils.completeSuggestions(query, dosageFormService.findSubDosFormByMain(mainIt));
    }
	
	public void onItemSelectMain(SelectEvent event) {
		if(event.getObject() instanceof DosFormItem){
			mainIt = (DosFormItem) event.getObject();
			subIt = null;
			
			List<DosFormItem> list = dosageFormService.findSubDosFormByMain(mainIt);
			if(list != null){
				if(list.size() == 1)
					subIt = list.get(0);
				if(list.size() > 0 && !list.get(0).getName().trim().isEmpty())
					subIt = list.get(0);
			}
		}	
	}
	
	public void onItemSelect(SelectEvent event) {
		if(event.getObject() instanceof DosFormItem){
			subIt = (DosFormItem) event.getObject();
		}
	}
	
	public boolean hideSubList(){
		List<DosFormItem> list = dosageFormService.findSubDosFormByMain(mainIt);
		if(list != null && list.size() > 0)
			return true;
		
		return false;
	}
	
	public DosageForm findDosageFormByItem(){
		DosageForm df = dosageFormService.findDosageFormByItem(mainIt, subIt);
		return df;
	}
	
	public DosFormItem getMainIt() {
		return mainIt;
	}
	public void setMainIt(DosFormItem mainIt) {
		this.mainIt = mainIt;
	}
	public DosFormItem getSubIt() {
		return subIt;
	}
	public void setSubIt(DosFormItem subIt) {
		this.subIt = subIt;
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
	public DosageFormService getDosageFormService() {
		return dosageFormService;
	}
	public void setDosageFormService(DosageFormService dosageFormService) {
		this.dosageFormService = dosageFormService;
	}
	
}
