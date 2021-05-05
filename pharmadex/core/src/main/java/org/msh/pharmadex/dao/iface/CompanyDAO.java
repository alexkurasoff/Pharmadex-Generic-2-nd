package org.msh.pharmadex.dao.iface;

import java.util.List;

import org.msh.pharmadex.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Author: usrivastava
 */
public interface CompanyDAO extends JpaRepository<Company, Long> {
	
	public List<Company> findByCompanyName(String companyName);
}

