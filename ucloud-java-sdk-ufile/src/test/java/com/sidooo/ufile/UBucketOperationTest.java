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

import com.sidooo.ufile.model.UBucket;
import com.sidooo.ufile.model.UBucketListing;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class UBucketOperationTest
{
    static final String UFILE_CONFIG_FILE = "/Users/kimzhang/.ucloud/ufile.properties";
    static final String LOCAL_TEST_FILE = "/Users/kimzhang/Downloads/Jenkins2.png";
    private UFileCredentials credentials;
    private String region = "cn-bj2";
    private UFile ufile;
    static final String TEST_BUCKET_NAME = "ufile-sdk-test";

    @Before
    public void setup()
    {
        credentials = new UFileCredentials();
        credentials.loadConfig(UFILE_CONFIG_FILE);
        ufile = UFileClientBuilder.standard(region, credentials);
    }

    @After
    public void teardown()
    {
        try {
            ufile.shutdown();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(expected = Test.None.class)
    public void test1()
            throws Exception
    {
        Random random = new Random();
        int max = 9999;
        int min = 1000;
        int s = random.nextInt(max) % (max - min + 1) + min;
        String bucketName = "usql-test-" + s;

        // 创建bucket
        UBucket newBucket = ufile.createBucket(bucketName, "public", "cn-bj2");
        assertEquals(newBucket.getName(), bucketName);

        // 获取bucket信息
        UBucket bucket = ufile.getBucket(bucketName);
        assertEquals(bucket.getId(), newBucket.getId());

        UBucketListing buckets = ufile.listBuckets();
        for (UBucket entry : buckets.getBuckets()) {
            System.out.println(entry.getId() + ":" + entry.getName());
        }

        // 删除bucket
//        ufile.deleteBucket(bucketName);
    }
}
