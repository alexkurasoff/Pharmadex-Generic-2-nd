package org.msh.pharmadex.mbean.amendment;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ProdExcipient;
import org.msh.pharmadex.domain.amendment.AmdREMOVE_IPI;
import org.msh.pharmadex.domain.amendment.AmdState;
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
public class AmdREMOVE_IPIMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
	
	@ManagedProperty(value = "#{innService}")
	private InnService innService;
		
	private AmdREMOVE_IPI amendment = null;
	
	private List<ProdExcipientDTO> allExcipients = null;
	private List<ProdExcipient> delExcipients = null;

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdREMOVE_IPI(id);
			if(amendment.getState() != null && !amendment.getState().equals(AmdState.SAVED))
				delExcipients = amendment.getProdExcipient();
		}else{
			amendment = new AmdREMOVE_IPI();
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdREMOVE_IPI) amd;
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
		if(!isChanged())
			return "noamdchanges";
		
		return "";
	}

	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		amendment.setProdExcipient(delExcipients);
		
		AmdREMOVE_IPI amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdREMOVE_IPI(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdREMOVE_IPI(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	public List<ProdExcipientDTO> getAllExcipients() {
		if(allExcipients != null && allExcipients.size() > 0)
			return allExcipients;
		
		allExcipients = getAmendmentService().fetchProdExcipientDTO(amendment);
		return allExcipients;
	}

	public void setAllExcipients(List<ProdExcipientDTO> list) {
		this.allExcipients = list;
	}

	public void setDeleteProdExcipientDTO(ProdExcipientDTO select) {
		if(allExcipients != null && select != null){
			for(ProdExcipientDTO dto:allExcipients){
				if(dto.getProdExc().getId() == select.getProdExc().getId()){
					dto.setDelete(!dto.isDelete());
					break;
				}
			}
		}
	}
	
	public boolean isChanged(){
		boolean ch = false;
		if(amendment != null && getAllExcipients() != null){
			delExcipients = new ArrayList<ProdExcipient>();
			for(ProdExcipientDTO dto:getAllExcipients()){
				if(dto.isDelete())
					delExcipients.add(dto.getProdExc());
			}
			if(delExcipients.size() > 0)
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
	
	public List<ProdExcipient> getDelExcipients() {
		return delExcipients;
	}

	public void setDelExcipients(List<ProdExcipient> list) {
		this.delExcipients = list;
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
