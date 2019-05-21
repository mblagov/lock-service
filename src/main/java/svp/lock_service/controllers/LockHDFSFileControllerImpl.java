package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.common.HdfsHelper;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/locker")
public class LockHDFSFileControllerImpl implements LockHDFSFileController {

    public static final String FILE_DOESN_T_EXIST_ON_HDFS = "File doesn't exist on HDFS";
    public static final String FILE_ALREADY_LOCKED_BY_SOMEONE_ELSE = "File already locked by someone else";

    @Autowired
    private ZKManagerImpl zkManager;

    private HdfsHelper hdfsHelper;

    public LockHDFSFileControllerImpl() throws IOException, URISyntaxException {
        hdfsHelper = new HdfsHelper();
    }

    public BaseResponse lookAtLock(String itemId) throws IOException {
        if (!hdfsHelper.isFileExistsInHDFS(itemId)) {
            return BaseResponse.getErrorResponse(itemId, FILE_DOESN_T_EXIST_ON_HDFS);
        }

        String zookeeperNodePath = remakeFilePath(itemId);
        if (hasAlreadyLocked(zookeeperNodePath)) {
            return BaseResponse.getErrorResponse(itemId, "File is locked");
        } else {
            return BaseResponse.getSuccessResponse(itemId);
        }
    }


    public BaseResponse grabLock(String itemId) throws IOException {
        if (!hdfsHelper.isFileExistsInHDFS(itemId)) {
            return BaseResponse.getErrorResponse(itemId, FILE_DOESN_T_EXIST_ON_HDFS);
        }

        String zookeeperNodePath = remakeFilePath(itemId);
        if (hasAlreadyLocked(zookeeperNodePath)) {
            return BaseResponse.getErrorResponse(itemId, FILE_ALREADY_LOCKED_BY_SOMEONE_ELSE);
        }

        zkManager.create(zookeeperNodePath, zookeeperNodePath);
        return BaseResponse.getSuccessResponse(itemId);
    }


    public BaseResponse giveLockBack(String itemId) throws IOException {
        if (!hdfsHelper.isFileExistsInHDFS(itemId)) {
            return BaseResponse.getErrorResponse(itemId, FILE_DOESN_T_EXIST_ON_HDFS);
        }

        String zookeeperNodePath = remakeFilePath(itemId);
        if (!hasAlreadyLocked(zookeeperNodePath)) {
            return BaseResponse.getErrorResponse(itemId, FILE_ALREADY_LOCKED_BY_SOMEONE_ELSE);
        }

        zkManager.delete(zookeeperNodePath);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private String remakeFilePath(String originalPath) {
        return "/" + originalPath.replace("/", "-");
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }
}