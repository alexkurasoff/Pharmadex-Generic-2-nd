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
import org.msh.pharmadex.domain.DosUom;
import org.msh.pharmadex.domain.Excipient;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdExcipient;
import org.msh.pharmadex.domain.amendment.AmdADD_IPI;
import org.msh.pharmadex.domain.amendment.Amendment;
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
public class AmdADD_IPIMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
	
	@ManagedProperty(value = "#{innService}")
	private InnService innService;
		
	private AmdADD_IPI amendment = null;
	
	private List<ProdExcipient> oldExcipients = null;
	private List<ProdExcipient> newExcipients = null;
	private ProdExcipient newProdExc = new ProdExcipient();

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdADD_IPI(id);
			newExcipients = amendment.getProdExcipient();
		}else{
			amendment = new AmdADD_IPI();
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdADD_IPI) amd;
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
			oldExcipients = getAmendmentService().fetchProdExcipients(amendment);
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
		amendment.setProdExcipient(newExcipients);
		
		AmdADD_IPI amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdADD_IPI(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdADD_IPI(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	public List<ProdExcipient> getOldExcipients() {
		if(oldExcipients != null && oldExcipients.size() > 0)
			return oldExcipients;
		
		oldExcipients = getAmendmentService().fetchProdExcipients(amendment);
		return oldExcipients;
	}

	public void setOldExcipients(List<ProdExcipient> oldExcipients) {
		this.oldExcipients = oldExcipients;
	}

	public String addProdExcipient() {
		context = FacesContext.getCurrentInstance();
		bundle = context.getApplication().getResourceBundle(context, "msgs");

		try {
			context = FacesContext.getCurrentInstance();
		    if (newProdExc.getExcipient().getId() == null)
		    	newProdExc.setExcipient(innService.addExcipient(newProdExc.getExcipient()));
		    else
		    	newProdExc.setDosUnit(getProductService().getDosUomDAO().findOne(newProdExc.getDosUnit().getId()));

		    newProdExc.setDosUnit(getProductService().getDosUomDAO().findOne(newProdExc.getDosUnit().getId()));
		    
		    if(newExcipients == null)
		    	newExcipients = new ArrayList<ProdExcipient>();
		    newExcipients.add(newProdExc);
		    newProdExc = new ProdExcipient();
		    newProdExc.setExcipient(new Excipient());
		    newProdExc.setDosUnit(new DosUom());
		} catch (Exception e) {
			context.addMessage(null, new FacesMessage(bundle.getString("product_innname_valid")));
		}
		return null;
	}

	public boolean isChanged(){
		boolean ch = false;
		if(amendment != null && getNewExcipients() != null){
			if(getNewExcipients().size() > 0)
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
	
	public void removeProdExc(ProdExcipient pr) {
		if(newExcipients != null && newExcipients.size() > 0 && pr != null){
			newExcipients.remove(pr);
		}
	}
	
	public List<ProdExcipient> getNewExcipients() {
		return newExcipients;
	}

	public void setNewExcipients(List<ProdExcipient> newExcipients) {
		this.newExcipients = newExcipients;
	}
	
	public ProdExcipient getNewProdExc() {
		return newProdExc;
	}

	public void setNewProdExc(ProdExcipient newProdExc) {
		this.newProdExc = newProdExc;
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
	
}
