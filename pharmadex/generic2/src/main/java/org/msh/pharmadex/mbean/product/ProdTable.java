package org.msh.pharmadex.mbean.product;

import org.msh.pharmadex.domain.enums.ProdCategory;
import org.msh.pharmadex.domain.enums.RegState;
import org.msh.pharmadex.domain.enums.YesNoNA;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by utkarsh on 4/6/15.
 */
public class ProdTable implements Serializable {

	private static final long serialVersionUID = -7555262529794742103L;
	
	private Long id;
    private Long prodAppID;
    private String appName;
    private String prodName;
    private String innNames;
    private String dosForm;
    private String dosStrength;
    private String regNo;
    private Date regDate;
    private String shcedul;
   
    private String genName;
    private ProdCategory prodCategory;
    private Date regExpiryDate;
    private String manufName;
    private String prodDesc;
    private YesNoNA fnm;
    private RegState regState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getGenName() {
        return genName;
    }

    public void setGenName(String genName) {
        this.genName = genName;
    }

    public ProdCategory getProdCategory() {
        return prodCategory;
    }

    public void setProdCategory(ProdCategory prodCategory) {
        this.prodCategory = prodCategory;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Date getRegDate() {
        return regDate;
    }

    public void setRegDate(Date regDate) {
        this.regDate = regDate;
    }

    public Date getRegExpiryDate() {
        return regExpiryDate;
    }

    public void setRegExpiryDate(Date regExpiryDate) {
        this.regExpiryDate = regExpiryDate;
    }

    public String getManufName() {
        return manufName;
    }

    public void setManufName(String manufName) {
        this.manufName = manufName;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getProdDesc() {
        return prodDesc;
    }

    public void setProdDesc(String prodDesc) {
        this.prodDesc = prodDesc;
    }

    public Long getProdAppID() {
        return prodAppID;
    }

    public void setProdAppID(Long prodAppID) {
        this.prodAppID = prodAppID;
    }

	public YesNoNA getFnm() {
		return fnm;
	}

	public void setFnm(YesNoNA fnm) {
		this.fnm = fnm;
	}

	public RegState getRegState() {
		return regState;
	}

	public void setRegState(RegState regState) {
		this.regState = regState;
	}

	public String getInnNames() {
		return innNames;
	}

	public void setInnNames(String innNames) {
		this.innNames = innNames;
	}

	public String getDosForm() {
		return dosForm;
	}

	public void setDosForm(String dosForm) {
		this.dosForm = dosForm;
	}

	public String getDosStrength() {
		return dosStrength;
	}

	public void setDosStrength(String dosStrength) {
		this.dosStrength = dosStrength;
	}

	public String getShcedul() {
		return shcedul;
	}

	public void setShcedul(String shcedul) {
		this.shcedul = shcedul;
	}
    
	
}
