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

import com.lixiaocong.transmission4j.exception.AuthException;
import com.lixiaocong.transmission4j.exception.NetworkException;
import com.lixiaocong.transmission4j.response.Torrent;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class TransmissionClientTest {
    private Log log = LogFactory.getLog(getClass().getName());
    private TransmissionClient client;

    @Before
    public void before() throws MalformedURLException {
        client = new TransmissionClient("admin", "admin", "http://127.0.0.1:9091/transmission/rpc");
    }

    @Test
    public void testAuthError() {
        client = new TransmissionClient("admin", "error", "http://127.0.0.1:9091/transmission/rpc");
        Exception exception = null;
        try {
            client.getAll();
        } catch (AuthException | NetworkException e) {
            exception = e;
        }
        assert (exception instanceof AuthException);
    }

    @Test
    public void testNetworkError() {
        client = new TransmissionClient("admin", "admin", "http://127.0.0.1/transmission/rpc");
        Exception exception = null;
        try {
            client.getAll();
        } catch (AuthException | NetworkException e) {
            exception = e;
        }
        assert (exception instanceof NetworkException);
    }

    @Test
    public void startAll() throws Exception {
        assertTrue(client.startAll());
    }

    @Test
    public void start() throws Exception {
        List<Torrent> torrents = client.getAll();
        List<Integer> list = new LinkedList<>();
        for (Torrent torrent : torrents)
            list.add((int) torrent.getId());
        client.start(list);
    }

    @Test
    public void stopAll() throws Exception {
        assertTrue(client.stopAll());
    }

    @Test
    public void stop() throws Exception {
        List<Torrent> torrents = client.getAll();
        List<Integer> list = new LinkedList<>();
        for (Torrent torrent : torrents)
            list.add((int) torrent.getId());
        client.stop(list);
    }

    @Test
    public void add() throws Exception {
        File file = new File("src/test/resources/test.torrent");
        InputStream in = new FileInputStream(file);
        int len = in.available();
        byte[] data = new byte[len];
        in.read(data, 0, len);
        String str = Base64.encode(data);
        log.info(str);
        in.close();
        assertTrue(client.add(str));
    }

    @Test
    public void removeAll() throws Exception {
        assertTrue(client.removeAll());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, client.getAll().size());
    }

    @Test
    public void remove() throws Exception {
        List<Torrent> torrents = client.getAll();
        List<Integer> list = new LinkedList<>();
        for (Torrent torrent : torrents)
            list.add((int) torrent.getId());
        client.remove(list);
    }

    @Test
    public void getAll() throws Exception {
        List<Torrent> list = client.getAll();
        for (Torrent torrent : list)
            log.info(torrent.getName());
    }

    @Test
    public void get() throws Exception {

    }

    @Test
    public void sessionStats() throws Exception {
        log.info(client.sessionStats());
    }
}