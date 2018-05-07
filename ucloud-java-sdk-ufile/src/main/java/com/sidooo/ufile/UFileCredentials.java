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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UFileCredentials
{
    private String publicKey;
    private String privateKey;
    private String proxySuffix;
    private String downloadProxySuffix;

    public String getPublicKey()
    {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public String getPrivateKey()
    {
        return this.privateKey;
    }

    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    public String getProxySuffix()
    {
        return this.proxySuffix;
    }

    public void setProxySuffix(String proxySuffix)
    {
        this.proxySuffix = proxySuffix;
    }

    public String getDownloadProxySuffix()
    {
        return this.downloadProxySuffix;
    }

    public void setDownloadProxySuffix(String downloadProxySuffix)
    {
        this.downloadProxySuffix = downloadProxySuffix;
    }

    public void makeAuth(String stringToSign, UFileRequest request)
    {
        String signature = new HmacSHA1().sign(privateKey, stringToSign);
        String authorization = "UCloud" + " " + publicKey + ":" + signature;
        request.setAuthorization(authorization);
    }

    public void loadConfig(String configPath)
    {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configPath);
            Properties configProperties = new Properties();
            configProperties.load(inputStream);
            this.publicKey = configProperties.getProperty("UCloudPublicKey");
            this.privateKey = configProperties.getProperty("UCloudPrivateKey");
            this.proxySuffix = configProperties.getProperty("ProxySuffix");
            this.downloadProxySuffix = configProperties.getProperty("DownloadProxySuffix");
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
    }
}
