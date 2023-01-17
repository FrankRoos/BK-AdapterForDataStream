package org.gft.adapters.backend;

import org.apache.streampipes.connect.adapter.sdk.ParameterExtractor;
import org.apache.streampipes.sdk.helpers.Label;
import org.apache.streampipes.sdk.helpers.Labels;

public class HttpUtils {
    private static final String LENGTH = "length";
    private static final String LOWEST_DATE = "lowest_date";
    private static final String HIGHEST_DATE = "highest_date";
    private static final String SENSOR_SIGNAL = "signal";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    public static Label getUsernameLabel() {
        return Labels.withId(USERNAME_KEY);
    }

    public static Label getPasswordLabel() {
        return Labels.withId(PASSWORD_KEY);
    }

    public static Label getLengthLabel() {
        return Labels.withId(LENGTH);
    }

    public static Label getSignalLabel() {
        return Labels.withId(SENSOR_SIGNAL);
    }

    public static Label getLowestLabel() {
        return Labels.withId(LOWEST_DATE);
    }

    public static Label getHighestLabel() {
        return Labels.withId(HIGHEST_DATE);
    }

    public static HttpConfig getConfig(ParameterExtractor extractor) {

        String username = extractor.singleValue(USERNAME_KEY, String.class).trim();
        String password = extractor.secretValue(PASSWORD_KEY);
        String signal_name = extractor.singleValue(SENSOR_SIGNAL, String.class).trim();
        String lowest_date = extractor.singleValue(LOWEST_DATE, String.class).trim();//TODO .strip
        String highest_date = extractor.singleValue(HIGHEST_DATE, String.class).trim();//TODO .strip
        Integer length = extractor.singleValue(LENGTH, Integer.class);

        return new HttpConfig(username, password, signal_name, lowest_date, highest_date, length);
    }

}
