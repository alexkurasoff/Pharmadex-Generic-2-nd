package org.msh.pharmadex.mbean.amendment;

import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.amendment.Amendment;

/**
 * Something in common for all amendment types 
 * @author Alex Kurasoff
 *
 */
public interface IAmendment {

	/**
	 * Get general amendment
	 * @return
	 */
	Amendment getAmendment();
	
	void setAmendment(Amendment amd);
	
	Amendment findAmendment(Long id);
	
	/**
	 * Clean up this amendment
	 */
	void cleanUp();
	
	/**
	 * Validate selected Applicant 
	 * @return error code or empty string
	 */
	String validateApplicant();
	
	/**
	 * Validate selected Product
	 * @return error code or empty string
	 */
	String validateProduct();
	
	/**
	 * Validate amendment details
	 * @return error code or empty string
	 */
	String validateDetail();
	
	/**
	 * Save an amendment
	 * @return error code or ""
	 */
	String save(String step);
	
	/**
	 * Submit this amendment, means pass to a reviewer
	 * @return error code or ""
	 */
	String submit();
	
	/**
	 * Reviewer is disagreed
	 * @return error code or ""
	 */
	String reject();
	
	/**
	 * Registrar is confirmed it!
	 * if implement from ProdApplication form
	 * used by AmendmentProcess = NEW_APPLICATION
	 * @return error code or ""
	 */
	String implement(ProdApplications currentProdApp);

}
