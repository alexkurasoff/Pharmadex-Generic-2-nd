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
import org.msh.pharmadex.domain.amendment.AmdREPLACEMENT_IPI;
import org.msh.pharmadex.domain.amendment.AmdState;
import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.amendment.AmendmentService;

/**
 * This class serves Application related amendment - new description and physical appearance
 * @author Alex Kurasoff
 *
 */
@ManagedBean
@ViewScoped
public class AmdREPLACEMENT_IPIMBean implements IAmendment {
	
	@ManagedProperty(value="#{userSession}")
	private UserSession userSession;
		
	@ManagedProperty(value="#{amendmentService}")
	AmendmentService amendmentService;
	
	@ManagedProperty(value="#{productService}")
	ProductService productService;
		
	private AmdREPLACEMENT_IPI amendment = null;
	
	private List<ProdExcipient> prodExcipients = new ArrayList<ProdExcipient>();
	//private ProdExcipient selectProdExcipient = null;
	
	//private ProdExcipient editedProdExcipient = null;
	//private ProdExcipient newProdExcipient = null;
	//private ProdExcipient newEditProdExcipient = null;
	
	private ProdExcipient selectedValue = null;
	private ProdExcipient editedValue = null;
	private ProdExcipient newValue = null;
	private ProdExcipient value = null;
	
	private FacesContext context = FacesContext.getCurrentInstance();
	private ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
	
	@Override
	public Amendment findAmendment(Long id) {		
		if(id != null && id > 0){
			amendment = getAmendmentService().findAmdREPLACEMENT_IPI(id);
			if(amendment.getProdExcipient() != null && amendment.getProdExcipient().size() == 2){
				if(!amendment.getState().equals(AmdState.APPROVED)){
					editedValue = getAmendmentService().findProdExcipientEdit(amendment, true);
					newValue = getAmendmentService().findProdExcipientEdit(amendment, false);
				}else{
					// old-product is null
					editedValue = getAmendmentService().findProdExcipientEdit(amendment, false);
					newValue = getAmendmentService().findProdExcipientEdit(amendment, true);
				}
			}
		}else{
			amendment = new AmdREPLACEMENT_IPI();
		}
		prodExcipients = new ArrayList<ProdExcipient>();
		return amendment;
	}
	
	@Override
	public Amendment getAmendment() {
		return amendment;
	}

	@Override
	public void setAmendment(Amendment amd) {
		amendment = (AmdREPLACEMENT_IPI) amd;
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
		if(amendment.getState() != null && amendment.getState().equals(AmdState.SAVED)){
			if(amendment.getProdExcipient() != null)
				return "";
		}
		if(isProdExcipientChanged())
			return "";
		return "noamdchanges";
	}

	/**
	 * Save an amendment with state SAVED
	 */
	@Override
	public String save(String step) {
		AmdREPLACEMENT_IPI amd =  getAmendmentService().saveAndFlush(amendment);		
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
		String error = getAmendmentService().rejectAmdREPLACEMENT_IPI(amendment, subject, getUserSession().getLoggedInUserObj());
		return error;	
	}

	@Override
	public String implement(ProdApplications currentProdApp) {
		String subject = bundle.getString(amendment.getDictionary().getSubject().getKey());
		String error = getAmendmentService().implementAmdREPLACEMENT_IPI(amendment, currentProdApp, subject, getUserSession().getLoggedInUserObj());
		return error;
	}
	
	/*public ProdExcipient getNewProdExcipient(){
		if(isOpenChangePRodExcipient())
			return newProdExcipient;
		
		//newEditProdExcipient = getAmendmentService().copyProdExcipient(selectProdExcipient);
		return newEditProdExcipient;
	}
	
	private boolean isOpenChangePRodExcipient(){
		if(editedProdExcipient != null && selectProdExcipient != null){
			if(editedProdExcipient.getId() == selectProdExcipient.getId())
				return true;
		}
		return false;
	}
	*/
	public List<ProdExcipient> getProdExcipients() {
		if(!(prodExcipients != null && prodExcipients.size() > 0))
			prodExcipients = getAmendmentService().fetchProdExcipients(amendment);
		return prodExcipients;
	}

	public void setProdExcipients(List<ProdExcipient> prodExcipients) {
		this.prodExcipients = prodExcipients;
	}
	
	public boolean isProdExcipientChanged(){
		boolean ch = false;
		if(amendment != null && editedValue != null && newValue != null){
			if(editedValue.getExcipient().getId() != newValue.getExcipient().getId())
				return true;
			if(!editedValue.getDosStrength().equals(newValue.getDosStrength()))
				return true;
			if(editedValue.getDosUnit().getId() != newValue.getDosUnit().getId())
				return true;
		}
		return ch;
	}
	public void updateProdExc(){
		editedValue = getSelectedValue();
		if(newValue != null){
			newValue.setExcipient(value.getExcipient());
			newValue.setDosStrength(value.getDosStrength());
			newValue.setDosUnit(getProductService().getDosUomDAO().findOne(value.getDosUnit().getId()));
			newValue.setRefStd(value.getRefStd());
			newValue.setFunction(value.getFunction());
		}else
			newValue = value;
		
		if(editedValue != null && selectedValue != null &&
				editedValue.getId() != selectedValue.getId())
			editedValue = selectedValue;
		
		List<ProdExcipient> list = new ArrayList<ProdExcipient>();
		list.add(editedValue);
		list.add(newValue);
		amendment.setProdExcipient(list);
	}
	
	public void cancelProdExc(){
	}
	
	public void setSelectedRowItem(ProdExcipient exc){
		if(exc != null){
			setSelectedValue(getAmendmentService().getProdExcipientDAO().findOne(exc.getId()));
			if(editedValue != null){
				if(exc.getId() == editedValue.getId()){
					value = newValue;
				}else
					value = getAmendmentService().copyProdExcipient(getSelectedValue());
			}else
				value = getAmendmentService().copyProdExcipient(getSelectedValue());
			
			//getCompanyMBean().setSelectedCompany(value.getCompany());
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
	
	public String getStyleRow(ProdExcipient item){
		if(editedValue != null && item.getId() == editedValue.getId())
			return "rowSelect";
		return null;
	}
	
	public ProdExcipient getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(ProdExcipient selectedValue) {
		this.selectedValue = selectedValue;
	}

	public ProdExcipient getNewValue() {
		return newValue;
	}

	public void setNewValue(ProdExcipient newValue) {
		this.newValue = newValue;
	}
	
	public ProdExcipient getValue() {
		return value;
	}

	public void setNew(ProdExcipient newValue) {
		this.value = newValue;
	}

	public ProdExcipient getEditedValue() {
		return editedValue;
	}

	public void setEditedValue(ProdExcipient editedValue) {
		this.editedValue = editedValue;
	}

	public void setValue(ProdExcipient value) {
		this.value = value;
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
}
