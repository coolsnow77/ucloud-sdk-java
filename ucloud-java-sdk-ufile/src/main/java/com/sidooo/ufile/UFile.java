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

import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UBucket;
import com.sidooo.ufile.model.UBucketListing;
import com.sidooo.ufile.model.UObject;
import com.sidooo.ufile.model.UObjectListing;
import com.sidooo.ufile.model.UObjectMetadata;
import org.apache.http.client.HttpClient;

import java.io.File;

public interface UFile
{
    UFileCredentials getCredentials();

    HttpClient getHttpClient();

    String getRegion();

    UFile setRegion(String region);

    /**
     * 创建UFile Bucket
     *
     * @param bucketName
     * @param type
     * @param region
     * @return
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UBucket createBucket(String bucketName, String type, String region)
            throws UFileClientException, UFileServiceException;

    /**
     * 获取指定Region下的所有Bucket
     *
     * @return
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UBucketListing listBuckets()
            throws UFileClientException, UFileServiceException;

    UBucket getBucket(String bucketName)
            throws UFileClientException, UFileServiceException;

    String deleteBucket(String bucketName)
            throws UFileClientException, UFileServiceException;

    UObject getObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    /**
     * 获取对象指定偏移和长度的内容
     *
     * @param bucketName
     * @param key
     * @param offset
     * @param length
     * @return
     * @throws UFileClientException
     * @throws UFileServiceException
     */
    UObject getObject(String bucketName, String key, long offset, int length)
            throws UFileClientException, UFileServiceException;

    UObjectMetadata getObject(String bucketName, String key, File destinationFile)
            throws UFileClientException, UFileServiceException;

    String getObjectAsString(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    UObjectMetadata getObjectMetadata(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    UObjectMetadata putObject(String bucketName, String key, File file)
            throws UFileClientException, UFileServiceException;

    UObjectListing listObjects(String bucketName, String prefix, Integer limit)
            throws UFileClientException, UFileServiceException;

    UObjectListing listNextBatchOfObjects(UObjectListing perviousObjectListing)
            throws UFileClientException, UFileServiceException;

    String deleteObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException;

    void shutdown();
}
