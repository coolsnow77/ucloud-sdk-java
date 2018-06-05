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
package com.sidooo.ucloud;

import static java.util.Objects.requireNonNull;

/**
 * UCloud User Key
 * 支持从配置文件中加载
 *
 */
public class Credentials
{
    private final String publicKey;
    private final String privateKey;

    public Credentials(String publicKey, String privateKey)
    {
        requireNonNull(publicKey, "publicKey is null.");
        requireNonNull(privateKey, "privateKey is null.");

        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getPublicKey()
    {
        return this.publicKey;
    }

    public String getPrivateKey()
    {
        return this.privateKey;
    }
}
