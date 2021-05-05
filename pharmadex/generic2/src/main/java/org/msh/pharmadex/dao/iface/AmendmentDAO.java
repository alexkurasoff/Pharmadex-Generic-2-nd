package org.msh.pharmadex.dao.iface;

import org.msh.pharmadex.domain.amendment.Amendment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmendmentDAO extends JpaRepository<Amendment, Long>{

}
