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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

public class PostSender
        implements Sender
{
    public PostSender()
    {
    }

    @Override
    public void makeAuth(UFile ufile, UFileRequest request)
    {
        UFileCredentials credentials = ufile.getCredentials();
        String httpMethod = "POST";
        String contentMD5 = request.getContentMD5();
        String contentType = request.getContentType();
        String date = request.getDate();
        String canonicalizedUcloudHeaders = request.spliceCanonicalHeaders();
        String canonicalizedResource = "/" + request.getBucketName() + "/" + request.getKey();
        String stringToSign = httpMethod + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n" +
                canonicalizedUcloudHeaders + canonicalizedResource;
        credentials.makeAuth(stringToSign, request);
    }

    @Override
    public UFileResponse send(UFile ufile, UFileRequest request)
    {
        UFileCredentials credentials = ufile.getCredentials();
        String uri = "http://" + request.getBucketName() + credentials.getProxySuffix() + "/";
        HttpPost postMethod = new HttpPost(uri);

        HttpEntity resEntity = null;
        try {
            File file = new File(request.getFilePath());
            Map<String, String> headers = request.getHeaders();
            if (headers != null) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    postMethod.setHeader(entry.getKey(), entry.getValue());
                }
            }

            ContentType requestContentType = ContentType.create(request.getContentType());
            FileBody fileBody = new FileBody(file, requestContentType, file.getName());
            ContentType textContentType = ContentType.create("text/plain", Charset.forName("UTF-8"));

            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("Authorization", new StringBody(request.getAuthorization(), textContentType))
                    .addPart("FileName", new StringBody(request.getKey(), textContentType))
                    .addPart("file", fileBody)
                    .build();

            postMethod.setEntity(entity);
            postMethod.setHeader(entity.getContentType());

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