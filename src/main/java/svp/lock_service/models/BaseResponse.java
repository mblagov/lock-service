package svp.lock_service.models;

import org.json.JSONObject;

public class BaseResponse {

    private Status status;
    private String itemId;
    private String comment;

    public static BaseResponse fromJSON(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        BaseResponse baseResponse = new BaseResponse();

        baseResponse.status = Status.valueOf(jsonObject.getString("status"));
        baseResponse.itemId = jsonObject.getString("itemId");
        baseResponse.comment = jsonObject.getString("comment");
        return baseResponse;
    }

    public BaseResponse() {
    }

    public BaseResponse(Status status, String itemId, String comment) {
        this.status = status;
        this.itemId = itemId;
        this.comment = comment;
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

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static BaseResponse getSuccessResponse(String itemId) {
        return new BaseResponse(Status.SUCCESS, itemId, "");
    }

    public static BaseResponse getErrorResponse(String itemId) {
        return getErrorResponse(itemId, "");
    }

    public static BaseResponse getErrorResponse(String itemId, String comment) {
        return new BaseResponse(Status.ERROR, itemId, comment);
    }
}
