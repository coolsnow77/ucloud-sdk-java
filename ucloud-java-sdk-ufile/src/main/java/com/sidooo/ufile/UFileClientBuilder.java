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
import com.sidooo.ufile.request.BucketExecutor;
import com.sidooo.ufile.request.ObjectExecutor;

import static com.sidooo.ucloud.Region.CN_BJ2;
import static java.util.Objects.requireNonNull;

public class UFileClientBuilder
{
    private UFileClientBuilder() {}

    // 从系统配置文件中加载
    public static UFile defaultClient(String configFile)
    {
        requireNonNull(configFile, "config file is null.");

        Credentials credentials = new Credentials();
        credentials.loadConfig(configFile);
        return standard(credentials, CN_BJ2);
    }

    public static UFile standard(Credentials credentials, Region defaultRegion)
    {
        requireNonNull(credentials, "credentials is null.");
        requireNonNull(defaultRegion, "default region is null.");

        BucketExecutor bucketExecutor = new BucketExecutor(credentials);
        ObjectExecutor objectExecutor = new ObjectExecutor(credentials);
        return new UFileClient(credentials, bucketExecutor, objectExecutor).setDefaultRegion(defaultRegion);
    }
}
