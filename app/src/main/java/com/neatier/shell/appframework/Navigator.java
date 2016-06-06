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

package com.neatier.shell.appframework;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.neatier.shell.activities.MainActivity;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class used to navigate through the application.
 */
@Singleton
public class Navigator {

    public static Navigator getInstance() {
        return SInstanceHolder.sInstance;
    }

    @Inject protected Navigator() {
        //empty
    }

    /**
     * Goes to the {@link MainActivity}.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToMainActivity(Context context) {
        if (context != null) {
            Intent intentToLaunch = MainActivity.getStarterIntent(context);
            context.startActivity(intentToLaunch);
        }
    }

    public void navigateToUrl(final String url, Context context) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    private static class SInstanceHolder {
        private static final Navigator sInstance = new Navigator();
    }
}
