package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.common.HiveHelper;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.sql.SQLException;

@RestController
@RequestMapping("/tableLocker")
public class LockTableControllerImpl implements LockTableController {

    private static final String TABLE_DOESN_T_EXIST = "Table doesn't exist in Hive";
    private static final String TABLE_IS_LOCKED = "Table is locked";
    @Autowired
    private ZKManagerImpl zkManager;

    private HiveHelper hiveHelper;

    public LockTableControllerImpl() throws SQLException {
        hiveHelper = new HiveHelper();
    }

    public BaseResponse isLockFree(@RequestParam(value = "itemId") String itemId) throws SQLException {
        if (!hiveHelper.isTableExists(itemId)) {
            return BaseResponse.getErrorResponse(itemId, TABLE_DOESN_T_EXIST);
        }

        String zkNodePath = remakeFilePath(itemId);
        if (hasAlreadyLocked(zkNodePath)) {
            return BaseResponse.getErrorResponse(itemId, TABLE_IS_LOCKED);
        }
        return BaseResponse.getSuccessResponse(itemId);
    }

    public BaseResponse grabLock(@RequestParam(value = "itemId") String itemId) throws SQLException {
        String zkNodePath = remakeFilePath(itemId);
        if (!hiveHelper.isTableExists(itemId) || hasAlreadyLocked(zkNodePath)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.create(zkNodePath, itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    public BaseResponse giveLockBack(@RequestParam(value = "itemId") String itemId) throws SQLException {
        String zkNodePath = remakeFilePath(itemId);
        if (!hiveHelper.isTableExists(itemId) || !hasAlreadyLocked(zkNodePath)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.delete(zkNodePath);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private String remakeFilePath(String originalPath) {
        return "/" + originalPath.replace("/", "-");
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }
}
