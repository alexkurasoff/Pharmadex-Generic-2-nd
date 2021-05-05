package org.msh.pharmadex.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.msh.pharmadex.domain.amendment.Amendment;
import org.msh.pharmadex.domain.enums.CompanyType;

/**
 * Created by IntelliJ IDEA.
 * User: usrivastava
 * Date: 1/11/12
 * Time: 11:22 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "prod_company")
public class ProdCompany extends CreationDetail implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    //@ManyToOne (fetch = FetchType.LAZY)
    @ManyToOne (cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name="prod_id", nullable=true)
    private Product product;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="company_id", nullable = true)
    private Company company;

    @Enumerated(EnumType.STRING)
    private CompanyType companyType;
    
   /* @ManyToOne(targetEntity=Amendment.class, fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name = "amd_id", nullable=true)
    private Amendment amendment;*/
    
    @ManyToMany(targetEntity = Amendment.class, fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinTable(name = "amendment_prodCompany", joinColumns = @JoinColumn(name = "prodComp_id"), inverseJoinColumns = @JoinColumn(name = "amd_id"))
    private List<Amendment> amendment;


    public ProdCompany() {
    }

    public ProdCompany(Product product, Company company, CompanyType companyType) {
        this.product = product;
        this.company = company;
        this.companyType = companyType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }

	public List<Amendment> getAmendment() {
		return amendment;
	}

	public void setAmendment(List<Amendment> amendment) {
		this.amendment = amendment;
	}
    
}
