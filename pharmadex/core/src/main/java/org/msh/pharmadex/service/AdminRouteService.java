package org.msh.pharmadex.service;

import java.io.Serializable;
import java.util.List;

import org.msh.pharmadex.dao.iface.AdminRouteDAO;
import org.msh.pharmadex.domain.AdminRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Author: usrivastava
 */
@Service
public class AdminRouteService implements Serializable {


	@Autowired
	private AdminRouteDAO adminRouteDAO;

	public List<AdminRoute> getAdminRoutes() {
		return adminRouteDAO.findAll(new Sort(new Sort.Order(Sort.Direction.ASC, "name")));
	}

	public AdminRoute findOneByName(String name){
		List<AdminRoute> found = adminRouteDAO.findByName(name);
		if (found != null && found.size() > 0)
			return found.get(0);
		return null;
	}

	public AdminRouteDAO getAdminRouteDAO() {
		return adminRouteDAO;
	}

	public void setAdminRouteDAO(AdminRouteDAO adminRouteDAO) {
		this.adminRouteDAO = adminRouteDAO;
	}


}
