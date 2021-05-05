package org.msh.pharmadex.mbean.product;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import org.msh.pharmadex.domain.SuspDetail;
import org.msh.pharmadex.domain.enums.RecomendType;
import org.msh.pharmadex.domain.enums.SuspensionStatus;
import org.msh.pharmadex.service.ProdApplicationsServiceMZ;
import org.msh.pharmadex.service.ProductService;
import org.msh.pharmadex.service.SuspendServiceMZ;

/**
 * Author: usrivastava
 */
@ManagedBean
@RequestScoped
public class ProductMbean implements Serializable {
	private static final long serialVersionUID = -7982763544138941526L;

	@ManagedProperty(value = "#{productService}")
	private ProductService productService;
	
	@ManagedProperty(value = "#{prodApplicationsServiceMZ}")
	protected ProdApplicationsServiceMZ prodApplicationsServiceMZ;
	
	@ManagedProperty(value = "#{suspendServiceMZ}")
	protected SuspendServiceMZ suspendServiceMZ;

	private List<ProdTable> filteredProducts;
	private List<ProdTable> suspendedProds;
	private List<ProdTable> canceledProds;
	
	private List<SuspDetail> suspList;
	
	public String sentToDetail(Long id) {
		Flash flash = FacesContext.getCurrentInstance().getExternalContext().getFlash();
		flash.put("prodAppID", id);
		return "/internal/processreg?faces-redirect=true";
	}

	public List<ProdTable> getFilteredProducts() {
		return filteredProducts;
	}

	public void setFilteredProducts(List<ProdTable> filteredProducts) {
		this.filteredProducts = filteredProducts;
	}

	public String goToDetails(Long id) {
		Flash flash = FacesContext.getCurrentInstance().getExternalContext().getFlash();
		flash.put("prodAppID", id);
		flash.put("backTo","/public/suspendedproducts.xhtml");
		return "/internal/processreg?faces-redirect=true";
	}

	public List<ProdTable> getSuspendedProds() {
		if (suspendedProds == null) {
			suspendedProds = productService.findSuspendedProducts();
			getSuspList();
		}
		return suspendedProds;
	}

	public void setSuspendedProds(List<ProdTable> suspendedProds) {
		this.suspendedProds = suspendedProds;
	}

	public List<ProdTable> getCanceledProds() {
		if (canceledProds == null) {
			canceledProds = productService.findCanceledProduct();
			getSuspList();
		}
		return canceledProds;
	}

	public void setCanceledProds(List<ProdTable> revokedProds) {
		this.canceledProds = revokedProds;
	}
	
	public List<SuspDetail> getSuspList() {
		if(suspList == null){
			suspList = suspendServiceMZ.findAll(null);
		}
		return suspList;
	}

	public void setSuspList(List<SuspDetail> suspList) {
		this.suspList = suspList;
	}

	public Date findDueDateSuspDetail(Long prodAppID) {
		SuspDetail susp = findSuspDetail(prodAppID, RecomendType.SUSPEND);
		if(susp != null)
			return susp.getDueDate();
		return null;
	}
	
	public Date findDecisionDateSuspDetail(Long prodAppID) {
		SuspDetail susp = findSuspDetail(prodAppID, RecomendType.CANCEL);
		if(susp != null)
			return susp.getDecisionDate();
		return null;
	}
	
	private SuspDetail findSuspDetail(Long prodAppID, RecomendType typeDecision){
		if(prodAppID != null && suspList != null){
			for(SuspDetail susp:suspList){
				if(susp.getProdApplications() != null && susp.getDecision().equals(typeDecision)
						&& susp.getSuspensionStatus().equals(SuspensionStatus.REQUESTED)){
					if(susp.getProdApplications().getId().intValue() == prodAppID.intValue())
						return susp;
				}
			}
		}
		return null;
	}
    
	public ProdApplicationsServiceMZ getProdApplicationsServiceMZ() {
		return prodApplicationsServiceMZ;
	}

	public void setProdApplicationsServiceMZ(ProdApplicationsServiceMZ prodApplicationsServiceMZ) {
		this.prodApplicationsServiceMZ = prodApplicationsServiceMZ;
	}

	public SuspendServiceMZ getSuspendServiceMZ() {
		return suspendServiceMZ;
	}

	public void setSuspendServiceMZ(SuspendServiceMZ suspendServiceMZ) {
		this.suspendServiceMZ = suspendServiceMZ;
	}
	
	public ProductService getProductService() {
		return productService;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}
}
