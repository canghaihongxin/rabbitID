package com.bdf.rabbitId.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * author: 田培融
 */
@Data
@AllArgsConstructor
public class Result {

    private long id;

    private Status status;

    public static Result fail(){ return new Result(-1,Status.FAILURE);}

    public static Result ok(long id){return new Result(id,Status.SUCCESS);}

    public boolean isSuccess(){
        return this.status == Status.SUCCESS;
    }


    enum Status {
        SUCCESS, FAILURE
    }
}
