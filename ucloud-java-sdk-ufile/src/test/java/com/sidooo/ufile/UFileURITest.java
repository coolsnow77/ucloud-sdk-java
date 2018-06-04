package com.sidooo.ufile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UFileURITest
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
