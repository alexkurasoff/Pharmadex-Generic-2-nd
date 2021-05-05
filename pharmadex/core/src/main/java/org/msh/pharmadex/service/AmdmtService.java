package org.msh.pharmadex.service;

import org.msh.pharmadex.dao.AmdmtDAO;
import org.msh.pharmadex.dao.iface.AmdmtCategoryDAO;
import org.msh.pharmadex.dao.iface.ProdAppAmdmtDAO;
import org.msh.pharmadex.domain.AmdmtCategory;
import org.msh.pharmadex.domain.ProdAppAmdmt;
import org.msh.pharmadex.domain.ProdApplications;
import org.msh.pharmadex.domain.enums.AmdmtState;
import org.msh.pharmadex.domain.enums.AmdmtType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * Author: usrivastava
 */
@Component
public class AmdmtService implements Serializable {

    private static final long serialVersionUID = 7705194423798800232L;
    @Autowired
    private AmdmtCategoryDAO amdmtCategoryDAO;

    @Autowired
    private ProdAppAmdmtDAO prodAppAmdmtDAO;

    @Autowired
    private AmdmtDAO amdmtDAO;

    @Autowired
    private UserService userService;

    public List<AmdmtCategory> findAmdmtCategoryByType(AmdmtType amdmtType) {

        return amdmtCategoryDAO.findByAmdmtType(amdmtType);
    }

    public List<AmdmtCategory> findAllAmdmtCategory() {

        return amdmtCategoryDAO.findAll();
    }

    public List<AmdmtCategory> findAmdmtCatByIDs(List selAmdmtS) {
        return amdmtCategoryDAO.findByIdIn(selAmdmtS);
    }

    public String submitAmdmt(List<ProdAppAmdmt> prodAppAmdmts) {
        prodAppAmdmtDAO.save(prodAppAmdmts);
        prodAppAmdmtDAO.flush();
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public List<ProdAppAmdmt> findProdAmdmts(Long id) {
        return prodAppAmdmtDAO.findByProdApplications_IdOrderByIdAsc(id);
    }

    public List<ProdApplications> findAmdmtsRecieved() {
        return amdmtDAO.findByAmdmtState(AmdmtState.NEW_APPLICATION);
    }

    public String saveAmdmt(ProdAppAmdmt prodAppAmdmt, Long loggedINUserID) {
        try {
            prodAppAmdmt.setApprovedBy(userService.findUser(loggedINUserID));
            prodAppAmdmtDAO.saveAndFlush(prodAppAmdmt);
            return "persisted";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }

    }
}
