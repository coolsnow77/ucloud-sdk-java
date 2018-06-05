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

import com.sidooo.ucloud.Region;
import com.sidooo.ufile.UFileHeaders;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.model.UObject;
import com.sidooo.ufile.model.UObjectInputStream;
import com.sidooo.ufile.model.UObjectMetadata;
import org.apache.http.Header;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
 *
 * Request Parameters
 *      None
 *
 * Request Headers
 *   Authorization : 下载请求的授权签名
 *   Range : bytes=<byte_range>   分片下载的文件范围
 *   If-Modified-Since : <timestamp>   只返回从某时修改过的文件， 否则返回304
 *
 * Response Headers
 *   Content-Type:  文件类型
 *   Content-Length: 文件长度
 *   Content-Range:  文件的范围
 *   ETag:  哈希值
 *   X-SessionId:  会话Id
 *
 * Response Elements
 *   RetCode:  错误代码
 *   ErrMsg:  错误提示
 */

public final class GetObjectRequest
        extends UObjectRequest
{
    private static final String CONTENT_RANGE_REGEX = "^bytes ([0-9]+)-([0-9]+)/[0-9]+";

    public GetObjectRequest(Region region, String bucketName, String objectKey)
    {
        super(HttpType.GET, region, bucketName);
        this.setObjectKey(objectKey);
    }

    public GetObjectRequest(Region region, String bucketName, String objectKey, String objectRange)
    {
        super(HttpType.GET, region, bucketName);
        this.setObjectKey(objectKey);
        this.addHeader("Range", objectRange);
    }

    private String getContentRange(Header[] headers)
    {
        for (Header header : headers) {
            if (header.getName().equals(UFileHeaders.CONTENT_RANGE)) {
                return header.getValue();
            }
        }
        return null;
    }

    @Override
    public Object execute(ObjectExecutor executor)
            throws UFileServiceException
    {
        UResponse response = executor.execute(this, this.getObjectKey());
        Header[] headers = response.getHeaders();
        InputStream content = response.getContent();

        // 解析Content-Range
        // 格式： bytes 0-31/24135125
        Long rangeOffset = 0L;
        String contentRange = getContentRange(headers);
        if (contentRange != null) {
            Pattern r = Pattern.compile(CONTENT_RANGE_REGEX);
            Matcher matcher = r.matcher(contentRange);
            if (matcher.find()) {
                rangeOffset = Long.valueOf(matcher.group(1));
            }
        }

        UObject object = new UObject();
        object.setBucketName(this.getBucketName());
        object.setObjectKey(this.getObjectKey());
        object.setObjectContent(new UObjectInputStream(content, rangeOffset));
        object.setObjectMetadata(new UObjectMetadata(headers));
        return object;
    }
}
