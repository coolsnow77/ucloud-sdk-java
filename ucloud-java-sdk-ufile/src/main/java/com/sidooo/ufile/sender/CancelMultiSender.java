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
package com.sidooo.ufile.sender;

import com.sidooo.ufile.UFile;
import com.sidooo.ufile.UFileCredentials;
import com.sidooo.ufile.UFileRequest;
import com.sidooo.ufile.UFileResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class CancelMultiSender
        implements Sender
{
    private String uploadId;

    public CancelMultiSender(String uploadId)
    {
        this.uploadId = uploadId;
    }

    @Override
    public void makeAuth(UFile ufile, UFileRequest request)
    {
        UFileCredentials credentials = ufile.getCredentials();
        String httpMethod = "DELETE";
        String contentMD5 = request.getContentMD5();
        String contentType = "";
        String date = request.getDate();
        String canonicalUCloudHeaders = request.spliceCanonicalHeaders();
        String canonicalResource = "/" + request.getBucketName() + "/" + request.getKey();
        String stringToSign = httpMethod + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n" +
                canonicalUCloudHeaders + canonicalResource;
        credentials.makeAuth(stringToSign, request);
    }

    @Override
    public UFileResponse send(UFile ufile, UFileRequest request)
    {
        UFileCredentials credentials = ufile.getCredentials();
        String uri = "";
        try {
//            uri = "http://" + request.getBucketName() + UFileConfig.getInstance().getProxySuffix()
//                    + "/" + URIUtil.encodeWithinPath(request.getKey(), UTF8)
//                    + "?" + URIUtil.encodeWithinQuery("uploadId", UTF8)
//                    + "=" + URIUtil.encodeWithinQuery(this.uploadId, UTF8);
            uri = "http://" + request.getBucketName() + credentials.getProxySuffix()
                    + "/" + URLEncoder.encode(request.getKey(), UTF8)
                    + "?" + URLEncoder.encode("uploadId", UTF8)
                    + "=" + URLEncoder.encode(this.uploadId, UTF8);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpDelete postMethod = new HttpDelete(uri);

        HttpEntity resEntity = null;
        try {
            Map<String, String> headers = request.getHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    postMethod.setHeader(entry.getKey(), entry.getValue());
                }
            }

            UFileResponse ufileResponse = new UFileResponse();
            HttpResponse httpResponse = ufile.getHttpClient().execute(postMethod);

            resEntity = httpResponse.getEntity();
            ufileResponse.setStatusLine(httpResponse.getStatusLine());
            ufileResponse.setHeaders(httpResponse.getAllHeaders());

            if (resEntity != null) {
                ufileResponse.setContentLength(resEntity.getContentLength());
                ufileResponse.setContent(resEntity.getContent());
            }
            return ufileResponse;
        }
        catch (Exception e) {
            e.printStackTrace();

            try {
                if (resEntity != null && resEntity.getContent() != null) {
                    resEntity.getContent().close();
                }
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
}
