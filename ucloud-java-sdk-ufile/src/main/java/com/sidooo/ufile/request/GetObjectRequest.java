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
import com.sidooo.ufile.UObject;
import com.sidooo.ufile.UObjectMetadata;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/*
 *
 * Request Parameters
 *      None
 *
 * Request Headers
 *   Authorization : 下载请求的授权签名
 *   Range : bytes=<byte_range>   分片下载的文件范围
 *   If-Modified-Since : <timestamp>   只返回从某时修改过的文件， 否则返回304
 *
 * Response Headers
 *   Content-Type:  文件类型
 *   Content-Length: 文件长度
 *   Content-Range:  文件的范围
 *   ETag:  哈希值
 *   X-SessionId:  会话Id
 *
 * Response Elements
 *   RetCode:  错误代码
 *   ErrMsg:  错误提示
 */

public class GetObjectRequest
        extends UObjectRequest
{
    private String objectKey;
    private String objectRange;

    private UObject object;

    public GetObjectRequest(UFileCredentials credentials, String bucketName, String objectKey)
    {
        super(credentials, bucketName);
        this.objectKey = objectKey;
    }

    public GetObjectRequest(UFileCredentials credentials, String bucketName, String objectKey, String objectRange)
    {
        super(credentials, bucketName);
        this.objectKey = objectKey;
        this.objectRange = objectRange;
    }

    @Override
    HttpUriRequest createHttpRequest()
            throws UFileClientException
    {
        try {
            String uri = "http://"
                    + getBucketName() + getCredentials().getDownloadProxySuffix()
                    + "/" + URLEncoder.encode(objectKey, "UTF-8");
            URIBuilder builder = new URIBuilder(uri);
            HttpGet request = new HttpGet(builder.build());
            if (objectRange != null) {
                request.addHeader("Range", objectRange);
            }
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
    public void onSuccess(JsonObject response, Header[] headers, InputStream content)
            throws UFileServiceException
    {
        UObject object = new UObject();
        object.setKey(objectKey);
        object.setContent(content);
        UObjectMetadata objectMetadata = new UObjectMetadata(headers);
        object.setObjectMetadata(objectMetadata);
        object.setLengnth(objectMetadata.getContentLength());
        this.object = object;
    }

    public UObject getObject()
    {
        return this.object;
    }
}
