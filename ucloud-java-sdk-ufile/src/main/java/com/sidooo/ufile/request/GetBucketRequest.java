/*
 * Copyright © 2018 UCloud (上海优刻得信息科技有限公司)
 *
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
import com.sidooo.ufile.UFileRegion;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UBucket;

import static java.util.Objects.requireNonNull;

public final class GetBucketRequest
        extends UBucketRequest
{
    public GetBucketRequest(UFileRegion region, String bucketName)
    {
        super(HttpType.GET, "DescribeBucket", region);
        requireNonNull(bucketName, "Bucket name is null");
        this.addParameter("BucketName", bucketName);
    }

    @Override
    public Object execute(BucketExecutor executor) throws UFileServiceException
    {
        UResponse response = executor.execute(this);
        JsonObject json = response.getResponse();
        if (!json.has("DataSet")) {
            throw new UFileServiceException(200, "DataSet missing.");
        }
        JsonArray dataSet = json.getAsJsonArray("DataSet");
        if (dataSet.size() != 1) {
            throw new UFileServiceException(200, "Multiple bucket exists in GetBucketResponse.");
        }
        JsonObject jsonBucket = dataSet.get(0).getAsJsonObject();
        if (!jsonBucket.has("BucketName")) {
            throw new UFileServiceException(200, "Bucket Name missing.");
        }
        String bucketName = jsonBucket.get("BucketName").getAsString();

        if (!jsonBucket.has("BucketId")) {
            throw new UFileServiceException(200, "Bucket Id missing.");
        }
        String bucketId = jsonBucket.get("BucketId").getAsString();

        return new UBucket(bucketId, bucketName);
    }
}
