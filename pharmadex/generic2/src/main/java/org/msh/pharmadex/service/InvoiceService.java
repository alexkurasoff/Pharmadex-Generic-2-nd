package org.msh.pharmadex.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.msh.pharmadex.auth.UserSession;
import org.msh.pharmadex.dao.iface.InvoiceDAO;
import org.msh.pharmadex.domain.AgentAgreement;
import org.msh.pharmadex.domain.Applicant;
import org.msh.pharmadex.domain.Invoice;
import org.msh.pharmadex.domain.User;
import org.msh.pharmadex.domain.enums.InvoiceState;
import org.msh.pharmadex.mbean.InvoiceListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class InvoiceService implements Serializable {

    @Autowired
    InvoiceDAO invoiceDAO;

    @PersistenceContext
	EntityManager entityManager;
    
    public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
    public Invoice findInvoicesById(Long id) {
        return invoiceDAO.findInvoicesById(id);
    }

	public InvoiceDAO getInvoiceDAO() {
		return invoiceDAO;
	}

	public void setInvoiceDAO(InvoiceDAO invoiceDAO) {
		this.invoiceDAO = invoiceDAO;
	}
	 
	public String generateNo() {
		
		return "";
	}

	@Transactional(readOnly=true)
	public List<InvoiceListDTO> fetchInvoicesByState(User user, List<InvoiceState> state, UserSession userSession) {
		String sql = "SELECT inv FROM Invoice inv WHERE inv.state in (:a_states)";
		TypedQuery<Invoice> query = getEntityManager().createQuery(sql, Invoice.class);
		List<Invoice> invs = query.setParameter("a_states", state).getResultList();
		List<Invoice> byConvert = applyVisibleRulesToInvoices(user, invs, userSession);
		List<InvoiceListDTO> result = invoicesToDTO(byConvert);
		return result;
	}

	/**
	 * Filter invoice list based on user
	 * <ul>
	 * <li> RULE 1. this user is initiator (CreatedBy or UpdatedBy), regardless of user type, or
	 * <li> RULE 2. this user is responsible of the amendment's applicant, or
	 * <li> RULE 3. this user is responsible of a valid agent of amendmnet's applicant
	 * </ul>
	 * @param user
	 * @param saved
	 * @return
	 * 
	 */
	private List<Invoice> applyVisibleRulesToInvoices(User user, List<Invoice> saved, UserSession userSession) {
		List<Invoice> ret = new ArrayList<Invoice>();
		if(saved != null){
			for(Invoice amd : saved){
				//RULE1
				if(user.getUserId().equals(amd.getCreatedBy().getUserId()) || 
						user.getUserId().equals(amd.getUpdatedBy().getUserId())){
					ret.add(amd);
					continue;
				}
				//RULE2,3
				if(checkApplicantOrAgent(user, amd)){
					ret.add(amd);
					continue;
				}

				if(userSession.isReceiver()){// || userSession.isHead()){
					ret.add(amd);
					continue;
				}
			}
		}
		return ret;
	}

	/**
	 * RULE 2 and 3
	 * @param user
	 * @param inv
	 * @return
	 */
	private boolean checkApplicantOrAgent(User user, Invoice inv) {
		Applicant app = inv.getApplicant();
		//RULE 2
		List<User> users = app.getUsers();
		for(User u : users){
			if(u.getUserId().equals(user.getUserId())){
				return true;
			}
		}
		//RULE 3
		List<AgentAgreement> agents = inv.getApplicant().getAgents();
		if(agents!=null){
			for(AgentAgreement a : agents){
				if(a.isValid()){
					users = a.getAgent().getUsers();
					if(users != null){
						for(User u : users){
							if(u.getUserId().equals(user.getUserId())){
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}


	/**
	 * Convert invoices to DTO for on screen tables
	 * @param invs
	 * @return
	 */	
	private List<InvoiceListDTO> invoicesToDTO(List<Invoice> invs) {
		List<InvoiceListDTO> ret = new ArrayList<InvoiceListDTO>();
		if(invs != null){
			for(Invoice inv : invs){
				String applicantName = "";
				if(inv.getApplicant() != null)
					applicantName = inv.getApplicant().getAppName();

				ret.add(new InvoiceListDTO(inv.getId(),inv.getUpdatedDate(), inv.getState(), applicantName));
			}
		}
		return ret;
	}

}
