package org.msh.pharmadex.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.el.ELContext;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;

/**
 * Author: usrivastava
 */
public class JsfUtils {

	public static String getBrowserName(String userAgent) {

		if (userAgent.contains("MSIE")) {
			return "Internet Explorer";
		}
		if (userAgent.contains("Firefox")) {
			return "Firefox";
		}
		if (userAgent.contains("Chrome")) {
			return "Chrome";
		}
		if (userAgent.contains("Opera")) {
			return "Opera";
		}
		if (userAgent.contains("Safari")) {
			return "Safari";
		}
		return "Unknown";
	}

	public static <E> List<E> completeSuggestions(String query, List<E> x) {
		List<E> suggestions = new ArrayList<E>(x.size());
		if (query == null || query.equalsIgnoreCase(""))
			return x;

		for (E each : x) {
			if (each.toString().toLowerCase().startsWith(query.toLowerCase()))
				suggestions.add(each);
		}
		return suggestions;
	}

	public static Date addDate(Date dt, int year){
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.YEAR, year);
		return c.getTime();
	}

	public static Date addDays(Date dt, int countDay){
		Calendar c = Calendar.getInstance();
		c.setTime(dt);
		c.add(Calendar.DAY_OF_YEAR, countDay);
		return c.getTime();
	}

	/**
	 * get MBean class by name
	 * @param beanName
	 * @return
	 */
	public static Object getManagedBean(final String beanName) {
		FacesContext fc = FacesContext.getCurrentInstance();
		Object bean;
		try {
			ELContext elContext = fc.getELContext();
			bean = elContext.getELResolver().getValue(elContext, null, beanName);
		} catch (RuntimeException e) {
			throw new FacesException(e.getMessage(), e);
		}
		if (bean == null) {
			throw new FacesException("Managed bean with name '" + beanName
					+ "' was not found. Check your faces-config.xml or @ManagedBean annotation.");
		}
		return bean;
	}
}
