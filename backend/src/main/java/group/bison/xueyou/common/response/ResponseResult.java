package group.bison.xueyou.common.response;

import java.io.Serializable;
import java.util.Objects;

public class ResponseResult<T> implements Serializable {

    private T data;

    private Integer code = 0;

    private String message = "成功";

    public ResponseResult() {};

    public ResponseResult(Integer returnCode) {
        this.data = null;
        this.code = returnCode;
    }
    public ResponseResult(Integer returnCode, String message) {
        this.data = null;
        this.code = returnCode;
        this.message = message;
    }

    public ResponseResult(T data) {
        this.data = data;
        this.code = 0;
        this.message = "成功";
    }

    public boolean success() {
        return Objects.equals(this.code, 0);
    }

    public T getData() {
        return data;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
