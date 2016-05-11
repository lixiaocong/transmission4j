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
import com.lixiaocong.transmission4j.exception.NetworkException;
import com.lixiaocong.transmission4j.request.TransmissionRequest;
import com.lixiaocong.transmission4j.request.torrent.accessors.TorrentGetRequest;
import com.lixiaocong.transmission4j.request.torrent.action.TorrentStartRequest;
import com.lixiaocong.transmission4j.request.torrent.action.TorrentStopRequest;
import com.lixiaocong.transmission4j.response.TransmissionResponse;
import com.lixiaocong.transmission4j.response.torrent.accessors.Torrent;
import com.lixiaocong.transmission4j.response.torrent.accessors.TorrentGetResponse;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class TransmissionClient
{
    private static TransmissionClient ourInstance = new TransmissionClient();
    private String username;
    private String password;
    private URL url;
    //id is used in transmission rpc, details in https://trac.transmissionbt.com/browser/trunk/extras/rpc-spec.txt
    private String id;

    private TransmissionClient()
    {
        this.username = null;
        this.password = null;
        this.url = null;
        this.id = null;
    }

    public static TransmissionClient getInstance()
    {
        return ourInstance;
    }

    public static void init(String username, String password, URL url)
    {
        ourInstance.username = username;
        ourInstance.password = password;
        ourInstance.url = url;
    }

    private String execute(TransmissionRequest request) throws NetworkException, AuthException
    {
        ObjectMapper mapper = new ObjectMapper();
        String jsonRequest = null;
        try
        {
            jsonRequest = mapper.writeValueAsString(request);
        } catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }

        //config the connection
        HttpURLConnection conn;
        PrintWriter out;

        try
        {
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
        } catch (IOException e)
        {
            //TODO I'm not sure in what condition this will happen
            //If connect to an address which not exists, no exception will be thrown
            e.printStackTrace();
            throw new NetworkException("can't open connection" + url.getPath());
        }

        try
        {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", String.format("Basic %s", Base64.encode((username + ":" + password).getBytes("UTF-8"))));
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (id != null) conn.setRequestProperty("X-Transmission-Session-Id", id);
            out = new PrintWriter(conn.getOutputStream());
            out.print(jsonRequest);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null)
            {
                result += line;
            }
            return result;
        } catch (IOException e)
        {
            //there are 3 reasons to cause this exception
            //1.network exception
            //2.id is null
            //3.username or password wrong
            //if the response String's format is not json , it should be transmission's bug
            int responseCode;
            try
            {
                responseCode = conn.getResponseCode();
            } catch (IOException e1)//network not connected
            {
                throw new NetworkException("can't connect to given url" + url.getPath());
            }

            //id is null, update the id and try again
            if (responseCode == 409)
            {
                id = conn.getHeaderFields().get("X-Transmission-Session-Id").get(0);
                return execute(request);
            } else//password or username is wrong
            {
                throw new AuthException("auth failed");
            }
        }
    }

    public boolean torrentStart(List<Integer> ids) throws IOException
    {
        TransmissionRequest request = new TorrentStartRequest(ids);
        ObjectMapper mapper = new ObjectMapper();
        TransmissionResponse response = mapper.readValue(execute(request), TransmissionResponse.class);
        return "success".equals(response.getResult());
    }

    public boolean torrentStop(List<Integer> ids) throws IOException
    {
        TransmissionRequest request = new TorrentStopRequest(ids);
        ObjectMapper mapper = new ObjectMapper();
        TransmissionResponse response = mapper.readValue(execute(request), TransmissionResponse.class);
        return "success".equals(response.getResult());
    }

    public List<Torrent> torrentGet(List<Integer> ids) throws IOException
    {
        TransmissionRequest request = new TorrentGetRequest(ids);
        ObjectMapper mapper = new ObjectMapper();
        TorrentGetResponse response = mapper.readValue(execute(request), TorrentGetResponse.class);
        return response.getArguments().getTorrents();
    }
}
