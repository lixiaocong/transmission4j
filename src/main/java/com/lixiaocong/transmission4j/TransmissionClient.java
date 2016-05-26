/**
 * Copyright (c) 2016, lixiaocong <lxccs@iCloud.com>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * Neither the name of transmission4j nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.lixiaocong.transmission4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lixiaocong.transmission4j.exception.AuthException;
import com.lixiaocong.transmission4j.exception.JsonException;
import com.lixiaocong.transmission4j.exception.NetworkException;
import com.lixiaocong.transmission4j.request.TransmissionRequest;
import com.lixiaocong.transmission4j.request.torrent.accessors.TorrentGetRequest;
import com.lixiaocong.transmission4j.request.torrent.action.TorrentStartRequest;
import com.lixiaocong.transmission4j.request.torrent.action.TorrentStopRequest;
import com.lixiaocong.transmission4j.request.torrent.add.TorrentAddRequest;
import com.lixiaocong.transmission4j.response.TransmissionResponse;
import com.lixiaocong.transmission4j.response.torrent.accessors.Torrent;
import com.lixiaocong.transmission4j.response.torrent.accessors.TorrentGetResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Transmission java client
 */
public class TransmissionClient
{
    private static Log logger = LogFactory.getLog(TransmissionClient.class);

    private String username;
    private String password;
    private String id;  //id is used in transmission rpc, details in https://trac.transmissionbt.com/browser/trunk/extras/rpc-spec.txt

    private ObjectMapper mapper;
    private HttpClient httpClient;
    private HttpPost httpPost;

    public TransmissionClient(String username, String password, String uri)
    {
        logger.info("new TransmissionClient username:" + username + " password:" + password + " uri:" + uri);
        this.username = username;
        this.password = password;
        this.id = null;

        mapper = new ObjectMapper();
        buildHttpClient();

        httpPost = new HttpPost(uri);
        RequestConfig config = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).setConnectionRequestTimeout(2000).build();
        httpPost.setConfig(config);
    }

    private void buildHttpClient()
    {
        logger.info("build client with X-id " + id);
        Header authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, String.format("Basic %s", Base64.encodeBase64String((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
        Header idHeader = new BasicHeader("X-Transmission-Session-Id", id);
        List<Header> headers = new ArrayList<>();
        headers.add(authHeader);
        headers.add(idHeader);
        httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
    }

    private <T extends TransmissionResponse> T execute(TransmissionRequest request, Class<T> c) throws NetworkException, AuthException, JsonException
    {
        String requestStr;
        try
        {
            requestStr = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e)
        {
            logger.error("write object to json error", e);
            throw new JsonException(e.toString());
        }

        logger.info("execute request " + requestStr);
        httpPost.setEntity(new StringEntity(requestStr, ContentType.APPLICATION_JSON));
        HttpResponse response;
        try
        {
            response = httpClient.execute(httpPost);
        } catch (IOException e)
        {
            logger.warn("execute failed.", e);
            throw new NetworkException(e.toString());
        }

        int code = response.getStatusLine().getStatusCode();
        if (code == HttpStatus.SC_OK)
        {
            String responseStr;
            logger.info("execute success");
            try
            {
                responseStr = EntityUtils.toString(response.getEntity());
            } catch (IOException e)
            {
                logger.warn("read content of " + requestStr + ". exception:", e);
                throw new NetworkException(e.toString());
            }
            logger.info("execute response " + responseStr);
            try
            {
                return mapper.readValue(responseStr, c);
            } catch (IOException e)
            {
                logger.error("read object from json error", e);
                throw new JsonException(e.toString());
            }
        } else if (code == HttpStatus.SC_CONFLICT)
        {
            logger.info("execute response 409");
            id = response.getHeaders("X-Transmission-Session-Id")[0].getValue();
            buildHttpClient();
            return execute(request, c);
        } else if (code == HttpStatus.SC_UNAUTHORIZED)
        {
            logger.info("execute response 401");
            throw new AuthException("username: " + username + " or password " + password + " incorrect");
        }
        logger.error("execute error with response code " + code);
        throw new NetworkException("execute error with response code " + code);
    }

    public boolean torrentStart(List<Integer> ids) throws AuthException, NetworkException, JsonException
    {
        TransmissionRequest request = new TorrentStartRequest(ids);
        TransmissionResponse response = execute(request, TransmissionResponse.class);
        return response.getResult().equals("success");
    }

    public boolean torrentStop(List<Integer> ids) throws AuthException, NetworkException, JsonException
    {
        logger.info("start torrent with ids " + ids);
        TransmissionRequest request = new TorrentStopRequest(ids);
        TransmissionResponse response = execute(request, TransmissionResponse.class);
        return response.getResult().equals("success");
    }

    public boolean torrentAdd(String metainfo) throws AuthException, NetworkException, JsonException
    {
        logger.info("add torrent with metainfo " + metainfo);
        TransmissionRequest request = new TorrentAddRequest(metainfo);
        TransmissionResponse response = execute(request, TransmissionResponse.class);
        return response.getResult().equals("success");
    }

    public List<Torrent> torrentGet(List<Integer> ids) throws AuthException, NetworkException, JsonException
    {
        logger.info("get torrent with ids " + ids);
        TransmissionRequest request = new TorrentGetRequest(ids);
        TorrentGetResponse response = execute(request, TorrentGetResponse.class);
        return response.getArguments().getTorrents();
    }
}
