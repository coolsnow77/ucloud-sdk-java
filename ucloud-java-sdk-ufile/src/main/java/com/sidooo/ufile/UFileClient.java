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
import com.sidooo.ufile.request.CreateBucketRequest;
import com.sidooo.ufile.request.DeleteBucketRequest;
import com.sidooo.ufile.request.DeleteObjectRequest;
import com.sidooo.ufile.request.GetBucketRequest;
import com.sidooo.ufile.request.GetObjectMetaRequest;
import com.sidooo.ufile.request.GetObjectRequest;
import com.sidooo.ufile.request.ListBucketRequest;
import com.sidooo.ufile.request.ListObjectRequest;
import com.sidooo.ufile.request.PutObjectRequest;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UFileClient
        implements UFile
{
    private CloseableHttpClient httpClient;
    private UFileCredentials credentials;

    public UFileClient(UFileCredentials credentials)
    {
        this.credentials = credentials;
        this.httpClient = new DefaultHttpClient();
    }

    @Override
    public HttpClient getHttpClient()
    {
        return this.httpClient;
    }

    public String getContentAsString(InputStream content)
    {
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
    public UFileCredentials getCredentials()
    {
        return this.credentials;
    }

    public void setCredentials(UFileCredentials credentials)
    {
        this.credentials = credentials;
    }

    private void printResponseHeaders(UFileResponse response)
    {
        System.out.println("status line: " + response.getStatusLine());
        Header[] headers = response.getHeaders();
        for (int i = 0; i < headers.length; i++) {
            System.out.println("header " + headers[i].getName() + " : " + headers[i].getValue());
        }
        System.out.println("body length: " + response.getContentLength());
    }

    @Override
    public UBucket createBucket(String bucketName, String type, String region)
            throws UFileClientException, UFileServiceException
    {
        CreateBucketRequest request = new CreateBucketRequest(credentials, bucketName, type, region);
        request.execute(httpClient);
        return request.getNewBucket();
    }

    @Override
    public UBucket getBucket(String bucketName)
            throws UFileClientException, UFileServiceException
    {
        GetBucketRequest request = new GetBucketRequest(credentials, bucketName);
        request.execute(httpClient);
        return request.getBucket();
    }

    @Override
    public UBucketListing listBuckets()
            throws UFileClientException, UFileServiceException
    {
        ListBucketRequest request = new ListBucketRequest(credentials);
        request.execute(httpClient);
        return request.getBucketListing();
    }

    @Override
    public String deleteBucket(String bucketName)
            throws UFileClientException, UFileServiceException
    {
        DeleteBucketRequest request = new DeleteBucketRequest(credentials, bucketName);
        request.execute(httpClient);
        return request.getDeletedBucketId();
    }

    @Override
    public UObject getObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException
    {
        GetObjectRequest request = new GetObjectRequest(this.credentials, bucketName, key);
        request.execute(httpClient, key);
        return request.getObject();
    }

    @Override
    public UObjectMetadata getObject(String bucketName, String key, File destinationFile)
            throws UFileClientException, UFileServiceException
    {
        UObject object = getObject(bucketName, key);

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = object.getContent();
            outputStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
            int bufSize = 1024 * 4;
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
        UObject object = getObject(bucketName, key);
        InputStream content = object.getContent();
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
        GetObjectMetaRequest request = new GetObjectMetaRequest(credentials, bucketName, key);
        request.execute(httpClient, key);
        return request.getObjectMetadata();
    }

    @Override
    public UObjectMetadata putObject(String bucketName, String key, File file)
            throws UFileClientException, UFileServiceException
    {
        InputStream objectStream;
        try {
            objectStream = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            throw new UFileClientException(e);
        }
        PutObjectRequest request = new PutObjectRequest(credentials, bucketName, key, objectStream, file.length());
        request.execute(httpClient, key);
        return request.getNewObjectMetadata();
    }

    @Override
    public UObjectListing listObjects(String bucketName, String prefix, Integer limit)
            throws UFileClientException, UFileServiceException
    {
        ListObjectRequest request = new ListObjectRequest(credentials, bucketName, prefix, limit);
        request.execute(httpClient, "");
        return request.getObjectListing();
    }

    @Override
    public UObjectListing listNextBatchOfObjects(UObjectListing perviousObjectListing)
            throws UFileClientException, UFileServiceException
    {
        if (perviousObjectListing.getNextMarker().equals("")) {
            return null;
        }
        ListObjectRequest request = new ListObjectRequest(credentials,
                perviousObjectListing.getBucketName(),
                perviousObjectListing.getPrefix(),
                perviousObjectListing.getLimit(),
                perviousObjectListing.getNextMarker());
        request.execute(httpClient, "");
        return request.getObjectListing();
    }

    @Override
    public String deleteObject(String bucketName, String key)
            throws UFileClientException, UFileServiceException
    {
        DeleteObjectRequest request = new DeleteObjectRequest(credentials, bucketName, key);
        request.execute(httpClient, key);
        return request.getDeleteObjectKey();
    }

    @Override
    public void shutdown()
    {
        httpClient.getConnectionManager().shutdown();
    }
}
