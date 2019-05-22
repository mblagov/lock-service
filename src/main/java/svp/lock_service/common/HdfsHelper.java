package svp.lock_service.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HdfsHelper {

    private FileSystem fileSystem;
    private static final String HDFS_NODENAME_PORT = "hdfs://n56:8020";

    public HdfsHelper() throws URISyntaxException, IOException {
        Configuration conf = new Configuration();
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        fileSystem = FileSystem.get(new URI(HDFS_NODENAME_PORT), conf);
    }

    public boolean isFileExistsInHDFS(String path) throws IOException {
        return fileSystem.exists(new Path(path));
    }
}
