package svp.lock_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import svp.lock_service.models.BaseResponse;

import java.sql.SQLException;

public interface LockTableController {

    /**
     * Проверить таблицу на наличиие активной блокировки
     *
     * @param itemId - имя таблице, которую мы хотим проверить
     */
    @GetMapping("/exists")
    public BaseResponse lookAtLock(@RequestParam(value = "itemId") String itemId) throws SQLException;

    /**
     * Попытаться взять блокировку на таблицу
     *
     * @param itemId - имя таблицы
     */
    @GetMapping("/grab")
    public BaseResponse grabLock(@RequestParam(value = "itemId") String itemId) throws SQLException;

    /**
     * Попытаться отдать взятую клиентом блокировку
     *
     * @param itemId - имя заблокированной таблицы
     */
    @GetMapping("/giveback")
    public BaseResponse giveLockBack(@RequestParam(value = "itemId") String itemId) throws SQLException;
}
