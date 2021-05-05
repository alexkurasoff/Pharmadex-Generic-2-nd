package org.msh.pharmadex.service.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.msh.pharmadex.domain.AdminRoute;
import org.msh.pharmadex.service.AdminRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@FacesConverter(value = "adminRouteConverter")
@Component
@Scope("singleton")
public class AdminRouteConverter implements Converter, Serializable {
	private static final long serialVersionUID = -2229890484871176190L;
	
	@Autowired
    private AdminRouteService adminRouteService;

    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
    	AdminRoute item = null;
    	if (submittedValue.trim().equals("")) {
            return item;
        } else {
        	try {
                int number = Integer.parseInt(submittedValue);
                item = adminRouteService.getAdminRouteDAO().findOne(number);
            } catch (NumberFormatException exception) {
            	item = adminRouteService.findOneByName(submittedValue);
            }
        }

        return item;
    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
    	if (value == null || "".equals(value) || ("null".equals(value))) {
            return null;
        } else {
        	if (value instanceof Long) {
                return null;
            }else if (value instanceof String) {
                return String.valueOf(value);
            }else if (value instanceof AdminRoute){
            	AdminRoute ar = (AdminRoute) value;
                return ar.getName();
            }
        }
        return null;
    }
}