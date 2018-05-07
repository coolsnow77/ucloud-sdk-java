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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sidooo.ufile.UFileCredentials;
import com.sidooo.ufile.UObject;
import com.sidooo.ufile.UObjectListing;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.io.InputStream;
import java.net.URISyntaxException;

/*
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
 *
 */
public class ListObjectRequest
        extends UObjectRequest
{
    private String prefix;
    private String marker;
    private int limit = 20;

    private UObjectListing objectListing;

    public ListObjectRequest(UFileCredentials credentials, String bucketName,
            String prefix)
    {
        super(credentials, bucketName);
        this.prefix = prefix;
    }

    public ListObjectRequest(UFileCredentials credentials, String bucketName,
            String prefix, int limit)
    {
        super(credentials, bucketName);
        this.prefix = prefix;
        this.limit = limit;
    }

    public ListObjectRequest(UFileCredentials credentials, String bucketName,
            String prefix, int limit, String marker)
    {
        super(credentials, bucketName);
        this.prefix = prefix;
        this.marker = marker;
        this.limit = limit;
    }

    @Override
    public HttpUriRequest createHttpRequest() throws UFileClientException
    {
        try {
            String uri = "http://"
                    + getBucketName() + getCredentials().getProxySuffix()
                    + "/?list";
            URIBuilder builder = new URIBuilder(uri);
            if (marker != null) {
                builder.setParameter("marker", marker);
            }
            builder.setParameter("limit", Integer.toString(limit));
            HttpGet request = new HttpGet(builder.build());
            return request;
        }
        catch (URISyntaxException e) {
            throw new UFileClientException("URI Syntax Error.", e);
        }
    }

    @Override
    public void onSuccess(JsonObject json, Header[] headers, InputStream content) throws UFileServiceException
    {
        objectListing = new UObjectListing();

        if (!json.get("BucketName").getAsString().equals(getBucketName())) {
            throw new UFileServiceException("Bucket Name mismatch.");
        }
        objectListing.setBucketName(getBucketName());
        String bucketId = json.get("BucketId").getAsString();
        if (bucketId == null) {
            throw new UFileServiceException("Bucket Id missing.");
        }
        String nextMarker = json.get("NextMarker").getAsString();
        if (nextMarker != null) {
            objectListing.setNextMarker(nextMarker);
        }

        objectListing.setLimit(limit);
        objectListing.setPrefix(prefix);

        JsonArray array = json.get("DataSet").getAsJsonArray();
        for (JsonElement entry : array) {
            UObject object = new UObject();
            String fileName = entry.getAsJsonObject().get("FileName").getAsString();
            object.setKey(fileName);
            Long size = entry.getAsJsonObject().get("Size").getAsLong();
            object.setLengnth(size);
            String hash = entry.getAsJsonObject().get("Hash").getAsString();
            object.setHash(hash);
//            String mimeType = object.getAsJsonObject().get("MimeType").getAsString();
//            Long createTimestamp = object.getAsJsonObject().get("CreateTime").getAsLong();
//            Long modifyTimestamp = object.getAsJsonObject().get("ModifyTime").getAsLong();
//            metadata.setLastModified(new Date(modifyTimestamp));
            objectListing.putObjectSummary(object);
        }
    }

    public UObjectListing getObjectListing()
    {
        return this.objectListing;
    }
}