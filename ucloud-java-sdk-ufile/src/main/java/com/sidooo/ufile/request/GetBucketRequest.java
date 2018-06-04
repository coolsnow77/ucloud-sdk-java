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
import org.apache.http.Header;

import java.io.InputStream;

public class GetBucketRequest
        extends UBucketRequest
{
    // 输出结果
    private UBucket bucket;

    public GetBucketRequest(String region, String bucketName)
    {
        super(HttpType.GET, "DescribeBucket", region);
        this.addParameter("BucketName", bucketName);
    }

    @Override
    public void onSuccess(JsonObject json, Header[] headers, InputStream content)
            throws UFileServiceException
    {
        if (!json.has("DataSet")) {
            throw new UFileServiceException("DataSet missing.");
        }
        JsonArray dataSet = json.getAsJsonArray("DataSet");
        if (dataSet.size() != 1) {
            throw new UFileServiceException(("Multiple bucket exists in GetBucketResponse."));
        }
        JsonObject jsonBucket = dataSet.get(0).getAsJsonObject();
        if (!jsonBucket.has("BucketName")) {
            throw new UFileServiceException("Bucket Name missing.");
        }
        String bucketName = jsonBucket.get("BucketName").getAsString();

        if (!jsonBucket.has("BucketId")) {
            throw new UFileServiceException("Bucket Id missing.");
        }
        String bucketId = jsonBucket.get("BucketId").getAsString();

        this.bucket = new UBucket(bucketId, bucketName);
    }

    public UBucket getBucket()
    {
        return this.bucket;
    }
}
