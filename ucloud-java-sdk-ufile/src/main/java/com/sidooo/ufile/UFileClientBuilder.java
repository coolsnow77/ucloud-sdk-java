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

import com.sidooo.ucloud.Region;

import static com.sidooo.ucloud.Region.CN_BJ2;

public class UFileClientBuilder
{
    private UFileClientBuilder() {}

    // 从系统配置文件中加载
    public static UFile defaultClient(String configFile)
    {
        UFileCredentials credentials = new UFileCredentials();
        credentials.loadConfig(configFile);
        return standard(credentials, CN_BJ2);
    }

    public static UFile standard(UFileCredentials credentials, Region defaultRegion)
    {
        return new UFileClient(credentials).setDefaultRegion(defaultRegion);
    }
}
