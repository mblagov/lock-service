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

@RestController
@RequestMapping("/locker")
public class LockControllerImpl implements LockFileController {

    private static final String HDFS_NODENAME_PORT = "hdfs://n56:8020";
    private static final String FS_DEFAULT_NAME = "fs.default.name";

    @Autowired
    private ZKManagerImpl zkManager;

    private FileSystem fileSystem;

    public LockControllerImpl() throws IOException {
        Configuration conf = new Configuration();
        conf.set(FS_DEFAULT_NAME, HDFS_NODENAME_PORT);
        fileSystem = FileSystem.get(conf);
    }

    public BaseResponse lookAtLock(String itemId) throws IOException {
        if (!isFileExistsInHDFS(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        }

        if (zkManager.exists(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        } else {
            return BaseResponse.getErrorResponse(itemId);
        }
    }


    public BaseResponse grabLock(String itemId) throws IOException {
        if (!isFileExistsInHDFS(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        }

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

        if (!hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }

        zkManager.delete(itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }

    private boolean isFileExistsInHDFS(String path) throws IOException {
        return !fileSystem.exists(new Path(path));
    }

}