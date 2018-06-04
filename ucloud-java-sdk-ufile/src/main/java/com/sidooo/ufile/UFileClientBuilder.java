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

public class UFileClientBuilder
{
    private UFileClientBuilder() {}

    // 从系统配置文件中加载
    public static UFile defaultClient()
    {
        UFileCredentials credentials = new UFileCredentials();
        return standard("cn-bj", credentials);
    }

    public static UFile standard(String region, UFileCredentials credentials)
    {
        return new UFileClient(credentials).setRegion(region);
    }
}
