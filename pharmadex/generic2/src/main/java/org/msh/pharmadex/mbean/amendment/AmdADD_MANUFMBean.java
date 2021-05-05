package org.msh.pharmadex.mbean.amendment;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdCompany;
import org.msh.pharmadex.domain.amendment.AmdADD_MANUF;
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
public class AmdADD_MANUFMBean implements IAmendment {
	
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
		
	private AmdADD_MANUF amendment = null;
	
	private List<ProdCompany> oldManufs = null;
	private List<ProdCompany> newManufs = null;
	private ProdCompany newItemManuf = new ProdCompany();

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdADD_MANUF(id);
			newManufs = amendment.getProdCompany();
		}else{
			amendment = new AmdADD_MANUF();
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdADD_MANUF) amd;
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
			oldManufs = getAmendmentService().fetchProdCompanies(amendment);
		}
		return res;
	}
	
	@Override
	public String validateDetail() {
		if(!isChanged())
			return "noamdchanges";
		return "";
	}

	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		amendment.setProdCompany(newManufs);
		
		AmdADD_MANUF amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdADD_MANUF(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdADD_MANUF(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	public List<ProdCompany> getOldManufs() {
		if(oldManufs != null && oldManufs.size() > 0)
			return oldManufs;
		
		oldManufs = getAmendmentService().fetchProdCompanies(amendment);
		return oldManufs;
	}

	public void setOldManufs(List<ProdCompany> olds) {
		this.oldManufs = olds;
	}

	public String addProdCompany() {
		try {
		    List<ProdCompany> list = new ArrayList<ProdCompany>();
		    if(oldManufs != null && oldManufs.size() > 0)
		    	list.addAll(oldManufs);
		    if(newManufs != null && newManufs.size() > 0)
		    	list.addAll(newManufs);
		    List<ProdCompany> prodCompanies = companyService.addCompanyInAmendment(list, getCompanyMBean().getSelectedCompany(), getCompanyMBean().getCompanyTypes());
            if(prodCompanies != null && prodCompanies.size() > 0){
            	if(newManufs == null)
    		    	newManufs = new ArrayList<ProdCompany>();
            	
            	newManufs.addAll(prodCompanies);
            }else
            	informUser("doubleManuf", FacesMessage.SEVERITY_ERROR);
            
            getCompanyMBean().setSelectedCompany(null);
            getCompanyMBean().setCompanyTypes(null);
		} catch (Exception e) {
			 e.printStackTrace();
			 context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), e.getMessage()));
		}
		return null;
	}

	public void removeProdCompany(ProdCompany pr) {
		if(newManufs != null && newManufs.size() > 0 && pr != null){
			newManufs.remove(pr);
		}
	}
	
	public boolean isChanged(){
		boolean ch = false;
		if(amendment != null && getNewManufs() != null){
			if(getNewManufs().size() > 0)
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

	/**
	 * Inform user
	 * @param key message key
	 * @param severity
	 */
	private void informUser(String key, Severity severity) {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
		context.addMessage(null, new FacesMessage(severity, bundle.getString(key), ""));
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:growl");
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:messages");
	}
	
	public List<ProdCompany> getNewManufs() {
		return newManufs;
	}

	public void setNewManufs(List<ProdCompany> news) {
		this.newManufs = news;
	}
	
	public ProdCompany getNewItemManuf() {
		return newItemManuf;
	}

	public void setNewItemManuf(ProdCompany newInn) {
		this.newItemManuf = newInn;
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
