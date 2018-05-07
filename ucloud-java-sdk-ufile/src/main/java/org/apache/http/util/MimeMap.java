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
package org.apache.http.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MimeMap
{
    private static final String MIME_MAP_CONFIG = "org/apache/http/util/mime_type.list";

    private Map<String, String> mimeMap = new HashMap<String, String>();
    private static final MimeMap single = new MimeMap();

    private MimeMap()
    {
        InputStream inputStream = MimeMap.class.getClassLoader().getResourceAsStream(MIME_MAP_CONFIG);
        Properties prop = new Properties();
        try {
            if (inputStream == null) {
                throw new Exception(MIME_MAP_CONFIG + " resource not found");
            }
            prop.load(inputStream);
            Enumeration<?> e = prop.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = prop.getProperty(key);
                mimeMap.put(key, value);
            }
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

    public static MimeMap getInstatnce()
    {
        return single;
    }

    public String getMime(String path)
    {
        String noMatch = "application/octet-stream";
        int t = path.lastIndexOf(".");
        if (t < 0) {
            return noMatch;
        }
        String suffix = path.substring(t + 1, path.length());
        if (suffix == null) {
            return noMatch;
        }
        if (!this.mimeMap.containsKey(suffix)) {
            return noMatch;
        }
        return this.mimeMap.get(suffix);
    }
}
