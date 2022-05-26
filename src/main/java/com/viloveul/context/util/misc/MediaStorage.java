package com.viloveul.context.util.misc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface MediaStorage {

    String write(String fname, InputStream input) throws IOException;

    File load(String path);
}
