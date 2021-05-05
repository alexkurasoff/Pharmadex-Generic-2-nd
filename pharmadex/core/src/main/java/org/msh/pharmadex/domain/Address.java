package org.msh.pharmadex.domain;


import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author usrivastava
 */
@Embeddable
public class Address implements Serializable{
    private static final long serialVersionUID = 5188310112489422198L;
    @Column(length = 100)
   	private String address1;

   	@Column(length = 100)
   	private String address2;

   	@Column(length = 32)
   	private String zipcode;
   	
   	@Column(length = 32)
   	private String zipaddress;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CNTRY_ID")
    @NotAudited
    private Country country;

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }
    
    public String getZipaddress() {
		return zipaddress;
	}

	public void setZipaddress(String zipaddress) {
		this.zipaddress = zipaddress;
	}
	
    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address1 == null) ? 0 : address1.hashCode());
		result = prime * result + ((address2 == null) ? 0 : address2.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((zipcode == null) ? 0 : zipcode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (address1 == null) {
			if (other.address1 != null)
				return false;
		} else if (!address1.equals(other.address1))
			return false;
		if (address2 == null) {
			if (other.address2 != null)
				return false;
		} else if (!address2.equals(other.address2))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.getId().equals(other.country.getId()))
			return false;
		if (zipcode == null) {
			if (other.zipcode != null)
				return false;
		} else if (!zipcode.equals(other.zipcode))
			return false;
		return true;
	}

	
    
}

