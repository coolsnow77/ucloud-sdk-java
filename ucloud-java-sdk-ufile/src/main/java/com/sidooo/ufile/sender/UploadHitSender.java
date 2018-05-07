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
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

public class UploadHitSender
        implements Sender
{
    static final int BLOCK_SIZE = 4 * 1024 * 1024;

    public UploadHitSender()
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
        String canonicalUCloudHeaders = request.spliceCanonicalHeaders();
        String canonicalUCloudResource = "/" + request.getBucketName() + "/"
                + request.getKey();
        String stringToSign = httpMethod + "\n" + contentMD5 + "\n"
                + contentType + "\n" + date + "\n" + canonicalUCloudHeaders
                + canonicalUCloudResource;
        credentials.makeAuth(stringToSign, request);
    }

    @Override
    public UFileResponse send(UFile ufile, UFileRequest request)
    {
        UFileCredentials credentials = ufile.getCredentials();

        String hash = calcSha1(request.getFilePath());
        String uri = "";
        try {
            uri = "http://" + request.getBucketName() + credentials.getProxySuffix()
                    + "/" + URLEncoder.encode("uploadhit", UTF8)
                    + "?" + URLEncoder.encode("Hash", UTF8)
                    + "=" + URLEncoder.encode(hash, UTF8)
                    + "&" + URLEncoder.encode("FileName", UTF8)
                    + "=" + URLEncoder.encode(request.getKey(), UTF8)
                    + "&" + URLEncoder.encode("FileSize", UTF8)
                    + "=" + URLEncoder.encode("" + (new File(request.getFilePath()).length()), UTF8);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpPost postMethod = new HttpPost(uri);

        HttpEntity resEntity = null;
        try {
            Map<String, String> headers = request.getHeaders();
            if (headers != null) {
                for (Entry<String, String> entry : headers.entrySet()) {
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

    private String calcSha1(String filePath)
    {
        File file = new File(filePath);
        long fileLength = file.length();

        if (fileLength <= 4 * 1024 * 1024) {
            return smallFileSha1(file);
        }
        else {
            return largeFileSha1(file);
        }
    }

    private String largeFileSha1(File file)
    {
        InputStream inputStream = null;
        try {
            MessageDigest gsha1 = MessageDigest.getInstance("SHA1");
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int nRead = 0;
            int block = 0;
            int count = 0;
            while ((nRead = inputStream.read(buffer)) != -1) {
                count += nRead;
                sha1.update(buffer, 0, nRead);
                if (BLOCK_SIZE == count) {
                    gsha1.update(sha1.digest());
                    sha1 = MessageDigest.getInstance("SHA1");
                    block++;
                    count = 0;
                }
            }
            if (count != 0) {
                gsha1.update(sha1.digest());
                block++;
            }
            byte[] digest = gsha1.digest();

            byte[] blockBytes = ByteBuffer.allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN).putInt(block).array();

            byte[] result = ByteBuffer.allocate(4 + digest.length)
                    .put(blockBytes, 0, blockBytes.length)
                    .put(digest, 0, digest.length).array();

            return new String(Base64.encodeBase64URLSafe(result));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String smallFileSha1(File file)
    {
        InputStream inputStream = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int nRead = 0;
            while ((nRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, nRead);
            }
            byte[] digest = md.digest();
            byte[] blockBytes = ByteBuffer.allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN).putInt(1).array();
            byte[] result = ByteBuffer.allocate(4 + digest.length)
                    .put(blockBytes, 0, 4).put(digest, 0, digest.length)
                    .array();
            return new String(Base64.encodeBase64URLSafe(result));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
