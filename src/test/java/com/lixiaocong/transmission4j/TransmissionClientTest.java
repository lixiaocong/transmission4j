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

import com.lixiaocong.transmission4j.response.torrent.accessors.Torrent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class TransmissionClientTest
{
    private TransmissionClient client;

    @Before
    public void before() throws MalformedURLException
    {
        TransmissionClient.init("admin", "admin", new URL("http://192.168.1.120:9091/transmission/rpc"));
        client = TransmissionClient.getInstance();
    }

    @Test
    public void testTorrentStart() throws IOException
    {
        assertTrue(client.torrentStart(null));
    }

    @Test
    public void testTorrentStop() throws IOException
    {
        assertTrue(client.torrentStop(null));
    }

    @Test
    public void testTorrentGetTest() throws IOException
    {
        List<Torrent> list = client.torrentGet(null);
        for (Torrent torrent : list)
            System.out.println(torrent);
    }
}