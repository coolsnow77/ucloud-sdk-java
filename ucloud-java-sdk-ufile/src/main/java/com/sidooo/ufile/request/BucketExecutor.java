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
import com.sidooo.ucloud.Credentials;
import com.sidooo.ucloud.UCloudSignatureBuilder;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;
import java.util.Map;

public final class BucketExecutor
        extends AbstractExcector
{
    protected static final String UCLOUD_API_HOST = "api.ucloud.cn";

    public BucketExecutor(Credentials credentials)
    {
        super(credentials);
    }

    public UResponse execute(UBucketRequest request)
            throws UFileClientException
    {
        // 计算API请求的签名
        String signature = UCloudSignatureBuilder.getSignature(request, getCredentials());

        // 构建Http URI
        HttpUriRequest httpRequest = null;
        try {
            URIBuilder builder = new URIBuilder("https://" + UCLOUD_API_HOST + "/");
            for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
                builder.setParameter(entry.getKey(), entry.getValue());
            }
            builder.setParameter("PublicKey", getCredentials().getPublicKey());
            builder.setParameter("Signature", signature);

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
                    httpRequest = new HttpPut(builder.build());
                    break;
                case HEAD:
                    httpRequest = new HttpHead(builder.build());
                    break;
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
                    throw new UFileServiceException("Return Code error: " + content);
                }
                json.remove("RetCode");

                if (!json.has("Action")) {
                    throw new UFileServiceException("Action missing.");
                }
                String action = json.get("Action").getAsString();
                if (!action.equals(request.getActionName() + "Response")) {
                    throw new UFileServiceException("Action mismatch: " + action);
                }
                json.remove("Action");
                return new UResponse(json);
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
