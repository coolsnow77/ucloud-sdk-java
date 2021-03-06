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
package com.sidooo.ufile.model;

import java.util.ArrayList;
import java.util.List;

public class UBucketListing
{
    private List<UBucket> objectSummaries = new ArrayList<UBucket>();

    public List<UBucket> getBuckets()
    {
        return this.objectSummaries;
    }

    public void putBucket(UBucket bucket)
    {
        this.objectSummaries.add(bucket);
    }

    public int getSize()
    {
        return this.objectSummaries.size();
    }
}
