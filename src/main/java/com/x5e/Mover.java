package com.x5e;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import org.skife.jdbi.v2.DBI;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.util.TimeZone;
import java.util.zip.GZIPInputStream;


public class Mover {

    static String bucket = "classpass-smartspot-canned";

    @SuppressWarnings("unchecked")
    static Object get(Object thing,String key) {
        return ((Map<String,Object>) thing).get(key);
    }

    static TransferManager getTm() throws Exception {
        Yaml yaml = new Yaml();
        Object deploy = get(yaml.load(new FileInputStream(".travis.yml")),"deploy");
        TransferManager transferManager = new TransferManager(new AWSCredentials() {
            public String getAWSAccessKeyId() {return get(deploy,"access_key_id").toString();}
            public String getAWSSecretKey() {return get(deploy,"secret_access_key").toString();}
        });
        return transferManager;
    }

    public static void download(String fn) throws Exception {
        File target = new File(fn);
        if (target.exists()) throw new RuntimeException("file already exists!");
        Download download = getTm().download(bucket, fn, new File(fn));
        download.waitForCompletion();
    }

    public static void upload(String fn) throws Exception {
        File file = new File(fn);
        if (!file.exists()) throw new Exception("no file");
        Upload upload = getTm().upload(bucket,fn,file);
        upload.waitForCompletion();
    }


    public static void main(String args[]) throws Exception {
        if (args.length < 2) throw new RuntimeException("need two args");
        if (args[0].equals("download")) download(args[1]);
        if (args[0].equals("upload")) upload(args[1]);
        System.err.println("done!");
    }
}
