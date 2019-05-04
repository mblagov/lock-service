package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.models.Status;
import svp.lock_service.zk.ZKManagerImpl;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/locker")
public class LockController {

    private Map<String, Object> locks = new HashMap<>();

    @Autowired
    private ZKManagerImpl zkManager;

    @GetMapping("/exists")
    public BaseResponse lookAtLock(@RequestParam(value = "itemId") String itemId) {
        if (zkManager.exists(itemId)) {
            return BaseResponse.getSuccessResponse(itemId);
        }
        return BaseResponse.getErrorResponse(itemId);
    }

    @GetMapping("/giveback")
    public BaseResponse giveLockBack(@RequestParam(value = "itemId") String itemId) {
        if (!hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.delete(itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    private boolean hasAlreadyLocked(String itemId) {
        return zkManager.exists(itemId);
    }

}