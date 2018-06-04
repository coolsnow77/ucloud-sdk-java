package com.sidooo.ufile.request;

import com.sidooo.ufile.HmacSHA1;
import com.sidooo.ufile.UFileCredentials;

import java.util.Map;
import java.util.TreeMap;

public class UFileSignatureBuilder
{
    private static final String CANONICAL_PREFIX = "X-UCloud";

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
