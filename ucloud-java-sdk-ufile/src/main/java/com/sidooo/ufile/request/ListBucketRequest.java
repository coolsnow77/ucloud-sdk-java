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
package com.sidooo.ufile.request;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UBucket;
import com.sidooo.ufile.model.UBucketListing;
import org.apache.http.Header;

import java.io.InputStream;

public class ListBucketRequest
        extends UBucketRequest
{
    // 输入参数
    private int offset;
    private int limit = 20;

    // 输出结果
    private UBucketListing buckets;

    public ListBucketRequest(String region)
    {
        this(region, 0, 20);
    }

    public ListBucketRequest(String region, int offset, int limit)
    {
        super(HttpType.GET, "DescribeBucket", region);
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public void onSuccess(JsonObject json, Header[] headers, InputStream content)
            throws UFileServiceException
    {
        if (!json.has("DataSet")) {
            throw new UFileServiceException("DataSet missing.");
        }
        JsonArray dataSet = json.getAsJsonArray("DataSet");
        buckets = new UBucketListing();
        for (int i = 0; i < dataSet.size(); i++) {
            UBucket bucket = new UBucket();
            String bucketId = dataSet.get(0).getAsJsonObject().get("BuckeId").getAsString();
            bucket.setId(bucketId);
            String bucketName = dataSet.get(0).getAsJsonObject().get("BucketName").getAsString();
            bucket.setName(bucketName);
            buckets.putBucket(bucket);
        }
    }

    public UBucketListing getBucketListing()
    {
        return this.buckets;
    }
}
