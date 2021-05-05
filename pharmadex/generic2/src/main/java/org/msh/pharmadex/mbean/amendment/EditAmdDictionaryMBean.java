package org.msh.pharmadex.mbean.amendment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.msh.pharmadex.domain.amendment.AmdConditions;
import org.msh.pharmadex.domain.amendment.AmendmentCondition;
import org.msh.pharmadex.domain.amendment.AmendmentDictionary;
import org.msh.pharmadex.domain.amendment.AmendmentDocument;
import org.msh.pharmadex.domain.enums.AmendmentProcess;
import org.msh.pharmadex.domain.enums.AmendmentSubject;
import org.msh.pharmadex.domain.enums.PharmadexDB;
import org.msh.pharmadex.service.amendment.AmendmentDictionaryService;
import org.msh.pharmadex.service.amendment.AmendmentService;
import org.msh.pharmadex.util.JsfUtils;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;

@ManagedBean
@ViewScoped
public class EditAmdDictionaryMBean {

	@ManagedProperty(value = "#{amendmentDictionaryService}")
	private AmendmentDictionaryService amendmentDictionaryService;

	@ManagedProperty(value = "#{amendmentService}")
	private AmendmentService amendmentService;

	@ManagedProperty("#{flash}")
	private Flash flash;

	private static final String RETURN_TO = "return_to";
	private String AMD_DIC_ID = "amdDicID";

	private List<AmendmentDictionary> fullList = new ArrayList<AmendmentDictionary>();

	private AmendmentDictionary selectedItem;
	private AmendmentCondition selectedCondition;
	private AmendmentDocument selectedDocument;

	private AmendmentSubject selectedViewSubject = null;
	private AmendmentProcess selectedViewProcess = null;

	private int selectedTab;
	private String returnPath="/home.faces";
	
	private List<AmdConditions> listAmdConditions = null;

	@PostConstruct
	public void init(){
		Long dicId = (Long) getFlash().get(AMD_DIC_ID);
		if(dicId != null){
			selectedItem = getAmendmentDictionaryService().findAmendmentDictionary(dicId);
			if(selectedItem != null){
			}else
				informUser("global_fail", FacesMessage.SEVERITY_ERROR);
		}else{
			selectedItem = new AmendmentDictionary();
			selectedItem.setActive(true);
		}
		//return path
		String path = (String) getFlash().get(RETURN_TO);
		if(path != null){
			setReturnPath(path);
		}
	}

	private void informUser(String key, Severity severity) {
		FacesContext context = FacesContext.getCurrentInstance();
		ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msgs");
		context.addMessage(null, new FacesMessage(severity, bundle.getString(key), ""));
		FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("reghome:growl");
	}

	public String getReturnPath() {
		return returnPath;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}

	public Flash getFlash() {
		return flash;
	}

	public void setFlash(Flash flash) {
		this.flash = flash;
	}


	public AmendmentSubject getSelectedViewSubject() {
		return selectedViewSubject;
	}

	public void setSelectedViewSubject(AmendmentSubject selectedViewSubject) {
		this.selectedViewSubject = selectedViewSubject;
	}

	public AmendmentProcess getSelectedViewProcess() {
		return selectedViewProcess;
	}

	public void setSelectedViewProcess(AmendmentProcess selectedViewProcess) {
		this.selectedViewProcess = selectedViewProcess;
	}

	public AmendmentService getAmendmentService() {
		return amendmentService;
	}

	public void setAmendmentService(AmendmentService amendmentService) {
		this.amendmentService = amendmentService;
	}

	public void removeFromSet(AmendmentDictionary item){
		if(fullList != null && fullList.size() > 0){
			for(AmendmentDictionary dic:fullList){
				if(dic.getId().intValue() == item.getId().intValue()){
					dic.setActive(!item.isActive());
					getAmendmentDictionaryService().getAmendmentDictionaryDAO().saveAndFlush(dic);
				}
			}
		}
	}

	public void changeSelectedItem(AmendmentDictionary item){
		setSelectedItem(item);
	}

	public AmendmentDictionary getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(AmendmentDictionary selectedItem) {
		this.selectedItem = selectedItem;
	}

	public List<AmendmentDictionary> getFullList(boolean isEdit) {
		fullList = getAmendmentDictionaryService().findAll(selectedViewSubject, selectedViewProcess, isEdit);
		return fullList;
	}

	public void setFullList(List<AmendmentDictionary> fullList) {
		this.fullList = fullList;
	}

	public AmendmentDictionaryService getAmendmentDictionaryService() {
		return amendmentDictionaryService;
	}

	public void setAmendmentDictionaryService(AmendmentDictionaryService amendmentDictionaryService) {
		this.amendmentDictionaryService = amendmentDictionaryService;
	}

	public int getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(int selectedTab) {
		this.selectedTab = selectedTab;
	}

	public List<String> completeChapters(String query) {
		return JsfUtils.completeSuggestions(query, getAmendmentDictionaryService().fetchListChapters());
	}

	public void onItemSelect(SelectEvent event) {
		selectedItem.setSubChapter("");
	}

	public void onRowSelect(SelectEvent event) {
		listAmdConditions = null;
	}

	public List<String> completeSubChapters(String query) {
		return JsfUtils.completeSuggestions(query, getAmendmentDictionaryService().fetchListSubChapters(selectedItem.getChapter()));
	}

	public List<AmendmentSubject> getAmdSubjectes(){
		return Arrays.asList(AmendmentSubject.values());
	}

	public List<AmendmentProcess> getAmdProcesses(){
		return Arrays.asList(AmendmentProcess.values());
	}

	public List<PharmadexDB> getPharmadexDBes(){
		return Arrays.asList(PharmadexDB.values());
	}

	public AmendmentCondition getSelectedCondition() {
		if(selectedCondition == null){
			selectedCondition = new AmendmentCondition();
			selectedCondition.setActive(true);
		}
		return selectedCondition;
	}

	public void setSelectedCondition(AmendmentCondition selectedCondition) {
		this.selectedCondition = selectedCondition;
	}

	public AmendmentDocument getSelectedDocument() {
		if(selectedDocument == null){
			selectedDocument = new AmendmentDocument();
			selectedDocument.setActive(true);
		}
		return selectedDocument;
	}

	public void setSelectedDocument(AmendmentDocument selectedDocument) {
		this.selectedDocument = selectedDocument;
	}

	public void onTabChange(TabChangeEvent event) {

	}

	public void subjectChangeListenener() {
		setSelectedItem(null);
	}
	
	public void onAnswerChangeListenener() {
		//ValueChangeEvent event
	}

	public void proccesChangeListenener() {
		setSelectedItem(null);
	}

	public void addAmendmentCondition(){
		if(selectedCondition != null){
			String err = getAmendmentDictionaryService().varificationAmendmentCondition(selectedCondition);
			if(err.length() > 0)
				informUser(err, FacesMessage.SEVERITY_ERROR);
			else{
				if(selectedCondition.getId() != null && selectedCondition.getId() > 0){
					int index = -1;
					for(int i = 0; i < getSelectedItem().getAmendmentConditions().size(); i++){
						if(getSelectedItem().getAmendmentConditions().get(i).getId().intValue() == selectedCondition.getId().intValue()){
							index = i;
							break;
						}
					}
					if(index > 0){
						getSelectedItem().getAmendmentConditions().set(index, selectedCondition);
					}
				}else{
					if(selectedCondition.getRowIndex()>=0){
						getSelectedItem().getAmendmentConditions().set(selectedCondition.getRowIndex(), selectedCondition);
					}else{
						getSelectedItem().getAmendmentConditions().add(selectedCondition);
					}
				}

				selectedCondition = null;
			}
		}
	}

	public void addAmendmentDocument(){
		if(selectedDocument != null){
			String err = getAmendmentDictionaryService().varificationAmendmentDocument(selectedDocument);
			if(err.length() > 0)
				informUser(err, FacesMessage.SEVERITY_ERROR);
			else{
				if(selectedDocument.getId() != null && selectedDocument.getId() > 0){
					int index = -1;
					for(int i = 0; i < getSelectedItem().getAmendmentDocuments().size(); i++){
						if(getSelectedItem().getAmendmentDocuments().get(i).getId().intValue() == selectedDocument.getId().intValue()){
							index = i;
							break;
						}
					}
					if(index > 0){
						getSelectedItem().getAmendmentDocuments().set(index, selectedDocument);
					}
				}else{
					if(selectedDocument.getRowIndex()>=0){
						getSelectedItem().getAmendmentDocuments().set(selectedDocument.getRowIndex(), selectedDocument);
					}else{
						getSelectedItem().getAmendmentDocuments().add(selectedDocument);
					}
				}

				selectedDocument = null;
			}
		}
	}

	public void selectCondition(AmendmentCondition amdCond){
		setSelectedCondition(amdCond);
	}

	public void selectDocument(AmendmentDocument amdDoc){
		setSelectedDocument(amdDoc);
	}

	/**
	 * Store amendment's id to the flash storage
	 * @param amdId
	 */
	public void setAmdIdToFlash(Long id){
		getFlash().put(AMD_DIC_ID, id);
	}

	/**
	 * Set submit/cancel path to the flash storage
	 * @param path
	 */
	public void setReturnPathToFlash(String path){
		getFlash().put(RETURN_TO, path);
	}

	public String exit(){
		return getReturnPath();
	}

	public void cancel(){
		selectedCondition = null;
	}

	public String saveDictionary(){
		if(selectedItem != null){
			String err = getAmendmentDictionaryService().varificationAmendmentDictionary(selectedItem);
			if(err.length() > 0){
				informUser(err, FacesMessage.SEVERITY_ERROR);
			}else{
				getAmendmentDictionaryService().getAmendmentDictionaryDAO().saveAndFlush(selectedItem);
				informUser("global.success", FacesMessage.SEVERITY_INFO);
				return getReturnPath();
			}
		}
		return "";
	}
	
	public List<AmdConditions> getListAmdConditions(){
		if(listAmdConditions == null){
			if(getSelectedItem() != null && getSelectedItem().getId() != null && getSelectedItem().getId() > 0)
				listAmdConditions = getAmendmentDictionaryService().fetchAmdConditionByDictionary(getSelectedItem().getId());
		}
		
		return listAmdConditions;
	}
	
	public void setListAmdConditions(List<AmdConditions> lst){
		listAmdConditions = lst;
	}
}
