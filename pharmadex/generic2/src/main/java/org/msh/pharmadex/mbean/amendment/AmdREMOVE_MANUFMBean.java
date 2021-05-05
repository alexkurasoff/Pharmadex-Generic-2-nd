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
import org.msh.pharmadex.domain.ProdCompany;
import org.msh.pharmadex.domain.amendment.AmdREMOVE_MANUF;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.enums.CompanyType;
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
public class AmdREMOVE_MANUFMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
	
	@ManagedProperty(value = "#{innService}")
	private InnService innService;
		
	private AmdREMOVE_MANUF amendment = null;
	
	private List<ProdCompanyDTO> allManufs = null;
	private List<ProdCompany> delManufs = null;

	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdREMOVE_MANUF(id);
			if(amendment.getState() != null && !amendment.getState().equals(AmdState.SAVED))
				delManufs = amendment.getProdCompany();
		}else{
			amendment = new AmdREMOVE_MANUF();
		}
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdREMOVE_MANUF) amd;
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
		
		if(delManufs.size() == allManufs.size())
			return "noDeleteAll";
		
		if((allManufs.size() - delManufs.size()) == 1){
			for(ProdCompanyDTO dto:allManufs){
				if(!dto.isDelete()){
					if(!dto.getProdCompany().getCompanyType().equals(CompanyType.FIN_PROD_MANUF))
						return "no_fin_prod_manuf";
				}
			}
		}
		return "";
	}

	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		amendment.setProdCompany(delManufs);
		
		AmdREMOVE_MANUF amd = getAmendmentService().saveAndFlush(amendment);
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
		String error = getAmendmentService().rejectAmdREMOVE_MANUF(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdREMOVE_MANUF(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}

	public List<ProdCompanyDTO> getAllManufs() {
		if(allManufs != null && allManufs.size() > 0)
			return allManufs;
		
		allManufs = getAmendmentService().fetchProdCompanyDTO(amendment);
		return allManufs;
	}

	public void setAllManufs(List<ProdCompanyDTO> list) {
		this.allManufs = list;
	}

	public void setDeleteProdCompanyDTO(ProdCompanyDTO select) {
		if(allManufs != null && select != null){
			for(ProdCompanyDTO dto:allManufs){
				if(dto.getProdCompany().getId() == select.getProdCompany().getId()){
					dto.setDelete(!dto.isDelete());
					break;
				}
			}
		}
	}
	
	public boolean isChanged(){
		boolean ch = false;
		if(amendment != null && getAllManufs() != null){
			delManufs = new ArrayList<ProdCompany>();
			for(ProdCompanyDTO dto:getAllManufs()){
				if(dto.isDelete())
					delManufs.add(dto.getProdCompany());
			}
			if(delManufs.size() > 0)
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
	
	public List<ProdCompany> getDelManufs() {
		return delManufs;
	}

	public void setDelManufs(List<ProdCompany> list) {
		this.delManufs = list;
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
