package org.msh.pharmadex.service;

import org.msh.pharmadex.dao.PcCodeDAO;
import org.msh.pharmadex.dao.iface.AtcDAO;
import org.msh.pharmadex.domain.Atc;
import org.msh.pharmadex.domain.PcCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * Author: usrivastava
 */
@Service
public class PcCodeService implements Serializable {


    private static final long serialVersionUID = -1704390569748290869L;
    @Autowired
    private PcCodeDAO pcCodeDAO;

    private List<PcCode> pcCodeList;

    @Transactional
    public List<PcCode> getPcCodeList() {
        pcCodeList = (List<PcCode>) pcCodeDAO.findAll();
        return pcCodeList;
    }

    public PcCode findPcCodeById(String id) {
        return pcCodeDAO.findByPcCode(id);
    }


    public List<PcCode> findPcCodeByName(String innname) {
        return pcCodeDAO.findByPcName(innname);
    }

}
