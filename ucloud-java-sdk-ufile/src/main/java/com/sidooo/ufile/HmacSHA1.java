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

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HmacSHA1
{
    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");
    private static final String ALGORITHM = "HmacSHA1";
    private static final Object LOCK = new Object();
    private static Mac macInstance;

    public String getAlgorithm()
    {
        return ALGORITHM;
    }

    public HmacSHA1()
    {
    }

    public String sign(String key, String data)
    {
        try {
            byte[] signData = sign(
                    key.getBytes(DEFAULT_ENCODING),
                    data.getBytes(DEFAULT_ENCODING));

            return toBase64String(signData);
        }
        catch (Exception ex) {
            throw new RuntimeException("Unsupported algorithm: " + DEFAULT_ENCODING);
        }
    }

    private byte[] sign(byte[] key, byte[] data)
    {
        try {
            // Because Mac.getInstance(String) calls a synchronized method,
            // it could block on invoked concurrently.
            // SO use prototype pattern to improve perf.
            if (macInstance == null) {
                synchronized (LOCK) {
                    if (macInstance == null) {
                        macInstance = Mac.getInstance(ALGORITHM);
                    }
                }
            }

            Mac mac = null;
            try {
                mac = (Mac) macInstance.clone();
            }
            catch (CloneNotSupportedException e) {
                // If it is not clonable, create a new one.
                mac = Mac.getInstance(ALGORITHM);
            }
            mac.init(new SecretKeySpec(key, ALGORITHM));
            return mac.doFinal(data);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unsupported algorithm: " + ALGORITHM);
        }
        catch (InvalidKeyException ex) {
            throw new RuntimeException();
        }
    }

    public String toBase64String(byte[] binaryData)
    {
        return new String(Base64.encodeBase64(binaryData));
    }
}
