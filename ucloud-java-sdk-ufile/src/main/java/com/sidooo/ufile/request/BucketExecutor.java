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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class BucketExecutor
        extends AbstractExcector
{
    protected static final String UCLOUD_API_HOST = "api.ucloud.cn";

    public BucketExecutor(UFileCredentials credentials)
    {
        super(credentials);
    }

    public String getHttpString(UBucketRequest request)
    {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("Action", request.getActionName());
        sortedMap.put("PublicKey", getCredentials().getPublicKey());
        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        String httpString = "https://api.ucloud.cn/?";
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            httpString += entry.getKey() + "=" + URLEncoder.encode(entry.getValue()) + "&";
        }

        String signture = updateSignature(request);
        httpString += "Signature=" + signture;
        return httpString;
    }

    public String updateSignature(UBucketRequest request)
    {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("PublicKey", getCredentials().getPublicKey());
        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
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

        return hashKey;
    }

    public void execute(UBucketRequest request)
            throws UFileClientException
    {
        // 计算API请求的签名
        request.addHeader("PublicKey", getCredentials().getPublicKey());
        String signature = updateSignature(request);
        request.addHeader("Signature", signature);

        // 构建Http URI
        HttpUriRequest httpRequest = null;
        try {
            URIBuilder builder = new URIBuilder("https://" + UCLOUD_API_HOST);
            for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
                builder.setParameter(entry.getKey(), entry.getValue());
            }

            switch (request.getHttpType()) {
                case GET:
                    httpRequest = new HttpGet(builder.build());
                case POST:
                    httpRequest = new HttpPost(builder.build());
                case DELETE:
                    httpRequest = new HttpDelete(builder.build());
                case PUT:
                    httpRequest = new HttpPut(builder.build());
            }
        }
        catch (URISyntaxException e) {
            throw new UFileClientException("Create Http Request Error.", e);
        }

        try {
            HttpResponse httpResponse = httpClient.execute(httpRequest);
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
                request.onSuccess(json, null, null);
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
