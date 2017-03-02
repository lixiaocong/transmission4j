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

package com.lixiaocong.transmission4j.request.torrent.action;

import com.lixiaocong.transmission4j.exception.JsonException;
import com.lixiaocong.transmission4j.request.TransmissionRequest;
import com.lixiaocong.transmission4j.request.TransmissionRequestFactory;
import com.lixiaocong.transmission4j.utils.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class TorrentActionRequestTest
{
    private Log log = LogFactory.getLog(getClass().getName());

    @Test
    public void testTorrentActionRequest() throws JsonException {
        List<Integer> ids = new LinkedList<>();
        ids.add(1);
        ids.add(2);
        ids.add(3);

        TransmissionRequest startRequest = TransmissionRequestFactory.getStartRequest(ids);
        TransmissionRequest stopRequest = TransmissionRequestFactory.getStopRequest(ids);
        TransmissionRequest startRequestWithNoId = TransmissionRequestFactory.getStartRequest(null);
        TransmissionRequest stopRequestWithNoId = TransmissionRequestFactory.getStopRequest(null);

        log.info(JsonUtil.getJson(startRequest));
        log.info(JsonUtil.getJson(stopRequest));
        log.info(JsonUtil.getJson(startRequestWithNoId));
        log.info(JsonUtil.getJson(stopRequestWithNoId));
    }
}