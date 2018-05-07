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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class DeleteBucketRequest
        extends UBucketRequest
{
    private String bucketName;

    private String bucketId;

    public DeleteBucketRequest(UFileCredentials credentials, String bucketName)
    {
        super(credentials, "DeleteBucket");
        this.bucketName = bucketName;
    }

    @Override
    public HttpUriRequest createHttpRequest()
            throws UFileClientException
    {
        try {
            URIBuilder builder = new URIBuilder("https://" + UCLOUD_API_HOST);
            builder.setParameter("BucketName", this.bucketName);
            return new HttpGet(builder.build());
        }
        catch (URISyntaxException e) {
            throw new UFileClientException("Create Http Request Error.", e);
        }
    }

    @Override
    public void onSuccess(JsonObject json)
            throws UFileServiceException
    {
        if (!json.has("BucketId")) {
            throw new UFileServiceException("Bucket Id missing.");
        }
        this.bucketId = json.get("BucketId").getAsString();
    }

    public String getDeletedBucketId()
    {
        return this.bucketId;
    }
}
