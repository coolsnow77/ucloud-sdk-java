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
import com.sidooo.ufile.exception.UFileServiceException;

public class DeleteObjectRequest
        extends UObjectRequest
{
    public DeleteObjectRequest(Region region, String bucketName, String objectKey)
    {
        super(HttpType.DELETE, region, bucketName);
        this.setObjectKey(objectKey);
    }

    @Override
    public Object execute(ObjectExecutor executor)
            throws UFileServiceException
    {
        UResponse response = executor.execute(this, getObjectKey());
        return this.getObjectKey();
    }
}
