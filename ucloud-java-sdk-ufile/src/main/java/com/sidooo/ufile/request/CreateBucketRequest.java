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

import com.google.gson.JsonObject;
import com.sidooo.ufile.UFileCredentials;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UBucket;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class CreateBucketRequest
        extends UBucketRequest
{
    // 输入参数
    private String bucketName;
    private String bucketType = "public";
    private String region = "cn-bj2";

    // 输出结果
    private UBucket bucket;

    public CreateBucketRequest(UFileCredentials credentials, String bucketName)
    {
        super(credentials, "CreateBucket");
        this.bucketName = bucketName;
    }

    public CreateBucketRequest(UFileCredentials credentials, String bucketName, String bucketType)
    {
        super(credentials, "CreateBucket");
        this.bucketName = bucketName;
        this.bucketType = bucketType;
    }

    public CreateBucketRequest(UFileCredentials credentials, String bucketName, String bucketType, String region)
    {
        super(credentials, "CreateBucket");
        this.bucketName = bucketName;
        this.bucketType = bucketType;
        this.region = region;
    }

    @Override
    public HttpUriRequest createHttpRequest()
            throws UFileClientException
    {
        try {
            URIBuilder builder = new URIBuilder("https://" + UCLOUD_API_HOST);
            builder.setParameter("BucketName", this.bucketName);
            builder.setParameter("Type", this.bucketType);
            builder.setParameter("Region", this.region);
            return new HttpGet(builder.build());
        }
        catch (URISyntaxException e) {
            throw new UFileClientException("Create Http Request Error.", e);
        }
    }

    @Override
    public void onSuccess(JsonObject result)
            throws UFileServiceException
    {
        if (!result.has("BucketName")) {
            throw new UFileServiceException("Bucket Name missing.");
        }
        String bucketName = result.get("BucketName").getAsString();

        if (!result.has("BucketId")) {
            throw new UFileServiceException("Bucket Id missing.");
        }
        String bucketId = result.get("BucketId").getAsString();

        this.bucket = new UBucket(bucketId, bucketName);
    }

    public UBucket getNewBucket()
    {
        return this.bucket;
    }
}
