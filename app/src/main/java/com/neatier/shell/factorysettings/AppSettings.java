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
import com.neatier.shell.eventbus.RxBus;
import com.neatier.shell.exception.ErrorMessageFactory;

/**
 * Created by László Gálosi on 22/04/16
 */
public class AppSettings {

    public static final String PACKAGE_NAME = "com.neatier.shell";

    //Permissions and intent result request codes.
    public static final int PERM_REQUEST_CODE_CALL = 1 << 0;
    public static final int PERM_REQUEST_CODE_LOCATION = 1 << 1;
    public static final int PERM_REQUEST_CODE_READ_EXT_STORAGE = 1 << 2;
    public static final int PERM_REQUEST_CODE_IMAGE_CAPTURE = 1 << 3;
    public static final int PERM_REQUEST_CODE_VIDEO_CAPTURE = 1 << 4;
    public static final int PERM_REQUEST_CODE_IMAGE_CHOOSER = 1 << 5;
    public static final int REQUEST_CODE_CHECK_PLAY_SERVICES = 1 << 6;
    public static final int REQUEST_CODE_SIGN_IN_REQUIRED = 1 << 7;

    public static final String PREFKEY_PERMISION_DENIED_COUNT = "MaxPermissionDenied";
    public static final int MAX_PERMISSION_DENIED_LIMIT = 2;

    public static final String ACTION_REQUIRE_PERMISSION = "action_require_permission";
    public static final String ACTION_START_RESOLUTION = "action_start_resolution_for_result";
    public static final String ACTION_CAMERA_CAPTURE = PACKAGE_NAME + ".CameraCapture";
    public static final String ACTION_CAMERA_PREVIEW_RESULT = "CameraPreviewResult";

    public static final String PREF_DEFAULT_STORAGE_FILE = PACKAGE_NAME + "NeatierShell";
    public static final String PREF_DEV_SETTINGS_FILE = PACKAGE_NAME + "NeatierShell.DevSettings";
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1978;
    public static final long BACKPRESSURE_CAPACITY = 100000;

    public static final String IMAGE_PROVIDER_NAME =
          String.format("%s.fileprovider", AppSettings.PACKAGE_NAME);


    /**
     * Initialize constants for different enum-like classes.
     */
    public static void init(final Context context) {
        ErrorMessageFactory.ApiError.init(context);
        //Initialize RxBus event constants.
        RxBus.initConstants(context);
        PrefKey.init(context);
    }
}
