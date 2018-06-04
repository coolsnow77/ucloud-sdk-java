package com.sidooo.ufile;

import com.google.gson.JsonObject;
import com.sidooo.ufile.exception.UFileServiceException;
import com.sidooo.ufile.request.HttpType;
import com.sidooo.ufile.request.UBucketRequest;
import org.apache.http.Header;

import java.io.InputStream;

public class CreateUHostInstanceRequest extends UBucketRequest
{
    public CreateUHostInstanceRequest()
    {
        super(HttpType.GET, "CreateUHostInstance", "cn-bj2");
        addParameter("CPU", "2");
        addParameter("Name", "Host01");
        addParameter("LoginMode", "Password");
        addParameter("ImageId", "f43736e1-65a5-4bea-ad2e-8a46e18883c2");
        addParameter("ChargeType", "Month");
        addParameter("DiskSpace", "10");
        addParameter("Memory", "2048");
        addParameter("Password", "VUNsb3VkLmNu");
        addParameter("Quantity", "1");
        addParameter("Zone", "cn-bj2-04");
        addParameter("Region", "cn-bj2");
    }

    @Override
    public void onSuccess(JsonObject result, Header[] headers, InputStream content)
            throws UFileServiceException
    {

    }
}
