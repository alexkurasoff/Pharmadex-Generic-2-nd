package org.msh.pharmadex.mbean.product;

import org.msh.pharmadex.domain.PcCode;
import org.msh.pharmadex.service.GlobalEntityLists;


public class RegPCCODEHelper {

    private PcCode pc_code;
    private GlobalEntityLists globalEntityLists;

    public RegPCCODEHelper(PcCode atc, GlobalEntityLists globalEntityLists) {
        this.pc_code = atc;
        this.globalEntityLists = globalEntityLists;
    }


}
