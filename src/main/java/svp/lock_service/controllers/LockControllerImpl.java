package svp.lock_service.controllers;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/locker")
public class LockControllerImpl implements LockFileController {

    private static final String HDFS_NODENAME_PORT = "hdfs://n56:8020";

    @Autowired
    private ZKManagerImpl zkManager;

    private FileSystem fileSystem;

    public LockControllerImpl() throws IOException, URISyntaxException {
        Configuration conf = new Configuration();
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        fileSystem = FileSystem.get(new URI(HDFS_NODENAME_PORT), conf);
    }

    public BaseResponse lookAtLock(String itemId) throws IOException {
        if (!isFileExistsInHDFS(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        }

        String zookeeperNodePath = remakeFilePath(itemId);
        if (hasAlreadyLocked(zookeeperNodePath)) {
            return BaseResponse.getSuccessResponse(itemId);
        } else {
            return BaseResponse.getErrorResponse(itemId);
        }
    }


    public BaseResponse grabLock(String itemId) throws IOException {
        if (!isFileExistsInHDFS(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }

        String zookeeperNodePath = remakeFilePath(itemId);
        if (hasAlreadyLocked(zookeeperNodePath)) {
            return BaseResponse.getErrorResponse(itemId);
        }

        zkManager.create(zookeeperNodePath, zookeeperNodePath);
        return BaseResponse.getSuccessResponse(itemId);
    }


    public BaseResponse giveLockBack(String itemId) throws IOException {
        if (!isFileExistsInHDFS(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }

        String zookeeperNodePath = remakeFilePath(itemId);
        if (!hasAlreadyLocked(zookeeperNodePath)) {
            return BaseResponse.getErrorResponse(itemId);
        }

        zkManager.delete(zookeeperNodePath);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private String remakeFilePath(String originalPath) {
        return originalPath.replace("/", "-");
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }

    private boolean isFileExistsInHDFS(String path) throws IOException {
        return !fileSystem.exists(new Path(path));
    }

}