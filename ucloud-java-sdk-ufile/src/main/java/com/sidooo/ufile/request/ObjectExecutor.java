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
import com.google.gson.JsonParser;
import com.sidooo.ucloud.Credentials;
import com.sidooo.ufile.UFileHeaders;
import com.sidooo.ufile.UFileSignatureBuilder;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
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

public final class ObjectExecutor
        extends AbstractExcector
{
    public static final String AUTHORIZATION = "Authorization";

    public ObjectExecutor(Credentials credentials)
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

    public UResponse execute(UObjectRequest request, String objectKey)
            throws UFileClientException
    {
        // 生成Http请求
        HttpUriRequest httpRequest = null;
        try {
            String uri = null;
            if (request.getObjectKey() != null) {
                uri = "http://"
                        + request.getBucketName() + "." + request.getRegion().getValue() + ".ufileos.com"
                        + "/" + URLEncoder.encode(request.getObjectKey(), "UTF-8");
            }
            else {
                uri = "http://"
                        + request.getBucketName() + "." + request.getRegion().getValue() + ".ufileos.com"
                        + "/?list";
            }
            URIBuilder builder = new URIBuilder(uri);
            for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
                builder.setParameter(entry.getKey(), entry.getValue());
            }
            switch (request.getHttpType()) {
                case GET:
                    httpRequest = new HttpGet(builder.build());
                    break;
                case POST:
                    httpRequest = new HttpPost(builder.build());
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(builder.build());
                    break;
                case PUT:
                    HttpPut put = new HttpPut(builder.build());
                    if (request.getObjectStream() != null) {
                        InputStreamEntity entity = new InputStreamEntity(
                                request.getObjectStream(), request.getObjectStreamLength());
                        put.setEntity(entity);
                    }
                    httpRequest = put;
                    break;
                case HEAD:
                    httpRequest = new HttpHead(builder.build());
            }

            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                httpRequest.addHeader(entry.getKey(), entry.getValue());
            }
            // 计算API的签名
            String signature = UFileSignatureBuilder.getSignature(request, objectKey, getCredentials());
            httpRequest.addHeader(AUTHORIZATION, signature);
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
                    return new UResponse(httpResponse.getAllHeaders());
                }
                else {
                    if (resEntity.getContentLength() <= 0) {
                        return new UResponse(httpResponse.getAllHeaders());
                    }
                    String contentType = getContentType(httpResponse.getAllHeaders());
                    if (contentType.equals("text/plain") || contentType.equals("application/json")) {
                        Reader reader = new InputStreamReader(resEntity.getContent());
                        JsonObject json = (new JsonParser()).parse(reader).getAsJsonObject();
                        if (!json.has("RetCode")) {
                            // List Objects请求时没有RetCode
                            return new UResponse(json, httpResponse.getAllHeaders());
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
                            return new UResponse(json, httpResponse.getAllHeaders());
                        }
                    }
                    else {
                        // 二进制的数据
                        return new UResponse(httpResponse.getAllHeaders(), cloneContent(resEntity.getContent()));
                    }
                }
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 204) {
                return new UResponse(httpResponse.getAllHeaders());
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 206) {
                // 请求对象部分数据
                HttpEntity resEntity = httpResponse.getEntity();
                if (resEntity == null) {
                    return new UResponse(httpResponse.getAllHeaders());
                }
                else {
                    return new UResponse(httpResponse.getAllHeaders(), cloneContent(resEntity.getContent()));
                }
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
