package org.msh.pharmadex.mbean.amendment;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.Company;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.amendment.AmdEDIT_API;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.mbean.product.CompanyMBean;
import org.msh.pharmadex.service.CompanyService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This class serves Application related amendment - new description and physical appearance
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdEDIT_APIMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
	
	@ManagedProperty(value="#{companyMBean}")
	CompanyMBean companyMBean;
	
	@ManagedProperty(value = "#{companyService}")
	private CompanyService companyService;
		
	private AmdEDIT_API amendment = null;
	
	private List<ProdInn> prodInns = new ArrayList<ProdInn>();

	private ProdInn selectedValue = null;
	private ProdInn editedValue = null;
	private ProdInn newValue = null;
	private ProdInn value = null;
	
	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdEDIT_API(id);
			if(amendment.getProdInn() != null && amendment.getProdInn().size() == 2){
				if(!amendment.getState().equals(AmdState.APPROVED)){
					editedValue = getAmendmentService().findProdInnEdit(amendment, true);
					newValue = getAmendmentService().findProdInnEdit(amendment, false);
				}else{
					// old-product is null
					editedValue = getAmendmentService().findProdInnEdit(amendment, false);
					newValue = getAmendmentService().findProdInnEdit(amendment, true);
				}
				if(newValue != null)
					getCompanyMBean().setSelectedCompany(newValue.getCompany());
			}
		}else{
			amendment = new AmdEDIT_API();
		}
		prodInns = new ArrayList<ProdInn>();
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdEDIT_API) amd;
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
		String res = getAmendmentService().validateSelectProduct(getAmendment());
		return res;
	}
	
	@Override
	public String validateDetail() {
		if(isProdInnChanged())
			return "";
		
		return "noamdchanges";
	}

	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		AmdEDIT_API amd =  getAmendmentService().saveAndFlush(amendment);		
		if(amd != null){
			findAmendment(amd.getId());
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
		String error = getAmendmentService().rejectAmdEDIT_API(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdEDIT_API(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}
	
	public ProdInn getValue() {
		return value;
	}

	public List<ProdInn> getProdInns() {
		if(!(prodInns != null && prodInns.size() > 0))
			prodInns = getAmendmentService().fetchProdInns(amendment);
		return prodInns;
	}

	public void setProdInns(List<ProdInn> prods) {
		this.prodInns = prods;
	}
	
	public boolean isProdInnChanged(){
		boolean ch = false;
		if(amendment != null && editedValue != null && newValue != null){
			if(!editedValue.getDosStrength().equals(newValue.getDosStrength()))
				return true;
			if(editedValue.getDosUnit().getId() != newValue.getDosUnit().getId())
				return true;
			if(editedValue.getCompany().getId() != newValue.getCompany().getId())
				return true;
		}
		return ch;
	}
	
	public void updateProdInn(){
		editedValue = getSelectedValue();
		if(newValue != null){
			newValue.setInn(value.getInn());
			newValue.setDosStrength(value.getDosStrength());
			newValue.setDosUnit(getProductService().getDosUomDAO().findOne(value.getDosUnit().getId()));
			newValue.setRefStd(value.getRefStd());
		}else
			newValue = value;
		
		if(getCompanyMBean().getSelectedCompany() != null){
			Company compInn = getCompanyService().saveCompany(getCompanyMBean().getSelectedCompany());
			newValue.setCompany(compInn);
		}
		
		if(editedValue != null && selectedValue != null &&
				editedValue.getId() != selectedValue.getId())
			editedValue = selectedValue;
		
		List<ProdInn> list = new ArrayList<ProdInn>();
		list.add(editedValue);
		list.add(newValue);
		amendment.setProdInn(list);
	}
	
	public void cancelProdInn(){
	}
	
	public void setSelectedRowItem(ProdInn exc){
		if(exc != null){
			setSelectedValue(getAmendmentService().getProdInnDAO().findOne(exc.getId()));
			if(editedValue != null){
				if(exc.getId() == editedValue.getId()){
					value = newValue;
				}else
					value = getAmendmentService().copyProdInn(getSelectedValue());
			}else
				value = getAmendmentService().copyProdInn(getSelectedValue());
			
			getCompanyMBean().setSelectedCompany(value.getCompany());
		}
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
	
	public String getStyleRow(ProdInn item){
		if(editedValue != null && item.getId() == editedValue.getId())
			return "rowSelect";
		return null;
	}
	
	public ProdInn getNewValue(){
		return newValue;
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

	public ProdInn getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(ProdInn selectProd) {
		this.selectedValue = selectProd;
	}

	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}

	public CompanyMBean getCompanyMBean() {
		return companyMBean;
	}

	public void setCompanyMBean(CompanyMBean companyMBean) {
		this.companyMBean = companyMBean;
	}

	public CompanyService getCompanyService() {
		return companyService;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}
	
	
}
