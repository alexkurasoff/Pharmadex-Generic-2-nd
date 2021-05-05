package org.msh.pharmadex.mbean.product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Calendar;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.ReviewDetail;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import net.sf.jasperreports.engine.JRException;

/**
 * Author: dudchenko
 */
@ManagedBean
@ViewScoped
public class FileUploadControllerMZ implements Serializable {

	private static final long serialVersionUID = 4527601943158238740L;
	
	@ManagedProperty(value = "#{processProdBnMZ}")
    private ProcessProdBnMZ processProdBnMZ;
	
	@ManagedProperty(value = "#{userSession}")
	protected UserSession userSession;
	
	//@ManagedProperty(value = "#{fileUploadController}")
   // private FileUploadController fileUploadController;
	
	@ManagedProperty(value = "#{reviewDetailBn}")
    private ReviewDetailBn reviewDetailBn;
	
	private StreamedContent regcert = null;
	private StreamedContent rejectcert = null;
	private boolean visRegcert = false;
	
	private FacesContext facesContext = FacesContext.getCurrentInstance();
	private java.util.ResourceBundle resourceBundle = facesContext.getApplication().getResourceBundle(facesContext, "msgs");

    public StreamedContent getRegcert() throws SQLException, IOException, JRException {
        ProdApplications prodApplications = processProdBnMZ.findProdApplications();
        if(prodApplications != null && prodApplications.getRegCert() != null){
	        InputStream ist = new ByteArrayInputStream(prodApplications.getRegCert());
	        Calendar c = Calendar.getInstance();
	        regcert = new DefaultStreamedContent(ist, "docx", "registration_" + prodApplications.getId() + "_" + c.get(Calendar.YEAR)+".docx");
        }
        return regcert;
    }
    
    public StreamedContent getRejectcert() throws SQLException, IOException, JRException {
        ProdApplications prodApplications = processProdBnMZ.findProdApplications();
        if(prodApplications != null && prodApplications.getRejCert() != null){
	        InputStream ist = new ByteArrayInputStream(prodApplications.getRejCert());
	        Calendar c = Calendar.getInstance();
	        rejectcert = new DefaultStreamedContent(ist, "docx", "rejection_" + prodApplications.getId() + "_" + c.get(Calendar.YEAR)+".docx");
        }
        return rejectcert;
    }
    
    public boolean isVisRegcert() {
    	visRegcert = false;
        if(userSession.getLoggedInUserObj() != null && !userSession.isCompany()){
        	ProdApplications prodApplications = processProdBnMZ.findProdApplications();
            if(prodApplications != null && prodApplications.getRegCert() != null)
            	visRegcert = true;
        }
		return visRegcert;
	}

	public void setVisRegcert(boolean visRegcert) {
		this.visRegcert = visRegcert;
	}

	public ProcessProdBnMZ getProcessProdBnMZ() {
        return processProdBnMZ;
    }

    public void setProcessProdBnMZ	(ProcessProdBnMZ process) {
        this.processProdBnMZ = process;
    }
    
    
    public ReviewDetailBn getReviewDetailBn() {
		return reviewDetailBn;
	}

	public void setReviewDetailBn(ReviewDetailBn reviewDetailBn) {
		this.reviewDetailBn = reviewDetailBn;
	}

	public void deleteReviewDetail(ReviewDetail reviewDetail) {
    	if(reviewDetail == null)
			return;
    	
		String fname = reviewDetail.getFilename();
		facesContext = FacesContext.getCurrentInstance();
		try {
			reviewDetail.setFile(null);
			reviewDetail.setFilename(null);
			reviewDetailBn.saveReview(false);
			reviewDetailBn.setFileImg(false);
			
			FacesMessage msg = new FacesMessage(resourceBundle.getString("global_delete"), fname + " " + resourceBundle.getString("is_deleted"));
			facesContext.addMessage(null, msg);
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			FacesMessage msg = new FacesMessage(resourceBundle.getString("global_fail"), fname + " " + resourceBundle.getString("cannot_delte"));
			facesContext.addMessage(null, msg);
		}
	}

	public UserSession getUserSession() {
		return userSession;
	}

	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	
	
}
