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
package com.sidooo.ufile.model;

import java.io.IOException;
import java.io.InputStream;

public class UObject
{
    /** The name of the bucket in which this object is contained */
    private String bucketName;

    /** The key under which this object is stored */
    private String key;

    private Long objectSize;

    private transient UObjectInputStream objectContent;

    private UObjectMetadata objectMetadata;

    private String hash;

    public UObject() {}

    public String getBucketName()
    {
        return this.bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getObjectKey()
    {
        return key;
    }

    public void setObjectKey(String key)
    {
        this.key = key;
    }

    public UObjectInputStream getObjectContent()
    {
        return objectContent;
    }

    public void setObjectContent(UObjectInputStream content)
    {
        this.objectContent = content;
    }

    public UObjectMetadata getObjectMetadata()
    {
        return objectMetadata;
    }

    public void setObjectMetadata(UObjectMetadata metadata)
    {
        this.objectMetadata = metadata;
    }

    public void close() throws IOException
    {
        InputStream is = getObjectContent();
        if (is != null) {
            is.close();
        }
    }
}
