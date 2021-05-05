package org.msh.pharmadex.mbean.amendment;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.Company;
import org.msh.pharmadex.domain.DosUom;
import org.msh.pharmadex.domain.Inn;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.amendment.AmdADD_API;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.mbean.product.CompanyMBean;
import org.msh.pharmadex.service.CompanyService;
import org.msh.pharmadex.service.InnService;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This class serves Application related amendment - new description and physical appearance
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdADD_APIMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
	
	@ManagedProperty(value = "#{innService}")
	private InnService innService;
	
	@ManagedProperty(value="#{companyMBean}")
	CompanyMBean companyMBean;
	
	@ManagedProperty(value = "#{companyService}")
	private CompanyService companyService;
		
	private AmdADD_API amendment = null;
	
	private List<ProdInn> oldInns = null;
	private List<ProdInn> newInns = null;
	private ProdInn newItemInn = new ProdInn();

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdADD_API(id);
			newInns = amendment.getProdInn();
		}else{
			amendment = new AmdADD_API();
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdADD_API) amd;
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
		if(res.length() == 0){
			oldInns = getAmendmentService().fetchProdInns(amendment);
		}
		return res;
	}
	
	@Override
	public String validateDetail() {
		if(!isChanged())
			return "noamdchanges";
		String err = getAmendmentService().validateListProdInn(newInns, getAmendment());
		return err;
	}

	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		amendment.setProdInn(newInns);
		
		AmdADD_API amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdADD_API(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdADD_API(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	public List<ProdInn> getOldInns() {
		if(oldInns != null && oldInns.size() > 0)
			return oldInns;
		
		oldInns = getAmendmentService().fetchProdInns(amendment);
		return oldInns;
	}

	public void setOldInns(List<ProdInn> olds) {
		this.oldInns = olds;
	}

	public String addProdInn() {
		context = FacesContext.getCurrentInstance();
		bundle = context.getApplication().getResourceBundle(context, "msgs");

		try {
			context = FacesContext.getCurrentInstance();
		    if (newItemInn.getInn().getId() == null)
		    	newItemInn.setInn(innService.addInn(newItemInn.getInn()));
		    else
		    	newItemInn.setDosUnit(getProductService().getDosUomDAO().findOne(newItemInn.getDosUnit().getId()));

		    newItemInn.setDosUnit(getProductService().getDosUomDAO().findOne(newItemInn.getDosUnit().getId()));
		    
		    if(getCompanyMBean().getSelectedCompany() != null){
				Company compInn = getCompanyService().saveCompany(getCompanyMBean().getSelectedCompany());
				newItemInn.setCompany(compInn);
			}
		    
		    if(newInns == null)
		    	newInns = new ArrayList<ProdInn>();
		    newInns.add(newItemInn);
		    newItemInn = new ProdInn();
		    newItemInn.setInn(new Inn());
		    newItemInn.setDosUnit(new DosUom());
		    
		    getCompanyMBean().setSelectedCompany(null);
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(bundle.getString("product_innname_valid")));
		}
		return null;
	}

	public void removeProdInn(ProdInn pr) {
		if(newInns != null && newInns.size() > 0 && pr != null){
			newInns.remove(pr);
		}
	}
	
	public boolean isChanged(){
		boolean ch = false;
		if(amendment != null && getNewInns() != null){
			if(getNewInns().size() > 0)
				return true;
		}
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
	
	public List<ProdInn> getNewInns() {
		return newInns;
	}

	public void setNewInns(List<ProdInn> news) {
		this.newInns = news;
	}
	
	public ProdInn getNewItemInn() {
		return newItemInn;
	}

	public void setNewItemInn(ProdInn newInn) {
		this.newItemInn = newInn;
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

	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}

	public InnService getInnService() {
		return innService;
	}

	public void setInnService(InnService innService) {
		this.innService = innService;
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
