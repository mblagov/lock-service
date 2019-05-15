package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.common.FileUtils;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/locker")
public class LockController {

    private Map<String, Object> locks = new HashMap<>();

    @Autowired
    private ZKManagerImpl zkManager;

    /**
     * Проверить файл на наличиие активной блокировки
     * @param itemId - путь к файлу, который хочется проверить
     */
    @GetMapping("/exists")
    public BaseResponse lookAtLock(@RequestParam(value = "itemId") String itemId) {
        if (!FileUtils.isFileExists(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        return BaseResponse.getSuccessResponse(itemId);
    }

    /**
     * Попытаться взять блокировку на файл (временно файл, потом будет таблица в БД)
     * @param itemId - путь к файлу
     */
    @GetMapping("/grab")
    public BaseResponse grabLock(@RequestParam(value = "itemId") String itemId) {
        if (!FileUtils.isFileExists(itemId) || hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.create(itemId, itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    /**
     * Попытаться отдать взятую клиентом блокировку
     * @param itemId - путь в взятому в блокировку файлу
     */
    @GetMapping("/giveback")
    public BaseResponse giveLockBack(@RequestParam(value = "itemId") String itemId) {
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