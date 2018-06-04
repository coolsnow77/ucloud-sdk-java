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

import com.sidooo.ufile.HmacSHA1;
import com.sidooo.ufile.UFileCredentials;

import java.util.Map;
import java.util.TreeMap;

public class UFileSignatureBuilder
{
    private static final String CANONICAL_PREFIX = "X-UCloud";

    private UFileSignatureBuilder()
    {
    }

    public static String spliceCanonicalHeaders(UObjectRequest request)
    {
        Map<String, String> headers = request.getHeaders();
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

    public static String getSignature(UObjectRequest request, String objectkey, UFileCredentials credentials)
    {
        String contentMD5 = request.getContentMD5();
        String contentType = request.getContentType();
        String date = request.getDate();
        String canonicalizedUcloudHeaders = spliceCanonicalHeaders(request);
        String canonicalizedResource = "/" + request.getBucketName() + "/" + objectkey;
        String stringToSign = request.getHttpType() + "\n"
                + contentMD5 + "\n"
                + contentType + "\n"
                + date + "\n"
                + canonicalizedUcloudHeaders
                + canonicalizedResource;
        String signature = new HmacSHA1().sign(credentials.getPrivateKey(), stringToSign);
        return "UCloud" + " " + credentials.getPublicKey() + ":" + signature;
    }
}
