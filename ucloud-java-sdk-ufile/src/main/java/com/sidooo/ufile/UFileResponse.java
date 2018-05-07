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

import org.apache.http.Header;
import org.apache.http.StatusLine;

import java.io.InputStream;

public class UFileResponse
{
    private StatusLine statusLine;
    private long contentLength;
    private InputStream content;
    private Header[] headers;

    public void setStatusLine(StatusLine statusLine)
    {
        this.statusLine = statusLine;
    }

    public void setContentLength(long contentLength)
    {
        this.contentLength = contentLength;
    }

    public void setContent(InputStream content)
    {
        this.content = content;
    }

    public StatusLine getStatusLine()
    {
        return statusLine;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public InputStream getContent()
    {
        return content;
    }

    public void setHeaders(Header[] allHeaders)
    {
        this.headers = allHeaders;
    }

    public Header[] getHeaders()
    {
        return headers;
    }
}
