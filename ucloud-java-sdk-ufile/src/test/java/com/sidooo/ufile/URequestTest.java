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

import com.sidooo.ucloud.UCloudSignatureBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URequestTest
{
    static final String UFILE_CONFIG_FILE = "/Users/kimzhang/.ucloud/ufile.properties";
    private UFileCredentials credentials;

    @Before
    public void setup()
    {
        credentials = new UFileCredentials();
        credentials.loadConfig(UFILE_CONFIG_FILE);
    }

    @After
    public void teardown()
    {
    }

    @Test
    public void test1()
    {
        UFileCredentials credentials = new UFileCredentials();
        credentials.setPublicKey("ucloudsomeone@example.com1296235120854146120");
        credentials.setPrivateKey("46f09bb9fab4f12dfc160dae12273d5332b5debe");

        CreateUHostInstanceRequest request = new CreateUHostInstanceRequest();

        // 验证待签名的字符串
        String correctAPIString = "ActionCreateUHostInstanceCPU2ChargeTypeMonthDiskSpace10ImageIdf43736e1-65a5-4bea-ad2e-8a46e18883c2LoginModePasswordMemory2048NameHost01PasswordVUNsb3VkLmNuPublicKeyucloudsomeone@example.com1296235120854146120Quantity1Regioncn-bj2Zonecn-bj2-0446f09bb9fab4f12dfc160dae12273d5332b5debe";
        assertEquals(correctAPIString, UCloudSignatureBuilder.getAPIString(request, credentials));

        String signature = UCloudSignatureBuilder.getSignature(request, credentials);
        assertEquals("4f9ef5df2abab2c6fccd1e9515cb7e2df8c6bb65", signature);

        String correctHttpString = "https://api.ucloud.cn/?Action=CreateUHostInstance&CPU=2&ChargeType=Month&DiskSpace=10&ImageId=f43736e1-65a5-4bea-ad2e-8a46e18883c2&LoginMode=Password&Memory=2048&Name=Host01&Password=VUNsb3VkLmNu&PublicKey=ucloudsomeone%40example.com1296235120854146120&Quantity=1&Region=cn-bj2&Zone=cn-bj2-04&Signature=4f9ef5df2abab2c6fccd1e9515cb7e2df8c6bb65";
        assertEquals(correctHttpString, UCloudSignatureBuilder.getHttpString(request, credentials));
    }
}
