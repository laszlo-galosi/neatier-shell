/*
 * Copyright (C) 2017 Extremenet Ltd., All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *  Proprietary and confidential.
 *  All information contained herein is, and remains the property of Extremenet Ltd.
 *  The intellectual and technical concepts contained herein are proprietary to Extremenet Ltd.
 *   and may be covered by U.S. and Foreign Patents, pending patents, and are protected
 *  by trade secret or copyright law. Dissemination of this information or reproduction of
 *  this material is strictly forbidden unless prior written permission is obtained from
 *   Extremenet Ltd.
 *
 */

package com.neatier.shell.appframework.helpers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import com.neatier.shell.eventbus.EventBuilder;

/**
 * Created by LargerLife on 02/02/17.
 */

public interface PermissionInteraction {

    String EXTRA_RESULT_CODE = "ExtraResultCode";

    /**
     * Handles the specified permission request event of which event type is
     * {@link Event#EVT_REQUIRE_PERMISSION} and has {@link EventParam#PRM_REQUEST_CODE}
     */
    void handlePermissionEvent(EventBuilder event);

    /**
     * Request the specified permission from the user for the specified intent to start
     * with the specified request code. When its granted sends the specified intent
     * via {@link LocalBroadcastManager}
     *
     * @param intentToStart the intent to be performed after permission granted.
     * @param requestCodeForResult the request code for the permission request result.
     * @param permissions the permissions which is required, if none specified, the request code
     * will determine the permissions needed.
     */
    void grantPermissionForIntent(final Intent intentToStart, int requestCodeForResult,
          String... permissions);

    /**
     * Delegate method for {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     *
     * @See {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     */
    void onRequestPermissionsResult(final int requestCode,
          @NonNull final String[] permissions, @NonNull final int[] grantResults);

    /**
     * @return true if the user rejected or not asked yet for the specified permission and only if
     * the rejection count is less than {@link AppSettings#MAX_PERMISSION_DENIED_LIMIT}
     */
    boolean shouldRequestPermission(String... permission);

    void sendIntentForPermissionRequest(int requestCode, int grantResult, Intent data);
}
