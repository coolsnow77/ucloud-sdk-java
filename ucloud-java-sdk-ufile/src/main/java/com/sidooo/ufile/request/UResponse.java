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

import com.google.gson.JsonObject;
import org.apache.http.Header;

import java.io.InputStream;

public class UResponse
{
    /**
     * Http Response的Headers
     */
    private Header[] headers;

    /**
     * UFile 应答内容
     */
    private JsonObject response;

    /**
     * Http 返回的文件内容
     */
    private InputStream content;

    public UResponse(JsonObject response)
    {
        this.response = response;
    }

    public UResponse(Header[] headers)
    {
        this.headers = headers;
    }

    public UResponse(JsonObject response, Header[] headers)
    {
        this.response = response;
        this.headers = headers;
    }

    public UResponse(Header[] headers, InputStream content)
    {
        this.headers = headers;
        this.content = content;
    }

    public Header[] getHeaders()
    {
        return headers;
    }

    public UResponse setHeaders(Header[] allHeaders)
    {
        this.headers = allHeaders;
        return this;
    }

    public JsonObject getResponse()
    {
        return this.response;
    }

    public UResponse setResponse(JsonObject response)
    {
        this.response = response;
        return this;
    }

    public InputStream getContent()
    {
        return content;
    }

    public UResponse setContent(InputStream content)
    {
        this.content = content;
        return this;
    }
}
