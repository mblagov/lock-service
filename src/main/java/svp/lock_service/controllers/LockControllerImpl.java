package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.common.FileUtils;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/locker")
public class LockControllerImpl implements LockController {

    private Map<String, Object> locks = new HashMap<>();

    @Autowired
    private ZKManagerImpl zkManager;


    public BaseResponse lookAtLock(String itemId) {
        if (FileUtils.isFileExists(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        return BaseResponse.getSuccessResponse(itemId);
    }


    public BaseResponse grabLock(String itemId) {
        if (!FileUtils.isFileExists(itemId) || hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.create(itemId, itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }


    public BaseResponse giveLockBack(String itemId) {
        if (!FileUtils.isFileExists(itemId) || !hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.delete(itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }

}