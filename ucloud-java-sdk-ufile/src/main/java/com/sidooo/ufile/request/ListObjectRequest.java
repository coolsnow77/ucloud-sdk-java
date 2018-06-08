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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sidooo.ufile.UFileRegion;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UObjectListing;
import com.sidooo.ufile.model.UObjectSummary;

import java.util.Date;

import static java.util.Objects.requireNonNull;

/**
 * Request Parameters
 *   None
 *
 * Request Headers
 *   prefix : 对象prefix
 *   marker : 标志字符串
 *   limit  : 文件列表数目
 *   Authorization: 授权签名
 *
 * Request Elements
 *   None
 *
 * Response Elements
 *   BucketName:
 *   BucketId:
 *   NextMarker:
 *   DataSet:
 *
 */
public final class ListObjectRequest
        extends UObjectRequest
{
    private String prefix;
    private String marker;
    private int limit = 20;

    public ListObjectRequest(UFileRegion region, String bucketName,
            String prefix)
    {
        this(region, bucketName, prefix, 20);
    }

    public ListObjectRequest(UFileRegion region, String bucketName,
            String prefix, int limit)
    {
        super(HttpType.GET, region, bucketName);
        this.prefix = prefix;
        this.limit = limit;
        this.addParameter("limit", Integer.toString(limit));
    }

    public ListObjectRequest(UFileRegion region, String bucketName,
            String prefix, int limit, String marker)
    {
        super(HttpType.GET, region, bucketName);
        this.prefix = prefix;
        this.addParameter("marker", marker);
        this.addParameter("limit", Integer.toString(limit));
    }

    @Override
    public Object execute(ObjectExecutor executor)
            throws UFileServiceException
    {
        requireNonNull(executor, "Object executor is null");

        UResponse response = executor.execute(this, "");
        JsonObject json = response.getResponse();

        if (!json.get("BucketName").getAsString().equals(getBucketName())) {
            throw new UFileServiceException(200, "Bucket Name mismatch.");
        }
        UObjectListing objectListing = new UObjectListing();
        objectListing.setBucketName(getBucketName());
        String bucketId = json.get("BucketId").getAsString();
        if (bucketId == null) {
            throw new UFileServiceException(200, "Bucket Id missing.");
        }
        String nextMarker = json.get("NextMarker").getAsString();
        if (nextMarker != null && nextMarker.length() > 0) {
            objectListing.setNextMarker(nextMarker);
        }
        else {
            objectListing.setTruncated(false);
        }

        objectListing.setLimit(limit);
        objectListing.setPrefix(prefix);

        JsonArray array = json.get("DataSet").getAsJsonArray();
        for (JsonElement entry : array) {
            UObjectSummary objectSummary = new UObjectSummary();
            String fileName = entry.getAsJsonObject().get("FileName").getAsString();
            objectSummary.setObjectKey(fileName);
            Long size = entry.getAsJsonObject().get("Size").getAsLong();
            objectSummary.setSize(size);
            String hash = entry.getAsJsonObject().get("Hash").getAsString();
            objectSummary.setHash(hash);
            String mimeType = entry.getAsJsonObject().get("MimeType").getAsString();
            objectSummary.setMimeType(mimeType);
            Long created = entry.getAsJsonObject().get("CreateTime").getAsLong();
            objectSummary.setCreated(new Date(created));
            Long modifyTimestamp = entry.getAsJsonObject().get("ModifyTime").getAsLong();
            objectSummary.setLastModified(new Date(modifyTimestamp));
            objectListing.putObjectSummary(objectSummary);
        }

        return objectListing;
    }
}
