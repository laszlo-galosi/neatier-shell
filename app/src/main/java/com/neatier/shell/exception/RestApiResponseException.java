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

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.neatier.commons.helpers.JsonSerializer;
import com.neatier.commons.helpers.KeyValuePairs;
import rx.Subscriber;

/**
 * Created by László Gálosi on 23/03/16
 */
public class RestApiResponseException extends ErrorBundleException {

    public static final String RESP_STATUS = "Resp-Status";
    public static final String RESP_REASON = "Resp-Reason";
    public static final String RESP_BODY = "Resp-Body";
    public static final String RESP_URL_FROM = "Resp-Url-From";
    public static final String STACK_TRACE = "StackTrace";
    public static final String RESP_SENT = "Resp-Sent";

    static final String UNKNOWN_ERROR_RESPONSE =
          "{\"error\":\"unknown_error\",\"error_description\":\"Unknown error from the server\"}";
    public static final String EMPTY_RESPONSE_BODY =
          "{\"error\":\"empty response body\",\"error_description\":\"%s\"}";
    public static final String TIMEOUT_ERROR = "timeout";
    static final String RAW_ERROR_RESPONSE = "raw";

    private KeyValuePairs<String, Object> mResponseInfo;
    private JsonSerializer<ErrorResponse> mJsonSerializer;
    private ErrorResponse mErrorResponse;
    private String mRawErrorResponse;
    private int mStatusCode;
    private String mResponseUrl;

    public RestApiResponseException(final KeyValuePairs<String, Object> params) {
        this(params, null);
    }

    public RestApiResponseException(final KeyValuePairs<String, Object> params,
          final Throwable cause) {
        super((String) params.getOrDefault(RESP_REASON, "Server response: Unknown error:"), cause);
        this.mResponseInfo = params;
        this.mJsonSerializer = new JsonSerializer<>();
        this.mStatusCode = (int) params.getOrDefault(RESP_STATUS, -1);
        this.mResponseUrl = (String) params.get(RESP_URL_FROM);
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public void initErrorResponse() {
        try {
            this.mErrorResponse = mJsonSerializer.deserialize(
                  (String) mResponseInfo.getOrDefault(RESP_BODY, UNKNOWN_ERROR_RESPONSE),
                  ErrorResponse.class);
        } catch (JsonSyntaxException e) {
            mRawErrorResponse =
                  ((String) mResponseInfo.getOrDefault(RESP_BODY, UNKNOWN_ERROR_RESPONSE))
                        .replaceAll("\\n", "");
            mErrorResponse = new ErrorResponse(RAW_ERROR_RESPONSE, mRawErrorResponse);
        }
    }

    public ErrorResponse getErrorResponse() {
        if (mErrorResponse == null) {
            mResponseInfo.getJsonSerializedAsync(RESP_BODY, ErrorResponse.class, mJsonSerializer)
                         .subscribe(new Subscriber<ErrorResponse>() {
                             @Override public void onCompleted() {
                             }

                             @Override public void onError(final Throwable e) {
                                 mErrorResponse = new ErrorResponse(
                                       RAW_ERROR_RESPONSE,
                                       (String) mResponseInfo.getOrDefault(RESP_BODY, ""));
                                 mRawErrorResponse =
                                       (String) mResponseInfo.getOrDefault(RESP_BODY, "");
                             }

                             @Override public void onNext(final ErrorResponse er) {
                                 mErrorResponse = er;
                             }
                         });
        }
        return mErrorResponse;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("RestApiResponseException{");
        sb.append("ResponseInfo=").append(mResponseInfo);
        sb.append(", tatusCode=").append(mStatusCode);
        sb.append(", url=").append(mResponseUrl);
        sb.append(", ErrorResponse=").append(mErrorResponse);
        sb.append('}');
        return sb.toString();
    }

    public static class ErrorResponse {
        @SerializedName("error") public String error;
        @SerializedName("error_description") public String errorDescription;
        @SerializedName("Message") public String message;

        public ErrorResponse(final String error, final String errorDescription) {
            this.error = error;
            this.errorDescription = errorDescription;
        }

        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("ErrorResponse{");
            sb.append("error='").append(error).append('\'');
            sb.append(", errorDescription='").append(errorDescription).append('\'');
            sb.append(", message='").append(message).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
