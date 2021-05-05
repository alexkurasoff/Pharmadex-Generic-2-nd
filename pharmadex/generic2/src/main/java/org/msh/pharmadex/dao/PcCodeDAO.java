package org.msh.pharmadex.dao;

import java.util.List;

import org.msh.pharmadex.domain.PcCode;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PcCodeDAO extends JpaRepository<PcCode, Long> {

    public List<PcCode> findByPcName(String atcName);

    public PcCode findByPcCode(String atcCode);


}

