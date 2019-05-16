package svp.lock_service.models;

public class BaseResponse {

    private final Status status;
    private final String itemId;

    public BaseResponse(Status status, String itemId) {
        this.status = status;
        this.itemId = itemId;
    }

    public Status getStatus() {
        return status;
    }

    public String getCode() {
        return itemId;
    }

    public static BaseResponse getSuccessResponse(String itemId) {
        return new BaseResponse(Status.SUCCESS, itemId);
    }

    public static BaseResponse getErrorResponse(String itemId) {
        return new BaseResponse(Status.ERROR, itemId);
    }
}
