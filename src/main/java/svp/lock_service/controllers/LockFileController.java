package svp.lock_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import svp.lock_service.models.BaseResponse;

public interface LockFileController {

    /**
     * Проверить файл на наличиие активной блокировки
     *
     * @param itemId - путь к файлу, который хочется проверить
     */
    @GetMapping("/exists")
    public BaseResponse lookAtLock(@RequestParam(value = "itemId") String itemId);

    /**
     * Попытаться взять блокировку на файл
     *
     * @param itemId - путь к файлу
     */
    @GetMapping("/grab")
    public BaseResponse grabLock(@RequestParam(value = "itemId") String itemId);

    /**
     * Попытаться отдать взятую клиентом блокировку
     *
     * @param itemId - путь в взятому в блокировку файлу
     */
    @GetMapping("/giveback")
    public BaseResponse giveLockBack(@RequestParam(value = "itemId") String itemId);
}
