/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gft.adapters.backend;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.streampipes.connect.adapter.guess.SchemaGuesser;
import org.apache.streampipes.connect.adapter.model.generic.Protocol;
import org.apache.streampipes.connect.adapter.sdk.ParameterExtractor;
import org.apache.streampipes.connect.api.IFormat;
import org.apache.streampipes.connect.api.IParser;
import org.apache.streampipes.connect.api.exception.ParseException;
import org.apache.streampipes.model.AdapterType;
import org.apache.streampipes.model.connect.grounding.ProtocolDescription;
import org.apache.streampipes.model.connect.guess.GuessSchema;
import org.apache.streampipes.model.schema.EventSchema;
import org.apache.streampipes.sdk.builder.adapter.ProtocolDescriptionBuilder;
import org.apache.streampipes.sdk.helpers.AdapterSourceType;
import org.apache.streampipes.sdk.helpers.Locales;
import org.apache.streampipes.sdk.utils.Assets;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/*TODO create for length insertion
       write documentation
 */

public class HttpProtocol extends PullProtocol {

  private static final long interval = 300;
  Logger logger = LoggerFactory.getLogger(Protocol.class);
  public static final String ID = "org.gft.adapters.backend";
  HttpConfig config;

  public HttpProtocol() {
  }

  public HttpProtocol(IParser parser, IFormat format, HttpConfig config) {
    super(parser, format, interval);
    this.config = config;
  }

  @Override
  public ProtocolDescription declareModel() {
    return ProtocolDescriptionBuilder.create(ID)
            .withAssets(Assets.DOCUMENTATION, Assets.ICON)
            .withLocales(Locales.EN)
            .category(AdapterType.Generic)
            .sourceType(AdapterSourceType.STREAM)
            .requiredTextParameter(HttpUtils.getUsernameLabel())
            .requiredSecret(HttpUtils.getPasswordLabel())
            .requiredTextParameter(HttpUtils.getSignalLabel())
            .requiredIntegerParameter(HttpUtils.getLengthLabel())
            .requiredTextParameter(HttpUtils.getLowestLabel())
            .requiredTextParameter(HttpUtils.getHighestLabel(), "CurrentDateTime")
            .build();
  }

  @Override
  public Protocol getInstance(ProtocolDescription protocolDescription, IParser parser, IFormat format) {
    ParameterExtractor extractor = new ParameterExtractor(protocolDescription.getConfig());
    HttpConfig config = HttpUtils.getConfig(extractor);
    return new HttpProtocol(parser, format, config);
  }

  @Override
  public GuessSchema getGuessSchema() throws ParseException {
    int n = 2;

    InputStream dataInputStream = getDataFromEndpoint();

    List<byte[]> dataByte = parser.parseNEvents(dataInputStream, n);
    if (dataByte.size() < n) {
      logger.error("Error in HttpStreamProtocol! Required: " + n + " elements but the resource just had: " +
              dataByte.size());

      dataByte.addAll(dataByte);
    }
    EventSchema eventSchema= parser.getEventSchema(dataByte);

    return SchemaGuesser.guessSchema(eventSchema);
  }

  @Override
  public List<Map<String, Object>> getNElements(int n) throws ParseException {
    List<Map<String, Object>> result = new ArrayList<>();

    InputStream dataInputStream = getDataFromEndpoint();

    List<byte[]> dataByte = parser.parseNEvents(dataInputStream, n);

    // Check that result size is n. Currently just an error is logged. Maybe change to an exception
    if (dataByte.size() < n) {
      logger.error("Error in HttpStreamProtocol! User required: " + n + " elements but the resource just had: " +
              dataByte.size());
    }

    for (byte[] b : dataByte) {
      result.add(format.parse(b));
    }

    return result;
  }

  @Override
  public void stop() {

  }

  public InputStream getDataFromEndpoint() throws ParseException {
    String urlString;
    InputStream result = null;
    String accessToken = login();

    if(config.getHighestDate().equals("CurrentDateTime")){
      urlString = config.getBaseUrl()+"?page="+config.getPage()+"&length="+config.getLength()+"&filter="+config.getFilter(CurrentDateTime())+"&sort="+config.getSort();
    }else {
      urlString = config.getBaseUrl()+"?page="+config.getPage()+"&length="+config.getLength()+"&filter="+config.getFilter(config.getHighestDate())+"&sort="+config.getSort();
    }

    //replace spaces by "%20" to avoid 400 Bad Request
    if(urlString.contains(" "))
      urlString = urlString.replace(" ", "%20");

    try {
      // Set the URL of the API endpoint
      URL url = new URL(urlString);
      // Open a connection to the API endpoint
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setRequestProperty("Accept", "application/json");
      // Set the token in the HTTP header of the request
      connection.setRequestProperty("Authorization", "Bearer " + accessToken);
      connection.setRequestProperty("Transfer-Encoding", "chunked");
      connection.setRequestProperty("Connection", "keep-alive");
      connection.setDoOutput(true);
      connection.setConnectTimeout(4000000);
      connection.setReadTimeout(400000);
      // Send the GET request to the API endpoint
      connection.connect();

      result = connection.getInputStream();
      System.out.println(result);
    } catch (Exception e) {
      // Handle any exceptions that occur
      e.printStackTrace();
    }
    return result;
  }

  public String CurrentDateTime(){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    System.out.println(dtf2.format(now));
    return dtf.format(now);
  }

  @Override
  public String getId() {
    return ID;
  }


  private String login() throws org.apache.http.ParseException {
    String response, token;

    try {
      Request request = Request.Post(config.getLoginUrl())
              .connectTimeout(1000)
              .socketTimeout(100000)
              .setHeader("Content-Type", "application/x-www-form-urlencoded")
              .setHeader("Accept","application/json")
              .bodyForm(Form.form().add("grant_type", config.getGrantType()).add("client_id", config.getClientId())
                      .add("client_secret", config.getClientSecret()).add("username",config.getUsername())
                      .add("password", config.getPassword()).add("scope",config.getScope()).build())
              .setHeader("connection","keep-alive");

      response = request
              .execute().returnContent().toString();

      if (response == null)
        throw new org.apache.http.ParseException("Could not receive Data from file: " + config.getLoginUrl());

      // Parse the JSON string as a JSON object
      JSONObject json_object = new JSONObject(response);
      // Access the data in the JSON object
      token = json_object.getString("access_token");

    } catch (Exception e) {
      System.out.println("Error while fetching data from URL: " + config.getLoginUrl() + e);
      throw new org.apache.http.ParseException("Error while fetching data from URL: " + config.getLoginUrl());
    }
    return token;
  }

}