package com.mqunar.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class StringUtils {
    public static final String ACTIVITY_METRIC_PREFIX = "Mobile/Activity/Name/";
    public static final String ACTIVITY_BACKGROUND_METRIC_PREFIX = "Mobile/Activity/Background/Name/";
    public static final String ACTIVITY_DISPLAY_NAME_PREFIX = "Display ";

    private static final Random random = new Random();

    public StringUtils() {}

    public static String slurp(InputStream stream) throws IOException {
        char[] buf = new char[8192];
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        while(true) {
            int n = reader.read(buf);
            if(n < 0) {
                return sb.toString();
            }

            sb.append(buf, 0, n);
        }
    }

    public static String sanitizeUrl(String urlString) {
        if(urlString == null) {
            return null;
        } else {
//            URL url;
//            try {
//                url = new URL(urlString);
//            } catch (MalformedURLException var3) {
//                return null;
//            }
//
//            StringBuffer sanitizedUrl = new StringBuffer();
//            sanitizedUrl.append(url.getProtocol());
//            sanitizedUrl.append("://");
//            sanitizedUrl.append(url.getHost());
//            if(url.getPort() != -1) {
//                sanitizedUrl.append(":");
//                sanitizedUrl.append(url.getPort());
//            }
//
//            sanitizedUrl.append(url.getPath());
//            return sanitizedUrl.toString();
            // do not substring url, use full url
            return urlString;
        }
    }

    public static Random getRandom() {
        return random;
    }

    public static String formatActivityMetricName(String name) {
        return ACTIVITY_METRIC_PREFIX + name;
    }

    public static String formatActivityBackgroundMetricName(String name) {
        return ACTIVITY_BACKGROUND_METRIC_PREFIX + name;
    }

    public static String formatActivityDisplayName(String name) {
        return ACTIVITY_DISPLAY_NAME_PREFIX + name;
    }

    public static String null2String(String str) {
        return str == null ? "" : str;
    }
}
