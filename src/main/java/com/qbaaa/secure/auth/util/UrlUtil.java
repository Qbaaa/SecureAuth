package com.qbaaa.secure.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class UrlUtil {

    public static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        return scheme + "://" + host + (port == 80 || port == 443 ? "" : ":" + port);
    }

    public static String extractDomain(HttpServletRequest request) {
        Pattern pattern = Pattern.compile("^(http[s]?://[^/]+/domains/[^/]+)");
        Matcher matcher = pattern.matcher(request.getRequestURL());
        return matcher.find() ? matcher.group(1) : null;
    }

}
