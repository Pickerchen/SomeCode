package com.sen5.ocup.yili;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * 头像上传保存阿里云token实例的实体
 * Created by chenqianghua on 2016/11/7.
 */
public class AvatarOOS {

    /**
     * AccessKeySecret : EL7zTsJHRcRu8v9qwyv8yiH3eE8GghSTxpSjPb1MnD4c
     * AccessKeyId : STS.H4VPmc6KLtdgN52z8w7ZW9enF
     * Expiration : 2016-11-07T07:54:41Z
     * SecurityToken : CAESuwMIARKAAXVmLVrYh3ZsTLn/UT3DBXxHd6nUEroJKIBEzQL548v70muTRs2NeaXB8kaG+lezX3yTcSE+kqIN3L+ubc0QdwEuPW8C6hoNx9HQLnG96duhIUcY0tiRVn58MqVZsdBuQLVDZ9936xamKUtDZBGbUo/ZCjK45RjPSeTOf/sQsfJjGh1TVFMuSDRWUG1jNktMdGRnTjUyejh3N1pXOWVuRiISMzI5NzUzMjc0ODg5ODc0MjYxKgMxMjAwkfOg7oMrOgZSc2FNRDVCmQEKATEakwEKBUFsbG93EicKDEFjdGlvbkVxdWFscxIGQWN0aW9uGg8KDW9zczpQdXRPYmplY3QSYQoOUmVzb3VyY2VFcXVhbHMSCFJlc291cmNlGkUKQ2Fjczpvc3M6KjoqOm9jdXAtcHJvZHVjdC9jdXN0b21lci95aWxpL2F2YXRvci8xMjAvMTQ3ODUwMTY4MTg1Mi5wbmdKEDE3NDI1MDc5MTQ1MTcyOTVSBTI2ODQyWg9Bc3N1bWVkUm9sZVVzZXJgAGoSMzI5NzUzMjc0ODg5ODc0MjYxcghvY3VwLW9zc3ivto3q05mMAw==
     * bucket : ocup-product
     * path : customer/yili/avator/120
     * filename : 1478501681852.png
     */

    private String AccessKeySecret;
    private String AccessKeyId;
    private String Expiration;
    private String SecurityToken;
    private String bucket;
    private String path;
    private String filename;

    public static AvatarOOS objectFromData(String str) {

        return new Gson().fromJson(str, AvatarOOS.class);
    }

    public static List<AvatarOOS> arrayAvatarOOSFromData(String str) {

        Type listType = new TypeToken<ArrayList<AvatarOOS>>() {
        }.getType();

        return new Gson().fromJson(str, listType);
    }

    public String getAccessKeySecret() {
        return AccessKeySecret;
    }

    public void setAccessKeySecret(String AccessKeySecret) {
        this.AccessKeySecret = AccessKeySecret;
    }

    public String getAccessKeyId() {
        return AccessKeyId;
    }

    public void setAccessKeyId(String AccessKeyId) {
        this.AccessKeyId = AccessKeyId;
    }

    public String getExpiration() {
        return Expiration;
    }

    public void setExpiration(String Expiration) {
        this.Expiration = Expiration;
    }

    public String getSecurityToken() {
        return SecurityToken;
    }

    public void setSecurityToken(String SecurityToken) {
        this.SecurityToken = SecurityToken;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
