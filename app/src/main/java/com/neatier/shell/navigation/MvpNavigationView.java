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

package com.neatier.shell.navigation;

import android.view.MenuItem;
import com.neatier.shell.appframework.AppMvp;
import java.util.List;

/**
 * Created by László Gálosi on 18/05/16
 */
public interface MvpNavigationView extends AppMvp.LongTaskBaseView {
    List<MenuItem> getMenuItems();

    int getMenuResource();

    boolean shouldDefaultSelect(final MenuItem menuItem);
}
