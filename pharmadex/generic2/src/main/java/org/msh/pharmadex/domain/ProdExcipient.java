package org.msh.pharmadex.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.msh.pharmadex.domain.amendment.Amendment;


@Entity
public class ProdExcipient extends CreationDetail implements Serializable {

	private static final long serialVersionUID = 6597026660123168100L;

	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true)
    private Long id;

    @Column(name = "dosage_strength", length = 255)
    private String dosStrength;

    @OneToOne
    @JoinColumn(name = "DOSUNIT_ID")
    private DosUom dosUnit = new DosUom();

    @Column(length = 255, nullable = true)
    private String function;

    @Column(length = 255, nullable = true)
    private String RefStd;

    @OneToOne()
    @JoinColumn(name = "EXPNT_ID")
    private Excipient excipient;

    @ManyToOne (cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_id", nullable = false)
    private Product product;
    
    @ManyToMany(targetEntity = Amendment.class, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinTable(name = "amendment_prodExcipient", joinColumns = @JoinColumn(name = "prodExc_id"), inverseJoinColumns = @JoinColumn(name = "amd_id"))
    private List<Amendment> amendment;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDosStrength() {
        return dosStrength;
    }

    public void setDosStrength(String dosStrength) {
        this.dosStrength = dosStrength;
    }

    public DosUom getDosUnit() {
        return dosUnit;
    }

    public void setDosUnit(DosUom dosUnit) {
        this.dosUnit = dosUnit;
    }

    public String getRefStd() {
        return RefStd;
    }

    public void setRefStd(String refStd) {
        RefStd = refStd;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Excipient getExcipient() {
        return excipient;
    }

    public void setExcipient(Excipient excipient) {
        this.excipient = excipient;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

	public List<Amendment> getAmendment() {
		return amendment;
	}

	public void setAmendment(List<Amendment> amendment) {
		this.amendment = amendment;
	}

}
