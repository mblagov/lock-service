package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/locker")
public class LockController {

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final int CODE_SUCCESS = 100;
    private static final int AUTH_FAILURE = 102;

    private Map<String, Object> locks = new HashMap<>();

    @Autowired
    private ZKManagerImpl zkManager;

    @GetMapping("exists")
    public BaseResponse lookAtLock(@RequestParam(value = "itemId") String itemId) {
        if (zkManager.exists(itemId)) {
            return new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        }
        return new BaseResponse(ERROR_STATUS, AUTH_FAILURE);
    }

    @GetMapping("delete")
    public void DeleteLock(@RequestParam(defaultValue = "true", isLockNeeded = true) final boolean isLockNeeded) {
        System.out.println("Going to delete path ", itemId);
        if (isLockNeeded) {
            zkManager.delete(itemId);
        }
    }

    @GetMapping("get")
    public String  itemId() {
        if(zkManager.getZNodeData(itemId, true)) {
            return "lock";
        }
        return null;
    }

    @GetMapping("back")
    public String GiveLockBack(@RequestParam(defaultValue = "false", isLockNeeded = false) final boolean isLockNeeded) {
        if (isLockNeeded == true) {
            return "already locked";
        }
        return null;
    }


 /* @PostMapping("/lock")
    public BaseResponse lock(@RequestParam(value = "key") String key, @RequestBody LockRequest request) throws InterruptedException {
        boolean isLockNeeded = request.isLockNeeded();
        locks.computeIfAbsent(key, k -> request.getItemId());

        if (isLockNeeded) {
            synchronized (locks.get(key)) {
                while (zkManager.exists(key)) {
                    locks.get(key).wait();
                }
                zkManager.create(key, request.getItemId());
                locks.get(key).notifyAll();
                return new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
            }
        } else {
            synchronized (locks.get(key)) {
                zkManager.delete(key);
                locks.get(key).notifyAll();
                return new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
            }
        }
    } */
}