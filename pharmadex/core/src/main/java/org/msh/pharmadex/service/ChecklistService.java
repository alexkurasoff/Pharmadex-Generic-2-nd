package org.msh.pharmadex.service;

import org.msh.pharmadex.dao.iface.ChecklistDAO;
import org.msh.pharmadex.domain.Checklist;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.enums.ProdAppType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * Author: usrivastava
 */
@Service
public class ChecklistService implements Serializable {

    @Autowired
    private ChecklistDAO checklistDAO;

    private List<Checklist> checklists;

    /**
     * Method to populate the checklist based on the application type selected on step 2 of the wizard.
     * @parm prodApplications - application
     * @param header      whether to fetch the entire checklist or just header information
     * @return list of Checklist object.
     */
    public List<Checklist> getChecklists(ProdApplications prodApplications, boolean header) {
        if (prodApplications == null)
            return null;

        if(prodApplications.getProdAppType().equals(ProdAppType.RENEW)) {
            checklists = checklistDAO.findByRenewalOrderByIdAsc(true);
        } else if  (prodApplications.getProdAppType().equals(ProdAppType.VARIATION))
            checklists = checklistDAO.findByVariationOrderByIdAsc(true);
         else if (prodApplications.isSra()) {
            checklists = checklistDAO.findByRecognizedMedOrderByIdAsc(true);    }
        else if   (prodApplications.getProdAppType().equals(ProdAppType.NEW_CHEMICAL_ENTITY)) {
            checklists = checklistDAO.findByNewMedOrderByIdAsc(true);
        } 
        else {
            checklists = checklistDAO.findByGenMedOrderByIdAsc(true);
        }
//        else if(prodAppType.equals(ProdAppType.RECOGNIZED))
//            checklists = checklistDAO.findByHeaderAndRecognizedMed(true,true);
        return checklists;
    }

    //SRA is recognized medicine
    //addParam  - kind of variation (minor/major)
    public List<Checklist> getETChecklists(ProdApplications prodApplications, boolean addParam) {
        if (prodApplications == null)
            return null;

        if (prodApplications.getProdAppType().equals(ProdAppType.RENEW)) {
            checklists = checklistDAO.findByRenewalOrderByIdAsc(true);
        }else if  (prodApplications.getProdAppType().equals(ProdAppType.VARIATION)) {
                if (addParam) {
                    checklists = checklistDAO.findByMajvarOrderByIdAsc(true);
                }else {
                    checklists = checklistDAO.findByVariationOrderByIdAsc(true);
                }
        }
        else if (prodApplications.isSra())
            if (prodApplications.getProdAppType().equals(ProdAppType.NEW_CHEMICAL_ENTITY))
                checklists = checklistDAO.findBySRANewMeds(true);
            else
                checklists = checklistDAO.findByRecognizedMedOrderByIdAsc(true);
        else if (prodApplications.getProdAppType().equals(ProdAppType.NEW_CHEMICAL_ENTITY))
            checklists = checklistDAO.findByNewMedOrderByIdAsc(true);
        else
            checklists = checklistDAO.findByGenMedOrderByIdAsc(true);
        return checklists;
    }

	public List<Checklist> findAll() {
		return checklistDAO.findAll();
	
	}
    
	public Checklist updateList(Checklist l){
	return checklistDAO.save(l);
	}
}
