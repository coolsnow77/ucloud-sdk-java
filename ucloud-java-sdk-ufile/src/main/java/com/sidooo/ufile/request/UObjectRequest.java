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

import com.sidooo.ufile.UFileRegion;
import com.sidooo.ufile.exception.UFileServiceException;

import java.io.InputStream;

public abstract class UObjectRequest
        extends URequest
{
    public static final String DATE = "Date";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_MD5 = "Content-MD5";
    public static final String AUTHORIZATION = "Authorization";

    private String filePath;

    /**
     * 操作的对象Bucket
     */
    private final String bucketName;

    /**
     * 操作的对象Key
     */
    private String objectKey;

    /**
     * 准备上传的对象数据流
     */
    private InputStream objectStream;

    /**
     * 准备上传的对象数据流长度
     */
    private Long objectStreamLength;

    private InputStream contentStream;

    private Long contentLength = Long.valueOf(-1);

    public UObjectRequest(HttpType httpType, UFileRegion region, String bucketName)
    {
        super(httpType, region);
        this.bucketName = bucketName;
    }

    public String getBucketName()
    {
        return this.bucketName;
    }

    public String getObjectKey()
    {
        return objectKey;
    }

    public void setObjectKey(String key)
    {
        this.objectKey = key;
    }

    public String getAuthorization()
    {
        return this.getHeader(AUTHORIZATION);
    }

    public void setAuthorization(String authorization)
    {
        this.addHeader(AUTHORIZATION, authorization);
    }

    public void setContentLength(Long contentLength)
    {
        this.contentLength = contentLength;
    }

    public Long getContentLength()
    {
        return this.contentLength;
    }

    public InputStream getContentStream()
    {
        return this.contentStream;
    }

    public void setContentStream(InputStream content)
    {
        this.contentStream = content;
    }

    public String getContentType()
    {
        String contentType = getHeader(CONTENT_TYPE);
        return contentType == null ? "" : contentType;
//        else if (this.filePath != null) {
//            String contentType = UFileRequest.mm.getMime(this.filePath);
//            this.setContentType(contentType);
//            return contentType;
//        }
//        else {
//            return "";
//        }
    }

    public String getContentMD5()
    {
        String contentMd5 = getHeader(CONTENT_MD5);
        return contentMd5 == null ? "" : contentMd5;
    }

    public String getDate()
    {
        String date = getHeader(DATE);
        return date == null ? "" : date;
    }

    public InputStream getObjectStream()
    {
        return this.objectStream;
    }

    public void setObjectStream(InputStream stream)
    {
        this.objectStream = stream;
    }

    public Long getObjectStreamLength()
    {
        return this.objectStreamLength;
    }

    public void setObjectStreamLength(Long length)
    {
        this.objectStreamLength = length;
    }

    public abstract Object execute(ObjectExecutor executor)
            throws UFileServiceException;
}
