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

import java.io.InputStream;

public class UObject
{
    private String key;

    private InputStream objectContent;

    private Long objectLength;

    private UObjectMetadata objectMetadata;

    private String hash;

    public UObject() {}

    public UObject(String key)
    {
        this.key = key;
    }

    public UObject(String key, InputStream content)
    {
        this.key = key;
        this.objectContent = content;
    }

    public UObject(String key, InputStream content, Long length)
    {
        this.key = key;
        this.objectContent = content;
        this.objectLength = length;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public InputStream getContent()
    {
        return objectContent;
    }

    public void setContent(InputStream content)
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

    public Long getLength()
    {
        return objectLength;
    }

    public void setLengnth(Long length)
    {
        this.objectLength = length;
    }

    public String getHash()
    {
        return hash;
    }

    public void setHash(String hash)
    {
        this.hash = hash;
    }
}
