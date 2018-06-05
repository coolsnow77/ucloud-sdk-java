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

import com.google.gson.JsonObject;
import com.sidooo.ufile.UFileRegion;
import com.sidooo.ufile.exception.UFileServiceException;

public final class DeleteBucketRequest
        extends UBucketRequest
{
    public DeleteBucketRequest(UFileRegion region, String bucketName)
    {
        super(HttpType.DELETE, "DeleteBucket", region);
        this.addParameter("BucketName", bucketName);
    }

    @Override
    public Object execute(BucketExecutor executor)
            throws UFileServiceException
    {
        UResponse response = executor.execute(this);
        JsonObject json = response.getResponse();
        if (!json.has("BucketId")) {
            throw new UFileServiceException("Bucket Id missing.");
        }
        return json.get("BucketId").getAsString();
    }
}
