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
package com.sidooo.ufile;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

public class DownloadUrl
{
    public static final String UCLOUD_PROXY_SUFFIX = ".ufile.ucloud.cn";
    public static final String UTF8 = "UTF-8";

    public DownloadUrl()
    {
    }

    public String getUrl(UFile ufile, UFileRequest request, int ttl, boolean isPrivate)
    {
        UFileCredentials credentials = ufile.getCredentials();
        if (!isPrivate) {
            try {
                return "http://" + request.getBucketName() + credentials.getDownloadProxySuffix()
                        + URLEncoder.encode("/" + request.getKey(), UTF8);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return makeDownloadUrl(ufile, request, ttl);
    }

    public String makeSignature(UFile ufile, UFileRequest request, long expires)
    {
        UFileCredentials credentials = ufile.getCredentials();

        String httpMethod = "GET";
        String contentMD5 = request.getContentMD5();
        String contentType = request.getContentType();
        String canonicalUCloudHeaders = request.spliceCanonicalHeaders();
        String canonicalResource = "/" + request.getBucketName() + "/" + request.getKey();
        String expireStr = "";
        if (expires > 0) {
            expireStr = String.valueOf(expires);
        }
        String stringToSign = httpMethod + "\n" + contentMD5 + "\n" + contentType + "\n" + expireStr + "\n" +
                canonicalUCloudHeaders + canonicalResource;

        String signature = new HmacSHA1().sign(credentials.getPrivateKey(), stringToSign);
        return signature;
    }

    public String makeDownloadUrl(UFile ufile, UFileRequest request, int ttl)
    {
        UFileCredentials credentials = ufile.getCredentials();

        String expireStr = "";
        long expires = 0;
        if (ttl > 0) {
            expires = (new Date().getTime() / 1000) + ttl;
            expireStr = String.valueOf(expires);
        }
        String signature = makeSignature(ufile, request, expires);
        String url = "";
        try {
            url = "http://" + request.getBucketName() + credentials.getDownloadProxySuffix()
                    + URLEncoder.encode("/" + request.getKey(), UTF8)
                    + "?" + URLEncoder.encode("UCloudPublicKey", UTF8)
                    + "=" + URLEncoder.encode(credentials.getPublicKey(), UTF8)
                    + "&" + URLEncoder.encode("Expires", UTF8)
                    + "=" + URLEncoder.encode(expireStr, UTF8)
                    + "&" + URLEncoder.encode("Signature", UTF8)
                    + "=" + URLEncoder.encode(signature, UTF8);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
}
