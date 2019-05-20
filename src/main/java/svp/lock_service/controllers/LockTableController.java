package svp.lock_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import svp.lock_service.common.TableUtils;
import svp.lock_service.models.BaseResponse;
import svp.lock_service.zk.ZKManagerImpl;

import java.sql.*;

@RestController
@RequestMapping("/tableLocker")
public class LockTableController {
    @Autowired
    private ZKManagerImpl zkManager;
    private static String driverName = "org.apache.hive.jdbc.HiveDriver";

    /**
     * Проверить таблицу на наличиие активной блокировки
     * @param itemId - имя таблице, которую мы хотим проверить
     */
    @GetMapping("/exists")
    public BaseResponse lookAtLock(@RequestParam(value = "itemId") String itemId) throws SQLException {
        if(!TableUtils.isTableExists(itemId)){
            return BaseResponse.getErrorResponse(itemId);
        }
        return BaseResponse.getSuccessResponse(itemId);
    }

    /**
     * Попытаться взять блокировку на таблицу
     * @param itemId - имя таблицы
     */
    @GetMapping("/grab")
    public BaseResponse grabLock(@RequestParam(value = "itemId") String itemId) throws SQLException{
        if (!TableUtils.isTableExists(itemId) || hasAlreadyLocked(itemId)) {
            return BaseResponse.getErrorResponse(itemId);
        }
        zkManager.create(itemId, itemId);
        return BaseResponse.getSuccessResponse(itemId);
    }

    /**
     * Попытаться отдать взятую клиентом блокировку
     * @param itemId - имя заблокированной таблицы
     */
    @GetMapping("/giveback")
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
