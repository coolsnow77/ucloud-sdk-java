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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.SortedMap;

public class FinishMultiSender
        implements Sender
{
    private String uploadId;
    private SortedMap<Integer, String> eTags;
    private String newKey;

    public FinishMultiSender(String uploadId, SortedMap<Integer, String> eTags,
            String newKey)
    {
        this.uploadId = uploadId;
        this.eTags = eTags;
        this.newKey = newKey;
    }

    @Override
    public void makeAuth(UFile ufile, UFileRequest request)
    {
        UFileCredentials credentials = ufile.getCredentials();
        String httpMethod = "POST";
        String contentMD5 = request.getContentMD5();
        /* Post StringEntity default Content-Type */
        String contentType = "text/plain; charset=ISO-8859-1";
        String date = request.getDate();
        String canonicalUCloudHeaders = request.spliceCanonicalHeaders();
        String canonicalResource = "/" + request.getBucketName() + "/"
                + request.getKey();
        String stringToSign = httpMethod + "\n" + contentMD5 + "\n"
                + contentType + "\n" + date + "\n" + canonicalUCloudHeaders
                + canonicalResource;
        credentials.makeAuth(stringToSign, request);
    }

    @Override
    public UFileResponse send(UFile ufile, UFileRequest request)
    {
        UFileCredentials credentials = ufile.getCredentials();
        String uri = "";
        try {
            uri = "http://" + request.getBucketName() + credentials.getProxySuffix()
                    + "/" + URLEncoder.encode(request.getKey(), UTF8)
                    + "?" + URLEncoder.encode("uploadId", UTF8)
                    + "=" + URLEncoder.encode(this.uploadId, UTF8)
                    + "&" + URLEncoder.encode("newKey", UTF8)
                    + "=" + URLEncoder.encode(this.newKey, UTF8);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpPost postMethod = new HttpPost(uri);

        HttpEntity resEntity = null;
        try {
            Map<String, String> headers = request.getHeaders();
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    postMethod.setHeader(entry.getKey(), entry.getValue());
                }
            }

            String serialEtags = serialEtagsWithComma(eTags);

            StringEntity reqEntity = new StringEntity(serialEtags);

            postMethod.setEntity(reqEntity);
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

    private String serialEtagsWithComma(SortedMap<Integer, String> eTags)
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> entry : eTags.entrySet()) {
            sb.append(entry.getValue());
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
