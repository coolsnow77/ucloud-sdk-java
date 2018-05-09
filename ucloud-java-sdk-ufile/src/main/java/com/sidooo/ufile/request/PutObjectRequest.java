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
import com.sidooo.ufile.model.UObjectMetadata;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/*
 * Request Parameters
 *   Authorization: 授权签名
 *   Content-Length: 文件长度
 *   Content-Type:   文件类型
 *   Content-MD5:    文件Hash
 *
 * Request Headers
 *   None
 *
 * Request Elements
 *   RetCode：  错误代码
 *   ErrMsg:    错误信息
 *
 * Request Headers
 *   Content-Type:
 *   Content-Length:
 *   ETag:   文件哈希值
 *   X-SessionId:   会话ID
 */
public class PutObjectRequest
        extends UObjectRequest
{
    private String objectKey;
    private InputStream objectStream;
    private Long objectLength;
    private String objectType;
    private String objectMd5;

    private UObjectMetadata newObjectMetadata;

    public PutObjectRequest(UFileCredentials credentials, String bucketName,
            String objectKey, InputStream objectStream, Long objectLength)
    {
        super(credentials, bucketName);
        this.objectKey = objectKey;
        this.objectStream = objectStream;
        this.objectLength = objectLength;
    }

    public PutObjectRequest(UFileCredentials credentials, String bucketName,
            String objectKey, InputStream objectStream, Long objectLength, String objectType)
    {
        super(credentials, bucketName);
        this.objectKey = objectKey;
        this.objectStream = objectStream;
        this.objectLength = objectLength;
        this.objectType = objectType;
    }

    public PutObjectRequest(UFileCredentials credentials, String bucketName,
            String objectKey, InputStream objectStream, Long objectLength, String objectType, String objectMd5)
    {
        super(credentials, bucketName);
        this.objectKey = objectKey;
        this.objectStream = objectStream;
        this.objectLength = objectLength;
        this.objectType = objectType;
        this.objectMd5 = objectMd5;
    }

    @Override
    public HttpUriRequest createHttpRequest() throws UFileClientException
    {
        try {
            String uri = "http://"
                    + getBucketName() + getCredentials().getProxySuffix()
                    + "/" + URLEncoder.encode(objectKey, "UTF-8");
            URIBuilder builder = new URIBuilder(uri);
            HttpPut request = new HttpPut(builder.build());
            if (objectType != null) {
                request.addHeader("Content-Type", objectType);
            }
            if (objectMd5 != null) {
                request.addHeader("Content-MD5", objectMd5);
            }
            InputStreamEntity reqEntity = new InputStreamEntity(objectStream, objectLength);
            request.setEntity(reqEntity);
            return request;
        }
        catch (UnsupportedEncodingException e) {
            throw new UFileClientException("URI Encode Error.", e);
        }
        catch (URISyntaxException e) {
            throw new UFileClientException("URI Syntax Error.", e);
        }
    }

    @Override
    public void onSuccess(JsonObject response, Header[] headers, InputStream content) throws UFileServiceException
    {
        newObjectMetadata = new UObjectMetadata(headers);
    }

    public UObjectMetadata getNewObjectMetadata()
    {
        return this.newObjectMetadata;
    }
}
