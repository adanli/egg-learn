package com.egg.integration.eggnio.file;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class LearnFileDescriptor {
    public static void main(String[] args) throws Exception{
        FileDescriptor fd = FileDescriptor.out;
        FileOutputStream fos = new FileOutputStream(fd);
        fos.write("abc".getBytes());

    }
}
