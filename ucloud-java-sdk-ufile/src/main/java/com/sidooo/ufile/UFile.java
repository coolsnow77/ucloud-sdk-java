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
package com.sidooo.ufile;

import com.sidooo.ucloud.Credentials;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UBucket;
import com.sidooo.ufile.model.UBucketListing;
import com.sidooo.ufile.model.UObject;
import com.sidooo.ufile.model.UObjectListing;
import com.sidooo.ufile.model.UObjectMetadata;

import java.io.File;

public interface UFile
{
    /**
     * 获取UFile的身份认证信息
     *
     * @return ucloud credentials
     */
    Credentials getCredentials();

    /**
     * 获取客户端默认的Region
     *
     * @return ufile region
     */
    UFileRegion getDefaultRegion();

    /**
     * 设置操作的默认Region
     *
     * @param region  区域
     * @return ufile client
     */
    UFile setDefaultRegion(UFileRegion region);

    /**
     * 创建UFile Bucket
     *
     * @param bucketName  Bucket名称
     * @param type  Bucket类型
     * @return ufile bucket
     * @throws UFileClientException   SDK客户端异常
     * @throws UFileServiceException  UFile服务异常
     */
    UBucket createBucket(String bucketName, String type)
            throws UFileClientException, UFileServiceException;

    /**
     * 获取指定Region下的所有Bucket
     *
     * @return listing of buckets
     * @throws UFileClientException SDK客户端异常
     * @throws UFileServiceException UFile服务异常
     */
    UBucketListing listBuckets()
            throws UFileClientException, UFileServiceException;

    /**
     * 获取Bucket详细信息
     *
     * @param bucketName  Bucket名称
     * @return ufile bucket
     * @throws UFileClientException SDK客户端异常
     * @throws UFileServiceException UFile服务异常
     */
    UBucket getBucket(String bucketName)
            throws UFileClientException, UFileServiceException;

    /**
     * 删除指定的Bucket
     *
     * @param bucketName
     * @return deleted bucket name
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    String deleteBucket(String bucketName)
            throws UFileClientException, UFileServiceException;

    /**
     * 获取UFile对象
     *
     * @param bucketName
     * @param key
     * @return ufile object
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UObject getObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    /**
     * 获取对象指定偏移和长度的内容
     *
     * @param bucketName
     * @param key
     * @param offset
     * @param length
     * @return ufile object
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UObject getObject(String bucketName, String key, long offset, int length)
            throws UFileClientException, UFileServiceException;

    /**
     * 下载UFile对象到指定的文件中
     *
     * @param bucketName
     * @param key
     * @param destinationFile
     * @return ufile object meatadata
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UObjectMetadata getObject(String bucketName, String key, File destinationFile)
            throws UFileClientException, UFileServiceException;

    /**
     * 将指定的UFile内容以String的方式返回
     *
     * @param bucketName
     * @param key
     * @return object content
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    String getObjectAsString(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    UObjectMetadata getObjectMetadata(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    /**
     * 上传文件到UFile中
     *
     * @param bucketName
     * @param key
     * @param file
     * @return metadata of ufile object
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UObjectMetadata putObject(String bucketName, String key, File file)
            throws UFileClientException, UFileServiceException;

    /**
     * 获取指定的Bucket下所有的UFile对象列表
     *
     * @param bucketName
     * @param prefix
     * @param limit
     * @return listing of ufile objects
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UObjectListing listObjects(String bucketName, String prefix, Integer limit)
            throws UFileClientException, UFileServiceException;

    /**
     * 获取下一页的对象列表
     *
     * @param perviousObjectListing
     * @return listing of ufile objects
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UObjectListing listNextBatchOfObjects(UObjectListing perviousObjectListing)
            throws UFileClientException, UFileServiceException;

    /**
     * 删除UFile对象
     *
     * @param bucketName
     * @param key
     * @return deleted object key
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    String deleteObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    /**
     * 关闭UFile客户端
     */
    void shutdown();
}
