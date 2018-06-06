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
package com.sidooo.ufile;

import com.sidooo.ucloud.UCloudCredentials;
import com.sidooo.ufile.request.BucketExecutor;
import com.sidooo.ufile.request.ObjectExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public final class UFileClientBuilder
{
    private UFileClientBuilder() {}

    /**
     * 从默认的配置文件中加载
     *
     * @return
     */
    public static UFile defaultClient()
    {
        String userHome = System.getProperty("user.home");
        String defaultPropertyFile = userHome + "/.ucloud/ufile.properties";
        return standard(defaultPropertyFile);
    }

    /**
     * 从系统配置文件中加载
     * UCloudPublicKey=<公钥>
     * UCloudPrivateKey=<私钥>
     * DefaultRegion=<Region>
     *
     * @param propertyFilePath
     * @return
     */
    public static UFile standard(String propertyFilePath)
    {
        // 对用户输入的配置文件进行检查
        requireNonNull(propertyFilePath, "config file is null.");
        File propertyFile = new File(propertyFilePath);
        if (!propertyFile.isFile() || !propertyFile.canRead()) {
            throw new RuntimeException("read property file failed. file: " + propertyFilePath);
        }

        // 加载配置文件
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propertyFile);
            properties.load(inputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String publicKey = properties.getProperty("UCloudPublicKey");
        if (publicKey == null) {
            throw new RuntimeException("UCloudPublicKey missing in configure file.");
        }
        String privateKey = properties.getProperty("UCloudPrivateKey");
        if (privateKey == null) {
            throw new RuntimeException("UCloudPrivateKey missing in configure file.");
        }
        UCloudCredentials credentials = new UCloudCredentials(publicKey, privateKey);

        String defaultUFileRegion = properties.getProperty("DefaultUFileRegion", "cn-bj");
        UFileRegion region = UFileRegion.getEnum(defaultUFileRegion);

        return standard(credentials, region);
    }

    public static UFile standard(UCloudCredentials credentials, UFileRegion defaultRegion)
    {
        requireNonNull(credentials, "credentials is null.");
        requireNonNull(defaultRegion, "default region is null.");

        BucketExecutor bucketExecutor = new BucketExecutor(credentials);
        ObjectExecutor objectExecutor = new ObjectExecutor(credentials);
        return new UFileClient(credentials, bucketExecutor, objectExecutor).setDefaultRegion(defaultRegion);
    }
}
