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

import org.apache.http.util.MimeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class UFileRequest
        implements Cloneable
{
    public static final String USER_AGENT = "User-Agent";
    public static final String JAVA_SDK_VER = "JavaSDK/1.0.10;JavaVersion/" + System.getProperty("java.version");

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String DATE = "Date";

    private static final String CANONICAL_PREFIX = "X-UCloud";

    private String bucketName;
    private String key;
    private String filePath;
    private InputStream inputStream;
    private long contentLength = -1;

    private Map<String, String> headers = new HashMap<String, String>();

    public static MimeMap mm = MimeMap.getInstatnce();

    public UFileRequest()
    {
        this.headers.put(USER_AGENT, JAVA_SDK_VER);
    }

    @Override
    public UFileRequest clone()
            throws CloneNotSupportedException
    {
        UFileRequest newOne = new UFileRequest();
        newOne.bucketName = new String(this.bucketName);
        newOne.key = new String(this.key);
        newOne.filePath = new String(this.filePath);
        newOne.setHeaders(new HashMap<String, String>());
        for (Entry<String, String> entry : this.headers.entrySet()) {
            newOne.headers.put(entry.getKey(), entry.getValue());
        }
        return newOne;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public void setFilePath(String filePath)
    {
        this.filePath = filePath;
        try {
            this.setInputStream(new FileInputStream(filePath));
            this.setContentLength(new File(filePath).length());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream)
    {
        this.inputStream = inputStream;
    }

    public void setContentLength(long contentLength)
    {
        this.contentLength = contentLength;
    }

    public long getContentLength()
    {
        return this.contentLength;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public String getKey()
    {
        return key;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public void setAuthorization(String authorization)
    {
        this.headers.put(UFileRequest.AUTHORIZATION, authorization);
    }

    public String getAuthorization()
    {
        return this.headers.get(UFileRequest.AUTHORIZATION);
    }

    public void addHeader(String key, String value)
    {
        this.headers.put(key, value);
    }

    public void setContentType(String contentType)
    {
        this.headers.put(CONTENT_TYPE, contentType);
    }

    public String getContentType()
    {
        if (this.headers.containsKey(CONTENT_TYPE)) {
            return this.headers.get(CONTENT_TYPE);
        }
        else if (this.filePath != null) {
            String contentType = UFileRequest.mm.getMime(this.filePath);
            this.setContentType(contentType);
            return contentType;
        }
        else {
            return "";
        }
    }

    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public String getContentMD5()
    {
        if (this.headers.containsKey(CONTENT_MD5)) {
            return this.headers.get(CONTENT_MD5);
        }
        return "";
    }

    public void setContentMD5(String contentMD5)
    {
        this.headers.put(CONTENT_MD5, contentMD5);
    }

    public void setDate(String date)
    {
        this.headers.put(DATE, date);
    }

    public String getDate()
    {
        if (this.headers.containsKey(DATE)) {
            return this.headers.get(DATE);
        }
        return "";
    }

    public String spliceCanonicalHeaders()
    {
        Map<String, String> headers = this.getHeaders();
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
}
