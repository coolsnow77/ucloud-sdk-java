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
import org.apache.http.Header;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class DeleteObjectRequest
        extends UObjectRequest
{
    private String objectKey;

    public DeleteObjectRequest(UFileCredentials credentials, String bucketName, String objectKey)
    {
        super(credentials, bucketName);
        this.objectKey = objectKey;
    }

    @Override
    public HttpUriRequest createHttpRequest()
            throws UFileClientException
    {
        try {
            String uri = "http://"
                    + getBucketName() + getCredentials().getProxySuffix()
                    + "/" + URLEncoder.encode(objectKey, "UTF-8");
            URIBuilder builder = new URIBuilder(uri);
            HttpDelete get = new HttpDelete(builder.build());
            return get;
        }
        catch (UnsupportedEncodingException e) {
            throw new UFileClientException("URI Encode Error.", e);
        }
        catch (URISyntaxException e) {
            throw new UFileClientException("URI Syntax Error.", e);
        }
    }

    @Override
    public void onSuccess(JsonObject response, Header[] headers, InputStream content)
            throws UFileServiceException
    {
    }

    public String getDeleteObjectKey()
    {
        return this.objectKey;
    }
}