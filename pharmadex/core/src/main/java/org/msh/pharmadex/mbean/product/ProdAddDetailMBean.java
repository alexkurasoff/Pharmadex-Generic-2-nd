package org.msh.pharmadex.mbean.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.msh.pharmadex.dao.iface.DosUomDAO;
import org.msh.pharmadex.domain.Atc;
import org.msh.pharmadex.domain.DosUom;
import org.msh.pharmadex.domain.Excipient;
import org.msh.pharmadex.domain.Inn;
import org.msh.pharmadex.domain.ProdExcipient;
import org.msh.pharmadex.domain.ProdInn;
import org.msh.pharmadex.mbean.GlobalLists;
import org.msh.pharmadex.service.AtcService;
import org.msh.pharmadex.service.GlobalEntityLists;
import org.msh.pharmadex.service.InnService;
import org.msh.pharmadex.util.JsfUtils;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by utkarsh on 3/29/15.
 */
@ManagedBean
@RequestScoped
public class ProdAddDetailMBean implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(CompanyMBean.class);
	FacesContext context;
	ResourceBundle bundle;
	@ManagedProperty(value = "#{innService}")
	private InnService innService;
	@ManagedProperty(value = "#{dosUomDAO}")
	private DosUomDAO dosUomDAO;
	@ManagedProperty(value = "#{prodRegAppMbean}")
	private ProdRegAppMbean prodRegAppMbean;
	@ManagedProperty(value = "#{atcService}")
	private AtcService atcService;
	@ManagedProperty(value = "#{globalEntityLists}")
	private GlobalEntityLists globalEntityLists;
	private Atc atc;
	private TreeNode selAtcTree;
	private ProdInn prodInn = new ProdInn();
	private ProdExcipient prodExcipient = new ProdExcipient();


	public String openAddInn() {
		prodInn = new ProdInn();
		prodInn.setInn(new Inn());
		prodInn.setDosUnit(new DosUom());
		return null;
	}

	public String openAddExp() {
		prodExcipient = new ProdExcipient();
		prodExcipient.setExcipient(new Excipient());
		prodExcipient.setDosUnit(new DosUom());
		return null;
	}

	public String addProdInn() {
		context = FacesContext.getCurrentInstance();
		bundle = context.getApplication().getResourceBundle(context, "msgs");

		try {
			if (prodInn.getInn().getId() == null)
				prodInn.setInn(innService.addInn(prodInn.getInn()));
			else
				prodInn.setDosUnit(dosUomDAO.findOne(prodInn.getDosUnit().getId()));

			prodInn.setProduct(prodRegAppMbean.getProduct());
			prodInn.setDosUnit(dosUomDAO.findOne(prodInn.getDosUnit().getId()));
			prodRegAppMbean.getSelectedInns().add(prodInn);

			List<Atc> selectedAtcs = prodRegAppMbean.getSelectedAtcs();
			List<Atc> a = atcService.findAtcByName(prodInn.getInn().getName());
			if (a != null) {
				if (selectedAtcs == null)
					selectedAtcs = new ArrayList<Atc>();
				selectedAtcs.addAll(a);
			}
			prodInn = new ProdInn();
			prodInn.setDosUnit(new DosUom());
		} catch (Exception e) {
			FacesMessage msg = new FacesMessage(e.getMessage(), "Detail....");
			context.addMessage(null, new FacesMessage(bundle.getString("product_innname_valid")));

		}
		return null;
	}

	/**
	 * Get excipients, except already selected
	 * @param query increments query from UI
	 * @return
	 */
	public List<Excipient> completeExcipients(String query) {
		List<Excipient> restExcipients = globalEntityLists.getExcipients();
		if(prodRegAppMbean.getProduct().getExcipients() != null) {
			for(ProdExcipient exp : prodRegAppMbean.getProduct().getExcipients()) {
				restExcipients.remove(exp.getExcipient());
			}
		}
		if(prodRegAppMbean.getSelectedExipients() != null) {
			for(ProdExcipient exp : prodRegAppMbean.getSelectedExipients()) {
				restExcipients.remove(exp.getExcipient());
			}
		}
		return JsfUtils.completeSuggestions(query, restExcipients);
	}

	public String addProdExcipient() {
		try {
			context = FacesContext.getCurrentInstance();
			if (prodExcipient.getExcipient().getId() == null)
				prodExcipient.setExcipient(innService.addExcipient(prodExcipient.getExcipient()));
			else
				prodExcipient.setDosUnit(dosUomDAO.findOne(prodExcipient.getDosUnit().getId()));

			List<ProdExcipient> selectedExipients = prodRegAppMbean.getSelectedExipients();
			prodExcipient.setProduct(prodRegAppMbean.getProduct());
			prodExcipient.setDosUnit(dosUomDAO.findOne(prodExcipient.getDosUnit().getId()));
			if(!selectedExipients.contains(prodExcipient.getExcipient())) {
				selectedExipients.add(prodExcipient);
			}
			prodExcipient = new ProdExcipient();
			prodExcipient.setDosUnit(new DosUom());
		} catch (Exception ex) {
			ex.printStackTrace();
			context.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), ex.getMessage()));
		}

		return null;
	}

	public void onRowEdit(RowEditEvent event) {
		ProdExcipient prodExcipient;
		ProdInn prodInn;
		FacesMessage msg = null;
		try {
			if (event.getObject() instanceof ProdExcipient) {
				prodExcipient = (ProdExcipient) event.getObject();
				msg = new FacesMessage(prodExcipient.getExcipient().getName() + " updated");
			} else if (event.getObject() instanceof ProdInn) {
				prodInn = (ProdInn) event.getObject();
				msg = new FacesMessage(prodInn.getInn().getName() + " updated");
			}

			FacesContext.getCurrentInstance().addMessage(null, msg);
		} catch (Exception ex) {
			ex.printStackTrace();
			context.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("global_fail"), ex.getMessage()));
		}

	}

	public void onRowCancel(RowEditEvent event) {
		//        FacesMessage msg = new FacesMessage(((ProdExcipient) event.getObject()).getExcipient().getName() + " updated");
		//        FacesContext.getCurrentInstance().addMessage(null, msg);
	}


	public void onCellEdit(CellEditEvent event) {
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();

		if (newValue != null && !newValue.equals(oldValue)) {
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Changed", "Old: " + oldValue + ", New:" + newValue);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public String cancelAddInn() {
		prodInn = new ProdInn();
		prodInn.setDosUnit(new DosUom());
		return null;
	}

	public String cancelAddExcipient() {
		prodExcipient = new ProdExcipient();
		prodExcipient.setDosUnit(new DosUom());
		return null;
	}


	public void openAddATC() {
		RegATCHelper regATCHelper = new RegATCHelper(atc, globalEntityLists);
	}

	public String addAtc() {
		List<Atc> selectedAtcs = prodRegAppMbean.getSelectedAtcs();
		if (selectedAtcs == null)
			selectedAtcs = new ArrayList<Atc>();
		selectedAtcs.add(atc);
		//        prodApplications.getProd().setAtcs(selectedAtcs);
		atc = null;
		return null;
	}

	public TreeNode getSelAtcTree() {
		if (selAtcTree == null) {
			populateSelAtcTree();
		}
		return selAtcTree;
	}

	public void setSelAtcTree(TreeNode selAtcTree) {
		this.selAtcTree = selAtcTree;
	}

	private void populateSelAtcTree() {
		selAtcTree = new DefaultTreeNode("selAtcTree", null);
		selAtcTree.setExpanded(true);
		if (atc != null) {
			List<Atc> parentList = atc.getParentsTreeList(true);
			TreeNode[] nodes = new TreeNode[parentList.size()];
			for (int i = 0; i < parentList.size(); i++) {
				if (i == 0) {
					nodes[i] = new DefaultTreeNode(parentList.get(i).getAtcCode() + ": " + parentList.get(i).getAtcName(), selAtcTree);
					nodes[i].setExpanded(true);
				} else {
					nodes[i] = new DefaultTreeNode(parentList.get(i).getAtcCode() + ": " + parentList.get(i).getAtcName(), nodes[i - 1]);
					nodes[i].setExpanded(true);
				}
			}
		}
	}

	public void updateAtc() {
		populateSelAtcTree();
	}

	public ProdInn getProdInn() {
		return prodInn;
	}

	public void setProdInn(ProdInn prodInn) {
		this.prodInn = prodInn;
	}

	public ProdExcipient getProdExcipient() {
		return prodExcipient;
	}

	public void setProdExcipient(ProdExcipient prodExcipient) {
		this.prodExcipient = prodExcipient;
	}

	public InnService getInnService() {
		return innService;
	}

	public void setInnService(InnService innService) {
		this.innService = innService;
	}

	public DosUomDAO getDosUomDAO() {
		return dosUomDAO;
	}

	public void setDosUomDAO(DosUomDAO dosUomDAO) {
		this.dosUomDAO = dosUomDAO;
	}

	public ProdRegAppMbean getProdRegAppMbean() {
		return prodRegAppMbean;
	}

	public void setProdRegAppMbean(ProdRegAppMbean prodRegAppMbean) {
		this.prodRegAppMbean = prodRegAppMbean;
	}

	public AtcService getAtcService() {
		return atcService;
	}

	public void setAtcService(AtcService atcService) {
		this.atcService = atcService;
	}

	public GlobalEntityLists getGlobalEntityLists() {
		return globalEntityLists;
	}

	public void setGlobalEntityLists(GlobalEntityLists globalEntityLists) {
		this.globalEntityLists = globalEntityLists;
	}

	public Atc getAtc() {
		return atc;
	}

	public void setAtc(Atc atc) {
		this.atc = atc;
	}
}
