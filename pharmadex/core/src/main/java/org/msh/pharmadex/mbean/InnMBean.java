package org.msh.pharmadex.mbean;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.msh.pharmadex.domain.Excipient;
import org.msh.pharmadex.domain.Inn;
import org.msh.pharmadex.service.InnService;
import org.springframework.web.util.WebUtils;

@ManagedBean
@ViewScoped
public class InnMBean implements Serializable {

	FacesContext facesContext = FacesContext.getCurrentInstance();
	java.util.ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");

	@ManagedProperty(value = "#{innService}")
	InnService innService;
	
	private boolean newInn = true;
	private boolean newExc = true;
	
	private List<Inn> allInns;
	private List<Inn> filteredInns;
	
	private List<Excipient> allExcipients;
	private List<Excipient> filteredExcipients;

	private Inn selectedInn = null;
	private Excipient selectedExcipient = null;
	
	private String oldNameInn = "";
	private String oldNameExp = "";
	
	private String backTo = "/admin/innlist.faces";


	public String exception() throws Exception {
		throw new Exception();
	}

	@PostConstruct
	private void init() {
		selectedInn = new Inn();
		selectedExcipient = new Excipient();
	}

	public void addInn() {
		selectedInn = new Inn();
	}
	
	public void cancelInn() {
		selectedInn = new Inn();
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		WebUtils.setSessionAttribute(request, "innMBean", null);

	}
	
	public void cancelExcipient() {
		selectedExcipient = new Excipient();
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		WebUtils.setSessionAttribute(request, "innMBean", null);
	}
	
	public void addExcipient() {
		selectedExcipient = new Excipient();
	}
	
	public String saveInn() {
		facesContext = FacesContext.getCurrentInstance();
		String n = selectedInn.getName();
		selectedInn.setName(selectedInn.getName().trim());
		
		if(innService.isNameInnDuplicated(selectedInn)){
			FacesMessage msg = new FacesMessage("Dublicate value ", n);
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			facesContext.addMessage(null, msg);
			facesContext.validationFailed();
			return "";
		}
		
		selectedInn = innService.saveInn(selectedInn);
		if(selectedInn != null)
			selectedInn = new Inn();
		allInns = null;
		filteredInns = null;

		return backTo;
	}
	
	public String updateInn() {
		facesContext = FacesContext.getCurrentInstance();
		String n = selectedInn.getName();
		selectedInn.setName(selectedInn.getName().trim());
		
		if(innService.isNameInnDuplicated(selectedInn)){
			selectedInn.setName(oldNameInn);
			FacesMessage msg = new FacesMessage("Dublicate value ", n);
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			facesContext.addMessage(null, msg);
			facesContext.validationFailed();
			return "";
		}
		
		selectedInn = innService.updateInn(selectedInn);
		if(selectedInn != null)
			selectedInn = new Inn();
		allInns = null;
		filteredInns = null;
		return backTo;
	}
	
	public String updateExcipient() {
		facesContext = FacesContext.getCurrentInstance();
		String n = selectedExcipient.getName();
		selectedExcipient.setName(selectedExcipient.getName().trim());
		
		if(innService.isNameExcipientDuplicated(selectedExcipient)){
			selectedExcipient.setName(oldNameExp);
			FacesMessage msg = new FacesMessage("Dublicate value ", n);
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			facesContext.addMessage(null, msg);
			facesContext.validationFailed();
			return "";
		}
		
		selectedExcipient = innService.updateExcipient(selectedExcipient);
		if(selectedExcipient != null)
			selectedExcipient = new Excipient();
		allExcipients = null;
		filteredExcipients = null;
		return backTo;
	}
	
	public String saveExcipient() {
		facesContext = FacesContext.getCurrentInstance();
		String n = selectedExcipient.getName();
		selectedExcipient.setName(selectedExcipient.getName().trim());
		
		if(innService.isNameExcipientDuplicated(selectedExcipient)){
			FacesMessage msg = new FacesMessage("Dublicate value ", n);
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			facesContext.addMessage(null, msg);
			facesContext.validationFailed();
			return "";
		}
		
		selectedExcipient = innService.saveExcipient(selectedExcipient);
		if(selectedExcipient != null)
			selectedExcipient = new Excipient();
		allExcipients = null;
		filteredExcipients = null;

		return backTo;
	}
	
	public Inn getSelectedInn() {
		return selectedInn;
	}

	public void setSelectedInn(Inn selectedInn) {
		this.selectedInn = selectedInn;
	}

	public Excipient getSelectedExcipient() {
		return selectedExcipient;
	}

	public void setSelectedExcipient(Excipient selectedExcipient) {
		this.selectedExcipient = selectedExcipient;
	}

	public List<Inn> getAllInns() {
		allInns = innService.getInnList();
		return allInns;
	}

	public void setAllInns(List<Inn> allInns) {
		this.allInns = allInns;
	}
	
	public List<Inn> getFilteredInns() {
		return filteredInns;
	}

	public List<Excipient> getAllExcipients() {
		allExcipients = innService.getExcipients();
		return allExcipients;
	}

	public void setAllExcipients(List<Excipient> allExcipients) {
		this.allExcipients = allExcipients;
	}

	public List<Excipient> getFilteredExcipients() {
		return filteredExcipients;
	}

	public void setFilteredExcipients(List<Excipient> filteredExcipients) {
		this.filteredExcipients = filteredExcipients;
	}

	public void setFilteredInns(List<Inn> filteredInns) {
		this.filteredInns = filteredInns;
	}

	public String getBackTo() {
		return backTo;
	}

	public void setBackTo(String backTo) {
		this.backTo = backTo;
	}

	public InnService getInnService() {
		return innService;
	}

	public void setInnService(InnService innService) {
		this.innService = innService;
	}

	public String getOldNameInn() {
		return oldNameInn;
	}

	public void setOldNameInn(String oldName) {
		this.oldNameInn = oldName;
	}

	public String getOldNameExp() {
		return oldNameExp;
	}

	public void setOldNameExp(String oldNameExp) {
		this.oldNameExp = oldNameExp;
	}

	public boolean isNewInn() {
		return newInn;
	}

	public void setNewInn(boolean newInn) {
		this.newInn = newInn;
	}
	
	public boolean isNewExc() {
		return newExc;
	}

	public void setNewExc(boolean newExc) {
		this.newExc = newExc;
	}

}
