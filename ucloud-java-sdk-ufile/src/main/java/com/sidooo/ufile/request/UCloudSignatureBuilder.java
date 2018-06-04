package com.sidooo.ufile.request;

import com.sidooo.ufile.UFileCredentials;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

public class UCloudSignatureBuilder
{
    public static String getHttpString(UBucketRequest request, UFileCredentials credentials)
    {
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("PublicKey", credentials.getPublicKey());
        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        String httpString = "https://api.ucloud.cn/?";
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            httpString += entry.getKey() + "=" + URLEncoder.encode(entry.getValue()) + "&";
        }

        String signture = getSignature(request, credentials);
        httpString += "Signature=" + signture;
        return httpString;
    }

    public static String getAPIString(UBucketRequest request, UFileCredentials credentials)
    {
        // 将字符串进行排序
        Map<String, String> sortedMap = new TreeMap<String, String>();
        sortedMap.put("PublicKey", credentials.getPublicKey());
        for (Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        String result = "";
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            result += entry.getKey() + entry.getValue();
        }

        return result + credentials.getPrivateKey();
    }

    public static String getSignature(UBucketRequest request, UFileCredentials credentials)
    {
        String signatureString  = getAPIString(request, credentials);

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
}
