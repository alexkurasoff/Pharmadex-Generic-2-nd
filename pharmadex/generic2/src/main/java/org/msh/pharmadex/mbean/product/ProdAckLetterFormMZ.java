/*
 * Copyright (c) 2014. Management Sciences for Health. All Rights Reserved.
 */

package org.msh.pharmadex.mbean.product;


import java.io.InputStream;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.enums.TemplateType;
import org.msh.pharmadex.service.ProdApplicationsService;
import org.msh.pharmadex.service.ProdApplicationsServiceMZ;
import org.msh.pharmadex.service.UserAccessService;
import org.primefaces.model.StreamedContent;

import net.sf.jasperreports.engine.JRException;

/**
 * Author: dudchenko
 */
@ManagedBean
@RequestScoped
public class ProdAckLetterFormMZ implements Serializable {

	@ManagedProperty(value = "#{userSession}")
	private UserSession userSession;
	
	@ManagedProperty(value = "#{templMBean}")
	private TemplMBean templMBean;

	@ManagedProperty(value = "#{prodApplicationsServiceMZ}")
	private ProdApplicationsServiceMZ prodApplicationsServiceMZ;

	private FacesContext context;
	private java.util.ResourceBundle bundle;

	public void initParametrs(ProdApplications prodApp) {
		context = FacesContext.getCurrentInstance();
		bundle = context.getApplication().getResourceBundle(context, "msgs");
		String send = prodApp.getUsername();
		if(send == null || send.trim().equals(""))
			send = bundle.getString("gestorDeCTRM_value");
		prodApp.setUsername(send);
	}

	public String getCreateLetterBtnName(String val){    
		context = FacesContext.getCurrentInstance();
		bundle = context.getApplication().getResourceBundle(context, "msgs");
		if(userSession.isStaff())
			return val;
		else
			return bundle.getString("btn_create_ackletter");
	}
	/**
	 * Create Dossier Receipt Ask Letter		
	 * @param prodApp
	 */
	public void createDossierReceiptLetter(ProdApplications prodApp){
		
		ProdApplicationsService paser = prodApplicationsServiceMZ.getProdApplicationsService();
		//TODO
		//20/09/17
		/*---
		String no = prodApp.getProdSrcNo();
		if(no == null || no.trim().equals("")){
						
			if(prodApp.getProdAppNo()==null){
				prodApp.setProdAppNo(paser.generateAppNo(prodApp));
			}
			prodApp.setProdSrcNo(paser.getSrcNumber(prodApp.getProdAppNo()));
		}
		--*/
		initParametrs(prodApp);
		
		StreamedContent streamedContent = getTemplMBean().getReport(prodApp.getId(), TemplateType.DOSSIER_RECIEPT);		
		prodApplicationsServiceMZ.createDossierReceiptAckLetter(prodApp, streamedContent, TemplateType.DOSSIER_RECIEPT);
		
	}

	public ProdApplicationsServiceMZ getProdApplicationsServiceMZ() {
		return prodApplicationsServiceMZ;
	}

	public void setProdApplicationsServiceMZ(ProdApplicationsServiceMZ prodApplicationsServiceMZ) {
		this.prodApplicationsServiceMZ = prodApplicationsServiceMZ;
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	
	/**
	 * Is generation of this letter allowed
	 * @return
	 */
	public boolean isAllowed(ProdApplications prodApp){
		if(prodApp == null){
			return false;
		}
		//20/09/17
		//return !getUserSession().isCompany() && prodApp.isScreeningNum();
		return !getUserSession().isCompany() && prodApp.getProdSrcNo()!=null;
	}

	public TemplMBean getTemplMBean() {
		return templMBean;
	}

	public void setTemplMBean(TemplMBean templMBean) {
		this.templMBean = templMBean;
	}
}
