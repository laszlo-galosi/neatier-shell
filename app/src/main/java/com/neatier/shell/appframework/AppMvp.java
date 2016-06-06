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
import com.neatier.commons.helpers.KeyValuePairs;
import com.neatier.shell.eventbus.EventBuilder;
import com.neatier.shell.exception.ErrorMessageFactory;
import rx.Subscriber;

/**
 * Created by László Gálosi on 18/05/16
 */
public interface AppMvp {

    /**
     * Interface representing a Presenter in a model view presenter (MVP) pattern.
     */
    interface Presenter {
        /**
         * Method that control the lifecycle of the view. It should be called in the view's
         * (Activity or Fragment) onResume() method.
         */
        void resume();

        /**
         * Method that control the lifecycle of the view. It should be called in the view's
         * (Activity or Fragment) onPause() method.
         */
        void pause();

        /**
         * Method that control the lifecycle of the view. It should be called in the view's
         * (Activity or Fragment) onDestroy() method.
         */
        void destroy();

        void setView(LongTaskBaseView view);

        void initialize();

        Subscriber<EventBuilder> getEventSubscriber();
    }

    interface View {

    }

    /**
     * Base view controller interface for long running operations.
     * Created by László Gálosi on 08/08/15
     */
    interface LongTaskBaseView extends View {

        String ARG_NIGHT_MODE = "NightModeActive";

        void showProgress();

        void hideProgress();

        void showError(ErrorMessageFactory.Error errorMessage);

        void onUpdateFinished(Throwable... errors);

        void onUpdateStarted();

        void onModelReady();

        boolean isResultFromCloud();

        Context getContext();

        KeyValuePairs<String, Object> getApiParams();
    }
}
