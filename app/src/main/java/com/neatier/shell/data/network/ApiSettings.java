/*
 *  Copyright (C) 2016 Delight Solutions Ltd., All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 *
 *  All information contained herein is, and remains the property of Delight Solutions Kft.
 *  The intellectual and technical concepts contained herein are proprietary to Delight Solutions
  *  Kft.
 *   and may be covered by U.S. and Foreign Patents, pending patents, and are protected
 *  by trade secret or copyright law. Dissemination of this information or reproduction of
 *  this material is strictly forbidden unless prior written permission is obtained from
 *   Delight Solutions Kft.
 */

package com.neatier.shell.data.network;

import android.support.annotation.Nullable;
import com.google.common.collect.Lists;
import com.neatier.commons.helpers.KeyValuePairs;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by László Gálosi on 08/06/16
 */
public class ApiSettings {
    public static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;

    public static final String PACKAGE_NAME = "com.neatier.shell";
    //Todo set the default endpoint to the server.
    public static final String DEFAULT_SERVER_ENDPOINT = "https://xboxapi.com";
    public static final long READ_TIMEOUT = 30;
    public static final long CONNECTION_TIMEOUT = 50;
    public static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
    public static final String PROP_ERROR_CODE = "error_code";
    public static final String PROP_ERROR_MESSAGE = "error_message";
    public static final String KEY_API_LIST_TYPE = "GAME_TYPE";
    public static final String KEY_API_XUID = "XUID";
    public static final String KEY_API_XAUTH = "X-AUTH";
    public static final String KEY_API_LANG = "Accept-Language";
    public static final String LIST_XBOXONE = "xboxonegames";
    public static final String LIST_XBOX360 = "xbox360games";
    public static final String LIST_LATEST_XBOXONE = "latest-xboxone-games";
    public static final String LIST_LATEST_XBOX360 = "latest-xbox360-games";
    public static final String ACTION_LIST_GAMES = "ListGames";
    public static final List<String> GAMES_PARAMS = Lists.newArrayList("sort");

    public static final String API_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ";
    public static final String KEY_API_ACTION = "ApiAction";

    //Api related constants.

    private ApiSettings() {
        // This class cannot be instantiated
    }

    /**
     * Sets or create a default request parameter set which is common for all request.
     *
     * @param keyValuePairs if non null, default parameters will be put to this map.
     */
    public static KeyValuePairs<String, Object> setOrCreateDefaultApiParams(
          @Nullable KeyValuePairs<String, Object> keyValuePairs) {
        if (keyValuePairs == null) {
            keyValuePairs = new KeyValuePairs<>(6);
        }
        //Todo: set the common request parameters
        //keyValuePairs.put(ApiSettings.KEY_API_PARAM, "default value");
        return keyValuePairs;
    }
}
