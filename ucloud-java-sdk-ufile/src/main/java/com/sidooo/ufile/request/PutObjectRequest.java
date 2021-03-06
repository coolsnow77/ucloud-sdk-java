/*
 * Copyright © 2018 UCloud (上海优刻得信息科技有限公司)
 *
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
import com.sidooo.ufile.model.UObjectMetadata;
import org.apache.http.Header;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/*
 * Request Parameters
 *   Authorization: 授权签名
 *   Content-Length: 文件长度
 *   Content-Type:   文件类型
 *   Content-MD5:    文件Hash
 *
 * Request Headers
 *   None
 *
 * Request Elements
 *   RetCode：  错误代码
 *   ErrMsg:    错误信息
 *
 * Request Headers
 *   Content-Type:
 *   Content-Length:
 *   ETag:   文件哈希值
 *   X-SessionId:   会话ID
 */
public final class PutObjectRequest
        extends UObjectRequest
{
    public PutObjectRequest(UFileRegion region, String bucketName,
            String objectKey, InputStream objectStream, Long objectLength)
    {
        super(HttpType.PUT, region, bucketName);
        this.setObjectKey(objectKey);
        this.setObjectStream(objectStream);
        this.setObjectStreamLength(objectLength);
    }

    public PutObjectRequest(UFileRegion region, String bucketName,
            String objectKey, InputStream objectStream, Long objectLength, String objectType)
    {
        super(HttpType.PUT, region, bucketName);
        this.setObjectKey(objectKey);
        this.addHeader("Content-Type", objectType);
        this.setObjectStream(objectStream);
        this.setObjectStreamLength(objectLength);
    }

    public PutObjectRequest(UFileRegion region, String bucketName,
            String objectKey, InputStream objectStream, Long objectLength, String objectType, String objectMd5)
    {
        super(HttpType.PUT, region, bucketName);
        this.setObjectKey(objectKey);
        this.addHeader("Content-Type", objectType);
        this.addHeader("Content-MD5", objectMd5);
        this.setObjectStream(objectStream);
        this.setObjectStreamLength(objectLength);
    }

    @Override
    public Object execute(ObjectExecutor executor)
            throws UFileServiceException
    {
        requireNonNull(executor, "Object executor is null");
        UResponse response = executor.execute(this, getObjectKey());
        Header[] headers = response.getHeaders();
        return new UObjectMetadata(headers);
    }
}
