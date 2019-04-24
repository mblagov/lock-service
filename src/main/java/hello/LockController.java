package hello;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locker")
public class LockController {

    private final String sharedKey = "SHARED_KEY";

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final int CODE_SUCCESS = 100;
    private static final int AUTH_FAILURE = 102;

    @GetMapping
    public BaseResponse showStatus() {
        return new BaseResponse(SUCCESS_STATUS, 1);
    }

    @PostMapping("/lock")
    public BaseResponse lock(@RequestParam(value = "key") String key, @RequestBody LockRequest request) {

        final BaseResponse response;

        if (sharedKey.equalsIgnoreCase(key)) {
            boolean lock = request.getLock();
            String itemId = request.getItemId();
            response = new BaseResponse(SUCCESS_STATUS, CODE_SUCCESS);
        } else {
            response = new BaseResponse(ERROR_STATUS, AUTH_FAILURE);
        }
        return response;
    }
}