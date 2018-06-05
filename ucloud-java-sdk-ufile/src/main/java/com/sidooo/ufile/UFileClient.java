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

import com.sidooo.ucloud.Credentials;
import com.sidooo.ucloud.Region;
import com.sidooo.ufile.exception.UFileClientException;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UBucket;
import com.sidooo.ufile.model.UBucketListing;
import com.sidooo.ufile.model.UObject;
import com.sidooo.ufile.model.UObjectListing;
import com.sidooo.ufile.model.UObjectMetadata;
import com.sidooo.ufile.request.BucketExecutor;
import com.sidooo.ufile.request.CreateBucketRequest;
import com.sidooo.ufile.request.DeleteBucketRequest;
import com.sidooo.ufile.request.DeleteObjectRequest;
import com.sidooo.ufile.request.GetBucketRequest;
import com.sidooo.ufile.request.GetObjectMetaRequest;
import com.sidooo.ufile.request.GetObjectRequest;
import com.sidooo.ufile.request.ListBucketRequest;
import com.sidooo.ufile.request.ListObjectRequest;
import com.sidooo.ufile.request.ObjectExecutor;
import com.sidooo.ufile.request.PutObjectRequest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Objects.requireNonNull;

public class UFileClient
        implements UFile
{
    /**
     * 用户的UCloud身份信息
     */
    private final Credentials credentials;

    /**
     * UFile操作默认的目标Region
     */
    private Region defaultRegion;

    /**
     * Bucket操作执行器
     */
    private final BucketExecutor bucketExecutor;

    /**
     * Object操作执行器
     */
    private final ObjectExecutor objectExecutor;

    public UFileClient(
            Credentials credentials,
            BucketExecutor bucketExecutor,
            ObjectExecutor objectExecutor)
    {
        this.credentials = credentials;
        this.bucketExecutor = bucketExecutor;
        this.objectExecutor = objectExecutor;
    }

    @Override
    public Region getDefaultRegion()
    {
        return defaultRegion;
    }

    @Override
    public UFile setDefaultRegion(Region defaultRegion)
    {
        this.defaultRegion = defaultRegion;
        return this;
    }

    @Override
    public Credentials getCredentials()
    {
        return this.credentials;
    }

//    private void printResponseHeaders(UFileResponse response)
//    {
//        System.out.println("status line: " + response.getStatusLine());
//        Header[] headers = response.getHeaders();
//        for (int i = 0; i < headers.length; i++) {
//            System.out.println("header " + headers[i].getName() + " : " + headers[i].getValue());
//        }
//        System.out.println("body length: " + response.getContentLength());
//    }

    @Override
    public UBucket createBucket(String bucketName, String type)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(type, "bucketType is null");

        CreateBucketRequest request = new CreateBucketRequest(defaultRegion, bucketName, type);
        return (UBucket) request.execute(bucketExecutor);
    }

    @Override
    public UBucket getBucket(String bucketName)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");

        GetBucketRequest request = new GetBucketRequest(defaultRegion, bucketName);
        return (UBucket) request.execute(bucketExecutor);
    }

    @Override
    public UBucketListing listBuckets()
            throws UFileClientException, UFileServiceException
    {
        ListBucketRequest request = new ListBucketRequest(defaultRegion);
        return (UBucketListing) request.execute(bucketExecutor);
    }

    @Override
    public String deleteBucket(String bucketName)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");

        DeleteBucketRequest request = new DeleteBucketRequest(defaultRegion, bucketName);
        return (String) request.execute(bucketExecutor);
    }

    @Override
    public UObject getObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(key, "key is null");

        GetObjectRequest request = new GetObjectRequest(defaultRegion, bucketName, key);
        return (UObject) request.execute(objectExecutor);
    }

    @Override
    public UObject getObject(String bucketName, String key, long offset, int length)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(key, "objectKey is null");

        String range = String.format("bytes=%d-%d", offset, offset + length - 1);
        GetObjectRequest request = new GetObjectRequest(defaultRegion, bucketName, key, range);
        return (UObject) request.execute(objectExecutor);
    }

    @Override
    public UObjectMetadata getObject(String bucketName, String key, File destinationFile)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(key, "objectKey is null");

        UObject object = getObject(bucketName, key);

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = object.getObjectContent();
            outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
            int bufSize = 1024 * 8;
            byte[] buffer = new byte[bufSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        catch (IOException e) {
            throw new UFileClientException(e);
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            catch (Exception e) {
                throw new UFileClientException(e);
            }
        }

        return object.getObjectMetadata();
    }

    @Override
    public String getObjectAsString(String bucketName, String key)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(key, "objectKey is null");

        UObject object = getObject(bucketName, key);
        InputStream content = object.getObjectContent();
        if (content != null) {
            // Error Message
            try {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = content.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                return result.toString("UTF-8");
            }
            catch (IOException e) {
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public UObjectMetadata getObjectMetadata(String bucketName, String key)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(key, "objectKey is null");

        GetObjectMetaRequest request = new GetObjectMetaRequest(defaultRegion, bucketName, key);
        return (UObjectMetadata) request.execute(objectExecutor);
    }

    @Override
    public UObjectMetadata putObject(String bucketName, String key, File file)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(key, "objectKey is null");
        requireNonNull(file, "targetFile is null");

        InputStream objectStream;
        try {
            objectStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            throw new UFileClientException(e);
        }
        PutObjectRequest request = new PutObjectRequest(defaultRegion, bucketName, key, objectStream, file.length());
        return (UObjectMetadata) request.execute(objectExecutor);
    }

    @Override
    public UObjectListing listObjects(String bucketName, String prefix, Integer limit)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");

        ListObjectRequest request = new ListObjectRequest(defaultRegion, bucketName, prefix, limit);
        return (UObjectListing) request.execute(objectExecutor);
    }

    @Override
    public UObjectListing listNextBatchOfObjects(UObjectListing perviousObjectListing)
            throws UFileClientException, UFileServiceException
    {
        if (perviousObjectListing.getNextMarker().equals("")) {
            return null;
        }
        ListObjectRequest request = new ListObjectRequest(defaultRegion,
                perviousObjectListing.getBucketName(),
                perviousObjectListing.getPrefix(),
                perviousObjectListing.getLimit(),
                perviousObjectListing.getNextMarker());
        return (UObjectListing) request.execute(objectExecutor);
    }

    @Override
    public String deleteObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException
    {
        requireNonNull(bucketName, "bucketName is null");
        requireNonNull(key, "objectKey is null");

        DeleteObjectRequest request = new DeleteObjectRequest(defaultRegion, bucketName, key);
        return (String) request.execute(objectExecutor);
    }

    @Override
    public void shutdown()
    {
        if (objectExecutor != null) {
            objectExecutor.close();
        }
        if (bucketExecutor != null) {
            bucketExecutor.close();
        }
    }
}
