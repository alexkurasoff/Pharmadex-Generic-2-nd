package org.msh.pharmadex.dao.iface;

import java.util.List;

import org.msh.pharmadex.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceDAO extends JpaRepository<Invoice, Long> {

	public Invoice findInvoicesById(Long id);

//    public List<Invoice> findByProdApplications_ProdApplicant_UsersAndPaymentStatus(List<User> prodApplications_ProdApplicant_Users,
//                                                                                    PaymentStatus paymentStatus);
		

}

