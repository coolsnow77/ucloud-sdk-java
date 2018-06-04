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
import com.google.gson.JsonParser;
import com.sidooo.ufile.HmacSHA1;
import com.sidooo.ufile.UFileCredentials;
import com.sidooo.ufile.UFileHeaders;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

public class ObjectExecutor
        extends AbstractExcector
{
    public static final String AUTHORIZATION = "Authorization";
    private static final String CANONICAL_PREFIX = "X-UCloud";

    public ObjectExecutor(UFileCredentials credentials)
    {
        super(credentials);
    }

    private String getContentType(Header[] headers)
    {
        for (Header header : headers) {
            if (header.getName().equals(UFileHeaders.CONTENT_TYPE)) {
                return header.getValue();
            }
        }
        return null;
    }

    private InputStream cloneContent(InputStream stream)
            throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Fake code simulating the copy
        // You can generally do better with nio if you need...
        // And please, unlike me, do something about the Exceptions :D
        byte[] buffer = new byte[1024];
        int len;
        while ((len = stream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();

        // Open new InputStreams using the recorded bytes
        // Can be repeated as many times as you wish
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public String spliceCanonicalHeaders(UObjectRequest request)
    {
        Map<String, String> headers = request.getHeaders();
        Map<String, String> sortedMap = new TreeMap<String, String>();

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().startsWith(CANONICAL_PREFIX)) {
                    sortedMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
            String result = "";
            for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
                result += entry.getKey() + ":" + entry.getValue() + "\n";
            }
            return result;
        }
        else {
            return "";
        }
    }

    private String updateSignature(UObjectRequest request, String objectkey)
    {
        String contentMD5 = request.getContentMD5();
        String contentType = request.getContentType();
        String date = request.getDate();
        String canonicalizedUcloudHeaders = spliceCanonicalHeaders(request);
        String canonicalizedResource = "/" + request.getBucketName() + "/" + objectkey;
        String stringToSign = request.getHttpType() + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n" +
                canonicalizedUcloudHeaders + canonicalizedResource;
        String signature = new HmacSHA1().sign(getCredentials().getPrivateKey(), stringToSign);
        return "UCloud" + " " + getCredentials().getPublicKey() + ":" + signature;
    }

    public void execute(UObjectRequest request, String objectKey)
            throws UFileClientException
    {
        // 计算API的签名
        String signature = updateSignature(request, objectKey);

        // 生成Http请求
        HttpUriRequest httpRequest = null;
        try {
            String uri = null;
            if (request.getObjectKey() != null) {
                uri = "http://"
                        + request.getBucketName() + getCredentials().getProxySuffix()
                        + "/" + URLEncoder.encode(request.getObjectKey(), "UTF-8");
            }
            else {
                uri = "http://"
                        + request.getBucketName() + getCredentials().getProxySuffix()
                        + "/?list";
            }
            URIBuilder builder = new URIBuilder(uri);
            switch (request.getHttpType()) {
                case GET:
                    httpRequest = new HttpGet(builder.build());
                    break;
                case POST:
                    httpRequest = new HttpPost(builder.build());
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(builder.build());
                case PUT:
                    HttpPut put = new HttpPut(builder.build());
                    if (request.getObjectStream() != null) {
                        InputStreamEntity entity = new InputStreamEntity(
                                request.getObjectStream(), request.getObjectStreamLength());
                        put.setEntity(entity);
                    }
                    httpRequest = put;
            }

            for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
                httpRequest.addHeader(entry.getKey(), entry.getValue());
            }
            httpRequest.addHeader(AUTHORIZATION, updateSignature(request, objectKey));
        }
        catch (UnsupportedEncodingException e) {
            throw new UFileClientException("URI Encode Error.", e);
        }
        catch (URISyntaxException e) {
            throw new UFileClientException("URI Syntax Error.", e);
        }

        CloseableHttpResponse httpResponse;
        try {
            httpResponse = httpClient.execute(httpRequest);
        }
        catch (Exception e) {
            throw new UFileClientException("Http Client Execute Failed.", e);
        }

        try {
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity resEntity = httpResponse.getEntity();
                if (resEntity == null) {
                    request.onSuccess(null, httpResponse.getAllHeaders(), null);
                }
                else {
                    if (resEntity.getContentLength() <= 0) {
                        request.onSuccess(null, httpResponse.getAllHeaders(), null);
                        return;
                    }
                    String contentType = getContentType(httpResponse.getAllHeaders());
                    if (contentType.equals("text/plain") || contentType.equals("application/json")) {
                        Reader reader = new InputStreamReader(resEntity.getContent());
                        JsonObject json = (new JsonParser()).parse(reader).getAsJsonObject();
                        if (!json.has("RetCode")) {
//                            throw new UFileServiceException("RetCode missing.");
                            // List Objects请求时没有RetCode
                            request.onSuccess(json, httpResponse.getAllHeaders(), null);
                            return;
                        }
                        Long returnCode = json.get("RetCode").getAsLong();
                        if (returnCode != 0) {
                            if (json.has("ErrMsg")) {
                                String errorMessage = json.get("ErrMsg").getAsString();
                                throw new UFileServiceException(returnCode, errorMessage);
                            }
                            throw new UFileServiceException("Return Code: " + returnCode);
                        }
                        else {
                            json.remove("RetCode");
                            if (json.has("ErrMsg")) {
                                json.remove("ErrMsg");
                            }
                            request.onSuccess(json, httpResponse.getAllHeaders(), null);
                        }
                    }
                    else {
                        // 二进制的数据
                        request.onSuccess(null, httpResponse.getAllHeaders(), cloneContent(resEntity.getContent()));
                    }
                }
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 204) {
                request.onSuccess(null, httpResponse.getAllHeaders(), null);
            }
            else {
                // 服务器返回错误
                HttpEntity resEntity = httpResponse.getEntity();
                if (resEntity != null) {
                    String content = getContentAsString(resEntity.getContent());
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(content).getAsJsonObject();
                    if (!json.has("RetCode")) {
                        throw new UFileServiceException("RetCode missing.");
                    }
                    Long returnCode = json.get("RetCode").getAsLong();
                    if (returnCode != 0) {
                        if (json.has("ErrMsg")) {
                            String errorMessage = json.get("ErrMsg").getAsString();
                            throw new UFileServiceException(returnCode, errorMessage);
                        }
                        throw new UFileServiceException("Return Code: " + returnCode);
                    }
                    else {
                        throw new UFileServiceException(returnCode);
                    }
                }
                else {
                    throw new UFileServiceException("Http Status Code:" + httpResponse.getStatusLine().getStatusCode());
                }
            }
        }
        catch (Exception e) {
            throw new UFileClientException(e);
        }
        finally {
            try {
                httpResponse.close();
            }
            catch (IOException e) {
                throw new UFileClientException(e);
            }
        }
    }
}
