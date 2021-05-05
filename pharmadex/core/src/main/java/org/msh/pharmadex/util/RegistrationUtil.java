package org.msh.pharmadex.util;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by usrivastava on 06/16/2014.
 */
public class RegistrationUtil {

    public static String formatString(String dosForm) {
    	if(dosForm==null){
    		return "";
    	}
        String value = dosForm.trim();
        value = value.toUpperCase();
        value = value.replaceAll(", ", "_");
        value = value.replaceAll(" / ", "_");
        value = value.replaceAll("/ ", "_");
        value = value.replaceAll("/", "_");
        value = value.replaceAll("\\(", "_");
        value = value.replaceAll("\\)", "_");
        value = value.replaceAll("'", "_");
        value = value.replaceAll("\\.", "_");
        value = value.replaceAll("\\s", "_");
        return value;
    }

    public static String generateRegNo(String count, String appType) {
        String prodAppNo = count + "/" + appType;
        return prodAppNo;
    }

    public String generateAppNo(Long prodAppID) {
        String prodAppNo = "";
        Calendar calendar = Calendar.getInstance(Locale.US);
        String year = "" + calendar.get(Calendar.YEAR);
        int m = (calendar.get(Calendar.MONTH) + 1);
        String month = String.format("%02d", m);
        String no = String.format("%04d", prodAppID);

        prodAppNo = no + "/" + month + "/" + year;
        return prodAppNo;
    }

    public String generateAppNo(Long count, String appType) {
        Calendar calendar = Calendar.getInstance();
        String year = "" + calendar.get(Calendar.YEAR);
        String no = String.format("%04d", count);


        String prodAppNo = no + "/" + appType + "/" + year;
        return prodAppNo;
    }
}
