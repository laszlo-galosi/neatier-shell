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

package com.neatier.shell.exception;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import com.neatier.shell.R;
import retrofit.RetrofitError;
import trikita.log.Log;

/**
 * Factory used to createApplication error messages from an Exception as a condition.
 */
public class ErrorMessageFactory {

    public static final int SHOW_AS_SNACK = 0;
    public static final int SHOW_AS_ALERT = 1;
    public static final int ERROR_UNAUTHORIZED_REQUEST = 401;
    public static final int ERROR_INTERNAL = 500;

    public static ErrorMessageFactory getInstance() {
        return SInstanceHolder.sInstance;
    }

    private ErrorMessageFactory() {
        //empty
    }

    /**
     * Creates a String representing an error message.
     *
     * @param throwable An exception used as a condition to retrieve the correct error message.
     * @return {@link String} an error message.
     */
    public static Error create(Throwable throwable) {
        Log.e("Exception occurred:", throwable);
        //exception.printStackTrace();
        if (throwable instanceof NetworkConnectionException) {
            return new Error(SHOW_AS_SNACK,
                             R.string.exception_message_no_connection).setDisableCloud(true);
        } else if (throwable instanceof RestApiResponseException) {
            return getByRestApiResponse((RestApiResponseException) throwable);
        } else {
            return new Error(SHOW_AS_SNACK, R.string.exception_message_generic);
        }
    }

    private static Error getByRestApiResponse(final RestApiResponseException exception) {
        RestApiResponseException.ErrorResponse errorResponse =
              exception.getErrorResponse();
        Log.d("Api response error ", errorResponse);
        if (errorResponse == null) {
            return new Error(SHOW_AS_SNACK, R.string.exception_message_generic);
        }
        if (RetrofitError.Kind.NETWORK.name().equals(errorResponse.error)
              || exception.getStatusCode() == ERROR_INTERNAL) {
            return new Error(SHOW_AS_ALERT, R.string.exception_message_server_error)
                  .setDisableCloud(true);
        } else {
            return new Error(SHOW_AS_SNACK, R.string.exception_message_generic);
        }
    }

    private static class SInstanceHolder {
        private static final ErrorMessageFactory sInstance = new ErrorMessageFactory();
    }

    public static class Error implements Parcelable {
        private int showAs;
        private @StringRes int messageRes;
        private boolean disableCloud = false;
        private boolean shouldKickUserOut = false;

        public Error(final int showAs, @StringRes final int messageRes) {
            this.showAs = showAs;
            this.messageRes = messageRes;
        }

        protected Error(Parcel in) {
            showAs = in.readInt();
            messageRes = in.readInt();
            disableCloud = in.readByte() != 0;
            shouldKickUserOut = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(showAs);
            dest.writeInt(messageRes);
            dest.writeByte((byte) (disableCloud ? 1 : 0));
            dest.writeByte((byte) (shouldKickUserOut ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Error> CREATOR = new Creator<Error>() {
            @Override
            public Error createFromParcel(Parcel in) {
                return new Error(in);
            }

            @Override
            public Error[] newArray(int size) {
                return new Error[size];
            }
        };

        public boolean shouldDisableCloudAccess() {
            return disableCloud;
        }

        public boolean shouldKickUserOut() {
            return shouldKickUserOut;
        }

        public boolean showAsAlert() {
            return showAs == SHOW_AS_ALERT;
        }

        public int getShowAs() {
            return showAs;
        }

        @StringRes public int getMessageRes() {
            return messageRes;
        }

        public Error setDisableCloud(final boolean disableCloud) {
            this.disableCloud = disableCloud;
            return this;
        }

        public Error setShouldKickUserOut(final boolean shouldKickUserOut) {
            this.shouldKickUserOut = shouldKickUserOut;
            return this;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("Error{");
            sb.append("showAs=").append(showAs);
            sb.append(", messageRes=").append(messageRes);
            sb.append(", disableCloud=").append(disableCloud);
            sb.append(", shouldKickUserOut=").append(shouldKickUserOut);
            sb.append('}');
            return sb.toString();
        }
    }
}
