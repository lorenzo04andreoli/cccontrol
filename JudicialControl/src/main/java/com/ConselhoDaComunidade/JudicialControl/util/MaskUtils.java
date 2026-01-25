package com.ConselhoDaComunidade.JudicialControl.util;

public class MaskUtils {

    public static String cpf(String cpf) {
        if (cpf == null) return "";
        String d = cpf.replaceAll("\\D", "");
        if (d.length() != 11) return cpf; // fallback
        return d.substring(0,3) + "." + d.substring(3,6) + "." + d.substring(6,9) + "-" + d.substring(9);
    }
}