package com.qbaaa.secure.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlUtil {

    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        return scheme + "://" + host + (port == 80 || port == 443 ? "" : ":" + port);
    }

}
