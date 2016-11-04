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

package com.neatier.shell.data.network.retrofit;

import com.google.gson.JsonElement;
import java.util.Map;
import retrofit2.adapter.rxjava.Result;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rx.Observable;

/**
 * Interface for data layer objects REST api definitions with
 * retrofit.
 * Created by László Gálosi on 29/07/15
 */
public interface NeatierShellApiService {
    @GET("/v2/{XUID}/{TITLE_TYPE}") @Headers({ "Content-Type: application/json; charset=utf-8" })
    Observable<Result<JsonElement>> listUserTitles(
          @Header("X-AUTH") String xAuth,
          @Header("Accept-Language") String lang,
          @Path("XUID") String xboxUserId,
          @Path("TITLE_TYPE") String listType,
          @QueryMap Map<String, Object> params);

    @GET("/v2/{TITLE_TYPE}") @Headers({ "Content-Type: application/json; charset=utf-8" })
    Observable<Result<JsonElement>> listTitles(
          @Header("X-AUTH") String xAuth,
          @Header("Accept-Language") String lang,
          @Path("TITLE_TYPE") String listType,
          @QueryMap Map<String, Object> params);

    @GET("/v2/browse-marketplace/{TITLE_TYPE}/")
    @Headers({ "Content-Type: application/json; charset=utf-8" })
    Observable<Result<JsonElement>> browseTitles(
          @Header("X-AUTH") String xAuth,
          @Header("Accept-Language") String lang,
          @Path("TITLE_TYPE") String listType,
          @QueryMap Map<String, Object> params);
}
