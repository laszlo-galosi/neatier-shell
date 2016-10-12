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

package com.neatier.shell.factorysettings;

import android.content.Context;
import com.neatier.shell.exception.ErrorMessageFactory;

/**
 * Created by László Gálosi on 22/04/16
 */
public class AppSettings {

    public static final String PACKAGE_NAME = "com.neatier.shell";

    public static final int PENDING_CALL_REQUEST_CODE = 1 >> 1;
    public static final String PREFKEY_PERMISION_DENIED_COUNT = "MaxPermissionDenied";
    public static final int MAX_PERMISSION_DENIED_LIMIT = 1;

    public static final String PREF_DEFAULT_STORAGE_FILE = PACKAGE_NAME + "NeatierShell";
    public static final String PREF_DEV_SETTINGS_FILE = PACKAGE_NAME + "NeatierShell.DevSettings";
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1978;

    /**
     * Initialize constants for different enum-like classes.
     */
    public static void init(final Context context) {
        ErrorMessageFactory.ApiError.init(context);
    }
}
