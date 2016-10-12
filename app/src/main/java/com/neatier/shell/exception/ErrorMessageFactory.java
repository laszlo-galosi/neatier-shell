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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.util.SparseArray;
import com.fernandocejas.arrow.optional.Optional;
import com.neatier.commons.exception.NetworkConnectionException;
import com.neatier.commons.exception.RestApiResponseException;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.shell.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import trikita.log.Log;

/**
 * Factory used to createApplication error messages from an Exception as a condition.
 */
public class ErrorMessageFactory {

    public static final int SHOW_AS_SNACK = 0;
    public static final int SHOW_AS_ALERT = 1;
    public static final int ERROR_UNAUTHORIZED_REQUEST = 401;
    public static final int ERROR_INTERNAL = 500;
    private JsonSerializer mJsonSerializer;

    public static ErrorMessageFactory getInstance() {
        return SInstanceHolder.sInstance;
    }

    private ErrorMessageFactory() {
        //empty
        mJsonSerializer = new JsonSerializer<>();
    }

    /**
     * Creates a String representing an error name.
     *
     * @param throwable An exception used as a condition to retrieve the correct error name.
     * @return {@link String} an error name.
     */
    public static Error create(Throwable throwable) {
        //Log.e("Exception occurred:", throwable);
        //exception.printStackTrace();
        if (throwable instanceof com.neatier.commons.exception.NetworkConnectionException) {
            return new Error(SHOW_AS_SNACK,
                             R.string.exception_message_no_connection).setDisableCloud(true);
        } else if (throwable instanceof com.neatier.commons.exception.RestApiResponseException) {
            return getByRestApiResponse(
                  (com.neatier.commons.exception.RestApiResponseException) throwable);
        } else {
            return new Error(SHOW_AS_SNACK, R.string.exception_message_generic);
        }
    }

    private static Error getByRestApiResponse(
          final com.neatier.commons.exception.RestApiResponseException exception) {
        com.neatier.commons.exception.RestApiResponseException.ErrorResponse errorResponse =
              exception.getErrorResponse();
        Log.d("Api response error ", errorResponse);
        RestApiResponseException.ErrorKind kind = exception.getKind();
        switch (kind.id) {
            case RestApiResponseException.ErrorKind.NETWORK:
            case RestApiResponseException.ErrorKind.SERVER:
                if (exception.getCause() instanceof NetworkConnectionException) {
                    return new Error(SHOW_AS_SNACK,
                                     R.string.exception_message_no_connection)
                          .setDisableCloud(true);
                } else {
                    return new Error(SHOW_AS_SNACK, R.string.exception_message_server_error)
                          .setRemoteAddress(exception.getRemoteAddress())
                          .setDisableCloud(true);
                }
            case RestApiResponseException.ErrorKind.AUTHENTICATION:
                return new Error(SHOW_AS_SNACK, R.string.exception_message_authentication)
                      .setDisableCloud(true);
            case RestApiResponseException.ErrorKind.REQUEST:
                Optional<ApiError> apiError = ApiError.find(exception.getReason());
                if (apiError.isPresent()) {
                    return apiError.get().getError();
                }
                return new Error(SHOW_AS_SNACK, R.string.exception_message_generic);
            default:
                return new Error(SHOW_AS_SNACK, R.string.exception_message_generic);
        }
    }

    private static class SInstanceHolder {
        private static final ErrorMessageFactory sInstance = new ErrorMessageFactory();
    }

    public static class Error implements Parcelable {
        private int showAs;
        private @StringRes int messageRes;
        private String rawMessage;
        private boolean disableCloud = false;
        private boolean shouldKickUserOut = false;
        private String remoteAddress;

        public Error(final int showAs, @StringRes final int messageRes) {
            this.showAs = showAs;
            this.messageRes = messageRes;
        }

        public Error(final int showAs, final String message) {
            this.showAs = showAs;
            this.rawMessage = message;
        }

        protected Error(Parcel in) {
            showAs = in.readInt();
            messageRes = in.readInt();
            rawMessage = in.readString();
            disableCloud = in.readByte() != 0;
            shouldKickUserOut = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(showAs);
            dest.writeInt(messageRes);
            dest.writeString(rawMessage);
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

        public String getRawMessage() {
            return rawMessage;
        }

        public Error setDisableCloud(final boolean disableCloud) {
            this.disableCloud = disableCloud;
            return this;
        }

        public Error setShouldKickUserOut(final boolean shouldKickUserOut) {
            this.shouldKickUserOut = shouldKickUserOut;
            return this;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }

        public Error setRemoteAddress(final String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("Error{");
            sb.append("showAs=").append(showAs);
            sb.append(", displayableMsgRes=").append(messageRes);
            sb.append(", rawMessage=").append(rawMessage);
            sb.append(", disableCloud=").append(disableCloud);
            sb.append(", shouldKickUserOut=").append(shouldKickUserOut);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class ApiError {
        private static SparseArray<ApiError> values;
        static TypedArray sPrefKeys;
        public final int id;
        public final String name;

        public static void init(final Context context) {
            sPrefKeys = context.getResources().obtainTypedArray(R.array.error_msgs);
            int len = sPrefKeys.length();
            values = new SparseArray<>(len);
            for (int i = 0; i < len; i++) {
                int id = sPrefKeys.getResourceId(i, -1);
                values.put(id, new ApiError(id, sPrefKeys.getString(i)));
            }
        }

        public static Optional<ApiError> find(int id) {
            return Optional.fromNullable(values.get(id));
        }

        public static Optional<ApiError> find(String name) {
            for (int i = 0, len = values.size(); i < len; i++) {
                @ApiErrorId int keyId = values.keyAt(i);
                if (values.get(keyId).name.equals(name)) {
                    return find(keyId);
                }
            }
            return Optional.absent();
        }

        public ApiError(final int id, final String message) {
            this.id = id;
            this.name = message;
        }

        @Override public String toString() {
            return name;
        }

        public @StringRes int displayableMessageRes() {
            switch (id) {
                default:
                    return R.string.exception_message_generic;
            }
        }

        public Error getError() {
            switch (id) {
                default:
                    return new Error(SHOW_AS_SNACK, displayableMessageRes());
            }
        }

        @IntDef({})
        @Retention(RetentionPolicy.SOURCE)
        /**
         * {@link InDef} annotation for identifying unique app events.
         */ public @interface ApiErrorId {
        }
    }
}
