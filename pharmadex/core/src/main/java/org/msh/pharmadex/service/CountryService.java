package org.msh.pharmadex.service;

import org.msh.pharmadex.dao.CountryDAO;
import org.msh.pharmadex.domain.Country;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * Author: usrivastava
 */
@Service
public class CountryService implements Serializable {

    private static final long serialVersionUID = 4520038222129025384L;
    @Autowired
    CountryDAO countryDAO;

    private List<Country> countries;

    public List<Country> getCountries() {
            countries = countryDAO.allCountry();
        return countries;
    }

    public Country findCountryById(long id) {
        return countryDAO.find(id);
    }
}
