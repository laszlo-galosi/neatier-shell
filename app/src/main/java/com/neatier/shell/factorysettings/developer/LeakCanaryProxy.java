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

package com.neatier.shell.factorysettings.developer;

import android.support.annotation.NonNull;

/**
 * Tiny interface to hide LeakCanary from main source set.
 * Yep LeakCanary has it's own no-op impl, but
 * this approach is universal and applicable to any libraries you want to
 * use in debug builds but not in release. Also, this interface is tinier than LeakCanary's no-op
 * one.
 */
public interface LeakCanaryProxy {
    void init();

    void watch(@NonNull Object object);
}
