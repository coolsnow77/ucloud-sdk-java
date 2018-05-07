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
package com.sidooo.ufile.body;

public class FinishMultiBody
{
    private String bucket;
    private String key;
    private String fileSize;

    public FinishMultiBody()
    {
    }

    public String getBucket()
    {
        return bucket;
    }

    public void setBucket(String bucket)
    {
        bucket = bucket;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        key = key;
    }

    public String getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(String fileSize)
    {
        fileSize = fileSize;
    }
}