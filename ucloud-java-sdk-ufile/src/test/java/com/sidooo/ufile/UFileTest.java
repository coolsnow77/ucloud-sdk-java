/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sidooo.ufile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UFileTest
{
    @Test
    public void test1()
    {
        String path = "http://cn-bj.ufileos.com/tpc/tpch-s1/orders.tbl";

        UFileURI uri = new UFileURI(path);
        assertEquals(uri.getRegion(), "cn-bj");
        assertEquals(uri.getBucket(), "tpc");
        assertEquals(uri.getKey(), "tpch-s1/orders.tbl");
    }

    @Test
    public void test2()
    {
        String path = "http://tpc.cn-bj.ufileos.com/tpch-s1/orders.tbl";

        UFileURI uri = new UFileURI(path);
        assertEquals(uri.getRegion(), "cn-bj");
        assertEquals(uri.getBucket(), "tpc");
        assertEquals(uri.getKey(), "tpch-s1/orders.tbl");
    }

    @Test
    public void test3()
    {
        String path = "ufile://tpc.cn-bj/tpch-s1/orders.tbl";

        UFileURI uri = new UFileURI(path);
        assertEquals(uri.getRegion(), "cn-bj");
        assertEquals(uri.getBucket(), "tpc");
        assertEquals(uri.getKey(), "tpch-s1/orders.tbl");
    }

    @Test
    public void test4()
    {
        String path = "ufile://tpc/tpch-s1/orders.tbl";

        UFileURI uri = new UFileURI(path);
        assertEquals(uri.getRegion(), null);
        assertEquals(uri.getBucket(), "tpc");
        assertEquals(uri.getKey(), "tpch-s1/orders.tbl");
    }
}
