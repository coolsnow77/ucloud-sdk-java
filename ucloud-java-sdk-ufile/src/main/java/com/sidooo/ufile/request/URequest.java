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
package com.sidooo.ufile.request;

import com.sidooo.ufile.UFileRegion;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class URequest
{
    private final UFileRegion region;

    private final HttpType httpType;

    /**
     * 请求的URL Parameters
     */
    private final Map<String, String> parameters = new HashMap<String, String>();

    /**
     * 请求的URL Headers
     */
    private Map<String, String> headers = new HashMap<String, String>();

    public URequest(HttpType httpType, UFileRegion region)
    {
        this.httpType = requireNonNull(httpType, "Http type is null");
        this.region = requireNonNull(region, "region is null");
    }

    public UFileRegion getRegion()
    {
        return this.region;
    }

    public HttpType getHttpType()
    {
        return this.httpType;
    }

    public void addParameter(String name, String value)
    {
        requireNonNull(name, "Parameter name is null");
        requireNonNull(value, "Parameter value is null");
        this.parameters.put(name, value);
    }

    public String getParameter(String name)
    {
        requireNonNull(name, "Parameter name is null");
        return this.parameters.get(name);
    }

    public Map<String, String> getParameters()
    {
        return this.parameters;
    }

    public void addHeader(String name, String value)
    {
        requireNonNull(name, "Parameter name is null");
        requireNonNull(value, "Parameter value is null");
        this.headers.put(name, value);
    }

    public Map<String, String> getHeaders()
    {
        return this.headers;
    }

    public String getHeader(String name)
    {
        requireNonNull(name, "Parameter name is null");
        return headers.get(name);
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }
}
