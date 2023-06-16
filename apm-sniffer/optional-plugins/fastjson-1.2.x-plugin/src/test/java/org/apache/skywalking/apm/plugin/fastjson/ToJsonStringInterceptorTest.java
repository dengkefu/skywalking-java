/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.plugin.fastjson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.apache.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.apache.skywalking.apm.agent.test.helper.SegmentHelper;
import org.apache.skywalking.apm.agent.test.tools.AgentServiceRule;
import org.apache.skywalking.apm.agent.test.tools.SegmentStorage;
import org.apache.skywalking.apm.agent.test.tools.SegmentStoragePoint;
import org.apache.skywalking.apm.agent.test.tools.TracingSegmentRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class ToJsonStringInterceptorTest {

    private ToJsonStringInterceptor toJsonStringInterceptor;

    @SegmentStoragePoint
    private SegmentStorage segmentStorage;

    @Rule
    public AgentServiceRule serviceRule = new AgentServiceRule();

    private Object[] arguments;

    private Object result;

    private Class[] argumentType;

    @Data
    private class TestBean {
        private int key;
    }

    @Before
    public void setUp() {
        toJsonStringInterceptor = new ToJsonStringInterceptor();
        arguments = new Object[]{new TestBean()};
        result = new JSONObject().toJSONString();
    }

    @Test
    public void testSendMessage() throws Throwable {

        Method method = JSON.class.getMethod("toJSONString", Object.class);
        toJsonStringInterceptor.beforeMethod(null, method, arguments, null, null);
        toJsonStringInterceptor.afterMethod(null, method, arguments, null, result);

        List<TraceSegment> traceSegmentList = segmentStorage.getTraceSegments();
        assertThat(traceSegmentList.size(), is(1));

        TraceSegment segment = traceSegmentList.get(0);
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(segment);
        assertThat(spans.size(), is(1));
    }

}