package org.msh.pharmadex.service.converter;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.msh.pharmadex.domain.Product;
import org.msh.pharmadex.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@FacesConverter(value = "prodConverter")
@Component
public class ProdConverter implements Converter, Serializable {

	private static final long serialVersionUID = -4589803987254612453L;

	@Autowired
    private ProductService productService;

    public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {
    	Product product = null;
    	if (submittedValue.trim().equals("")) {
            return product;
        } else {
            	try {
                    int number = Integer.parseInt(submittedValue);
                    product = productService.findProduct(new Long(number));
                } catch (NumberFormatException exception) {
                	product = productService.findOneByName(submittedValue);
                }
        }

        return product;

    }

    public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
        if (value == null || "".equals(value) || ("null".equals(value))) {
            return null;
        } else {
        	if (value instanceof Long) {
        		 return String.valueOf(value);
            }else if (value instanceof String) {
                return String.valueOf(value);
            }else if (value instanceof Product){
            	Product pr = (Product) value;
                if (pr.getId() != null)
                    return String.valueOf(pr.getId());
                else
                    return pr.getProdName();
            }
        }
        return null;
    }
}
