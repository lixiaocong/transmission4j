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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lixiaocong.transmission4j.exception.AuthException;
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
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TransmissionClient
{
    private String username;
    private String password;
    private String id;  //id is used in transmission rpc, details in https://trac.transmissionbt.com/browser/trunk/extras/rpc-spec.txt

    private ObjectMapper mapper;
    private HttpClient httpClient;
    private HttpPost httpPost;

    public TransmissionClient(String username, String password, String uri)
    {
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
        Header authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, String.format("Basic %s", Base64.encodeBase64String((username + ":" + password).getBytes(StandardCharsets.UTF_8))));
        Header idHeader = new BasicHeader("X-Transmission-Session-Id", id);
        List<Header> headers = new ArrayList<>();
        headers.add(authHeader);
        headers.add(idHeader);
        httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
    }

    private InputStream execute(String request) throws NetworkException, AuthException
    {
        httpPost.setEntity(new StringEntity(request, ContentType.APPLICATION_JSON));
        HttpResponse response;
        try
        {
            response = httpClient.execute(httpPost);
        } catch (IOException e)
        {
            throw new NetworkException(e.getMessage());
        }

        int code = response.getStatusLine().getStatusCode();
        if (code == 200)
        {
            try
            {
                return response.getEntity().getContent();
            } catch (IOException e)
            {
                e.printStackTrace();
                throw new NetworkException(e.getMessage());
            }
        } else if (code == 409)
        {
            id = response.getHeaders("X-Transmission-Session-Id")[0].getValue();
            buildHttpClient();
            return execute(request);
        } else if (code == 401)
        {
            throw new AuthException("username or password error");
        }
        return null;
    }

    public boolean torrentStart(List<Integer> ids) throws AuthException, NetworkException
    {
        TransmissionRequest request = new TorrentStartRequest(ids);
        TransmissionResponse response = null;
        try
        {
            response = mapper.readValue(execute(mapper.writeValueAsString(request)), TransmissionResponse.class);
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return "success".equals(response.getResult());
    }

    public boolean torrentStop(List<Integer> ids) throws AuthException, NetworkException
    {
        TransmissionRequest request = new TorrentStopRequest(ids);
        TransmissionResponse response = null;
        try
        {
            response = mapper.readValue(execute(mapper.writeValueAsString(request)), TransmissionResponse.class);
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return "success".equals(response.getResult());
    }

    public boolean torrentAdd(String metainfo) throws AuthException, NetworkException
    {
        TransmissionRequest request = new TorrentAddRequest(metainfo);
        TransmissionResponse response = null;
        try
        {
            response = mapper.readValue(execute(mapper.writeValueAsString(request)), TransmissionResponse.class);
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return "success".equals(response.getResult());
    }

    public List<Torrent> torrentGet(List<Integer> ids) throws AuthException, NetworkException
    {
        TransmissionRequest request = new TorrentGetRequest(ids);
        TorrentGetResponse response = null;
        try
        {
            response = mapper.readValue(execute(mapper.writeValueAsString(request)), TorrentGetResponse.class);
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
        return response.getArguments().getTorrents();
    }
}
