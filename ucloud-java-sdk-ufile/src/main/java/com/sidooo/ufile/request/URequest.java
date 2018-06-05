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
package com.sidooo.ufile.request;

import com.sidooo.ucloud.Region;

import java.util.HashMap;
import java.util.Map;

public class URequest
{
    private final Region region;

    private final HttpType httpType;

    /**
     * 请求的URL Parameters
     */
    private Map<String, String> parameters = new HashMap<String, String>();

    /**
     * 请求的URL Headers
     */
    private Map<String, String> headers = new HashMap<String, String>();

    public URequest(HttpType httpType, Region region)
    {
        this.httpType = httpType;
        this.region = region;
    }

    public Region getRegion()
    {
        return this.region;
    }

    public HttpType getHttpType()
    {
        return this.httpType;
    }

    public void addParameter(String name, String value)
    {
        this.parameters.put(name, value);
    }

    public String getParameter(String name)
    {
        return this.parameters.get(name);
    }

    public Map<String, String> getParameters()
    {
        return this.parameters;
    }

    public void addHeader(String name, String value)
    {
        this.headers.put(name, value);
    }

    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    public String getHeader(String name)
    {
        return headers.get(name);
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }
}
