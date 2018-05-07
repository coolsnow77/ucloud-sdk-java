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
import com.sidooo.ufile.UFileCredentials;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class UBucketRequest
        extends URequest
{
    protected static final String UCLOUD_API_HOST = "api.ucloud.cn";

    private final String actionName;

    // API Request Signature
    private String signature;

    private Map<String, String> parameters = new HashMap<String, String>();

    public UBucketRequest(UFileCredentials credentials, String actionName)
    {
        super(credentials);
        this.actionName = actionName;
    }

    /*
     * 创建准备发送到API服务器的URL请求
     */
    abstract HttpUriRequest createHttpRequest()
            throws UFileClientException;

    /*
     * API请求成功后的处理函数
     */
    abstract void onSuccess(JsonObject result)
            throws UFileServiceException;

    public void addParameter(String name, String value)
    {
        this.parameters.put(name, value);
    }

    public String getSignature()
    {
        return signature;
    }

    public void updateSignature()
    {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("PublicKey", getCredentials().getPublicKey());
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        String result = "";
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            result += entry.getKey() + entry.getValue();
        }

        String signatureString = result + getCredentials().getPrivateKey();

        String hashKey = null;
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("SHA-1");
            digest.update(signatureString.getBytes());
            byte[] messageDigest = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            hashKey = hexString.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        signature = hashKey;
    }

    public String getHttpString()
    {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("Action", this.actionName);
        sortedMap.put("PublicKey", getCredentials().getPublicKey());
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        String httpString = "https://api.ucloud.cn/?";
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            httpString += entry.getKey() + "=" + URLEncoder.encode(entry.getValue()) + "&";
        }

        updateSignature();
        httpString += "Signature=" + this.signature;
        return httpString;
    }

    public Map<String, String> getHeaders()
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("Action", this.actionName);
        headers.put("PublicKey", getCredentials().getPublicKey());
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            headers.put(entry.getKey(), URLEncoder.encode(entry.getValue()));
        }
        updateSignature();
        headers.put("Signature", this.signature);
        return headers;
    }

    public void execute(HttpClient httpClient)
            throws UFileClientException
    {
        try {
            HttpResponse httpResponse = httpClient.execute(createHttpRequest());
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                String content = getContentAsString(httpResponse.getEntity().getContent());
                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(content).getAsJsonObject();

                if (!json.has("RetCode")) {
                    throw new UFileServiceException("RetCode missing.");
                }
                Long returnCode = json.get("RetCode").getAsLong();
                if (returnCode != 0) {
                    throw new UFileServiceException("Return Code error.");
                }
                json.remove("RetCode");

                if (!json.has("Action")) {
                    throw new UFileServiceException("Action missing.");
                }
                String action = json.get("Action").getAsString();
                if (!action.equals("CreateBucketResponse")) {
                    throw new UFileServiceException("Action mismatch.");
                }
                json.remove("Action");
                onSuccess(json);
            }
            else {
                HttpEntity resEntity = httpResponse.getEntity();
                if (resEntity != null) {
                    String content = getContentAsString(resEntity.getContent());
                    JsonParser parser = new JsonParser();
                    JsonObject jsonResponse = parser.parse(content).getAsJsonObject();
                    if (!jsonResponse.has("RetCode")) {
                        throw new UFileServiceException("RetCode missing.");
                    }
                    Long returnCode = jsonResponse.get("RetCode").getAsLong();
                    if (returnCode != 0) {
                        throw new UFileServiceException("Return Code:" + returnCode);
                    }

                    if (jsonResponse.has("Message")) {
                        String message = jsonResponse.get("Message").getAsString();
                        throw new UFileServiceException(returnCode, message);
                    }
                    throw new UFileServiceException(returnCode);
                }
                else {
                    throw new UFileServiceException("Http Status Code:" + httpResponse.getStatusLine().getStatusCode());
                }
            }
        }
        catch (Exception e) {
            throw new UFileClientException(e);
        }
    }
}
