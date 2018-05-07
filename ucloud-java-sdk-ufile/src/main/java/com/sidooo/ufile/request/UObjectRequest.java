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
import com.sidooo.ufile.UFileRequest;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class UObjectRequest
        extends URequest
{
    private static final String CANONICAL_PREFIX = "X-UCloud";
    public static final String DATE = "Date";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_MD5 = "Content-MD5";

    private String filePath;
    private String bucketName;
    private InputStream contentStream;
    private Long contentLength = Long.valueOf(-1);
    private Map<String, String> headers = new HashMap<String, String>();

    public UObjectRequest(UFileCredentials credentials, String bucketName)
    {
        super(credentials);
        this.bucketName = bucketName;
    }

    /*
     * 创建准备发送到API服务器的URL请求
     */
    abstract HttpUriRequest createHttpRequest()
            throws UFileClientException;

    /*
     * API请求成功后的处理函数
     */
    abstract void onSuccess(JsonObject response, Header[] headers, InputStream content)
            throws UFileServiceException;

    public String getAuthorization()
    {
        return this.headers.get(UFileRequest.AUTHORIZATION);
    }

    public void setAuthorization(String authorization)
    {
        this.headers.put(UFileRequest.AUTHORIZATION, authorization);
    }

    public String getBucketName()
    {
        return this.bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public void setContentLength(Long contentLength)
    {
        this.contentLength = contentLength;
    }

    public Long getContentLength()
    {
        return this.contentLength;
    }

    public InputStream getContentStream()
    {
        return this.contentStream;
    }

    public void setContentStream(InputStream content)
    {
        this.contentStream = content;
    }

    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public String getContentType()
    {
        if (this.headers.containsKey(CONTENT_TYPE)) {
            return this.headers.get(CONTENT_TYPE);
        }
        else if (this.filePath != null) {
            String contentType = UFileRequest.mm.getMime(this.filePath);
            this.setContentType(contentType);
            return contentType;
        }
        else {
            return "";
        }
    }

    public void setContentType(String contentType)
    {
        this.headers.put(CONTENT_TYPE, contentType);
    }

    public String getContentMD5()
    {
        if (this.headers.containsKey(CONTENT_MD5)) {
            return this.headers.get(CONTENT_MD5);
        }
        return "";
    }

    public void setContentMD5(String contentMD5)
    {
        this.headers.put(CONTENT_MD5, contentMD5);
    }

    public String getDate()
    {
        if (this.headers.containsKey(DATE)) {
            return this.headers.get(DATE);
        }
        return "";
    }

    public void setDate(String date)
    {
        this.headers.put(DATE, date);
    }

    public String spliceCanonicalHeaders()
    {
        Map<String, String> headers = this.getHeaders();
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

    private String updateSignature(String httpMethod, String objectkey)
    {
        String contentMD5 = getContentMD5();
        String contentType = getContentType();
        String date = getDate();
        String canonicalizedUcloudHeaders = spliceCanonicalHeaders();
        String canonicalizedResource = "/" + this.bucketName + "/" + objectkey;
        String stringToSign = httpMethod + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n" +
                canonicalizedUcloudHeaders + canonicalizedResource;
        String signature = new HmacSHA1().sign(getCredentials().getPrivateKey(), stringToSign);
        return "UCloud" + " " + getCredentials().getPublicKey() + ":" + signature;
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

    public void execute(CloseableHttpClient httpClient, String objectKey)
            throws UFileClientException
    {
        CloseableHttpResponse httpResponse;
        try {
            HttpUriRequest httpRequest = createHttpRequest();
            httpRequest.addHeader(AUTHORIZATION, updateSignature(httpRequest.getMethod(), objectKey));
            httpResponse = httpClient.execute(httpRequest);
        }
        catch (Exception e) {
            throw new UFileClientException("Http Client Execute Failed.", e);
        }

        try {
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpEntity resEntity = httpResponse.getEntity();
                if (resEntity == null) {
                    onSuccess(null, httpResponse.getAllHeaders(), null);
                }
                else {
                    if (resEntity.getContentLength() <= 0) {
                        onSuccess(null, httpResponse.getAllHeaders(), null);
                        return;
                    }
                    String contentType = getContentType(httpResponse.getAllHeaders());
                    if (contentType.equals("text/plain") || contentType.equals("application/json")) {
                        Reader reader = new InputStreamReader(resEntity.getContent());
                        JsonObject json = (new JsonParser()).parse(reader).getAsJsonObject();
                        if (!json.has("RetCode")) {
//                            throw new UFileServiceException("RetCode missing.");
                            // List Objects请求时没有RetCode
                            onSuccess(json, httpResponse.getAllHeaders(), null);
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
                            onSuccess(json, httpResponse.getAllHeaders(), null);
                        }
                    }
                    else {
                        // 二进制的数据
                        onSuccess(null, httpResponse.getAllHeaders(), cloneContent(resEntity.getContent()));
                    }
                }
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 204) {
                onSuccess(null, httpResponse.getAllHeaders(), null);
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
