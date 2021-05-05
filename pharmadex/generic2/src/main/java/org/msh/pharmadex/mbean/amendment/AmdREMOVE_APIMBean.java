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
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.domain.amendment.AmdREMOVE_API;
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
public class AmdREMOVE_APIMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
	
	@ManagedProperty(value = "#{innService}")
	private InnService innService;
		
	private AmdREMOVE_API amendment = null;
	
	private List<ProdInnDTO> allInns = null;
	private List<ProdInn> delInns = null;

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdREMOVE_API(id);
			if(amendment.getState() != null && !amendment.getState().equals(AmdState.SAVED))
				delInns = amendment.getProdInn();
		}else{
			amendment = new AmdREMOVE_API();
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdREMOVE_API) amd;
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
		
		if(delInns.size() == allInns.size())
			return "noDeleteAll";
		return "";
	}

	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		amendment.setProdInn(delInns);
		
		AmdREMOVE_API amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdREMOVE_API(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdREMOVE_API(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	public List<ProdInnDTO> getAllInns() {
		if(allInns != null && allInns.size() > 0)
			return allInns;
		
		allInns = getAmendmentService().fetchProdInnDTO(amendment);
		return allInns;
	}

	public void setAllInns(List<ProdInnDTO> list) {
		this.allInns = list;
	}

	public void setDeleteProdInnDTO(ProdInnDTO select) {
		if(allInns != null && select != null){
			for(ProdInnDTO dto:allInns){
				if(dto.getProdInn().getId() == select.getProdInn().getId()){
					dto.setDelete(!dto.isDelete());
					break;
				}
			}
		}
	}
	
	public boolean isChanged(){
		boolean ch = false;
		if(amendment != null && getAllInns() != null){
			delInns = new ArrayList<ProdInn>();
			for(ProdInnDTO dto:getAllInns()){
				if(dto.isDelete())
					delInns.add(dto.getProdInn());
			}
			if(delInns.size() > 0)
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
	
	public List<ProdInn> getDelInns() {
		return delInns;
	}

	public void setDelInns(List<ProdInn> list) {
		this.delInns = list;
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
