package svp.lock_service.models;

import org.json.JSONObject;

public class BaseResponse {

    private Status status;
    private String itemId;

    public static BaseResponse fromJSON(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        BaseResponse baseResponse = new BaseResponse();

        baseResponse.status = Status.valueOf(jsonObject.getString("status"));
        baseResponse.itemId = jsonObject.getString("itemId");
        return baseResponse;
    }

    public BaseResponse() {
    }

    public BaseResponse(Status status, String itemId) {
        this.status = status;
        this.itemId = itemId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCode() {
        return itemId;
    }

    public String getItemId() {
        return itemId;
    }

    public static BaseResponse getSuccessResponse(String itemId) {
        return new BaseResponse(Status.SUCCESS, itemId);
    }

    public static BaseResponse getErrorResponse(String itemId) {
        return new BaseResponse(Status.ERROR, itemId);
    }
}
