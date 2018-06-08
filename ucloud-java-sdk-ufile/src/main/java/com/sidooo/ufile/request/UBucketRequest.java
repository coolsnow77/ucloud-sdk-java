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

import static java.util.Objects.requireNonNull;

public abstract class UBucketRequest
        extends URequest
{
    public UBucketRequest(HttpType httpType, String actionName, UFileRegion region)
    {
        super(httpType, region);

        requireNonNull(actionName, "action name is null.");
        this.addParameter("Action", actionName);
    }

    public String getActionName()
    {
        return getParameter("Action");
    }

    public abstract Object execute(BucketExecutor executor)
            throws UFileServiceException;
}
