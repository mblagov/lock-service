package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.common.TableUtils;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.sql.SQLException;

@RestController
@RequestMapping("/tableLocker")
public class LockTableControllerImpl implements LockTableController {

    private static final String TABLE_DOESN_T_EXIST = "File doesn't exist on HDFS";
    private static final String TABLE_IS_LOCKED = "Table is locked";
    @Autowired
    private ZKManagerImpl zkManager;

    public BaseResponse isLockFree(@RequestParam(value = "itemId") String itemId) throws SQLException {
        if (!TableUtils.isTableExists(itemId)) {
            return BaseResponse.getErrorResponse(itemId, TABLE_DOESN_T_EXIST);
        }

        if (hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId, TABLE_IS_LOCKED);
        }
        return BaseResponse.getSuccessResponse(itemId);
    }

    public BaseResponse grabLock(@RequestParam(value = "itemId") String itemId) throws SQLException {
        if (!TableUtils.isTableExists(itemId) || hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.create(itemId, itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    public BaseResponse giveLockBack(@RequestParam(value = "itemId") String itemId) throws SQLException {
        if (!TableUtils.isTableExists(itemId) || !hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.delete(itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }
}
