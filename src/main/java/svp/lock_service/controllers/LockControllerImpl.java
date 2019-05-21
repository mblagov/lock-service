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
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/locker")
public class LockControllerImpl implements LockFileController {

    private static final String HDFS_NODENAME_PORT = "hdfs://n56:8020";

    private Logger logger = Logger.getLogger(getClass().getName());

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

        itemId = remakeFilePath(itemId);
        if (hasAlreadyLocked(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        } else {
            return BaseResponse.getErrorResponse(itemId);
        }
    }


    public BaseResponse grabLock(String itemId) throws IOException {
        if (!isFileExistsInHDFS(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        }

        itemId = remakeFilePath(itemId);

        logger.log(Level.INFO, itemId);

        if (hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }

        zkManager.create(itemId, itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }


    public BaseResponse giveLockBack(String itemId) throws IOException {
        if (!isFileExistsInHDFS(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        }

        itemId = remakeFilePath(itemId);
        if (!hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }

        zkManager.delete(itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private String remakeFilePath(String originalPath) {
        String[] pathParts = originalPath.split("/");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/");

        for (String pathPart : pathParts) {
            stringBuilder.append(pathPart).append("-");
        }
        return stringBuilder.toString();
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }

    private boolean isFileExistsInHDFS(String path) throws IOException {
        return !fileSystem.exists(new Path(path));
    }

}