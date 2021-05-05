package org.msh.pharmadex.domain.amendment;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.msh.pharmadex.domain.AdminRoute;
/**
 * Specific Amendment to change product ADMIN ROUTE
 * @author Alex Kurasoff
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy=false)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("AmdPRODUCT_ADMINROUTE")
public class AmdPRODUCT_ADMINROUTE extends Amendment {

	private static final long serialVersionUID = 4768861459236720904L;

	@OneToOne(targetEntity=AdminRoute.class, fetch=FetchType.LAZY, cascade={CascadeType.ALL})
	@JoinColumn(name="`AdminRouteID`", nullable = true)
    private AdminRoute adminRoute;
	
	public AdminRoute getAdminRoute() {
		return adminRoute;
	}

	public void setAdminRoute(AdminRoute adminRoute) {
		this.adminRoute = adminRoute;
	}

	public String toString() {
		return super.toString();
	}
}
