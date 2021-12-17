package com.tanhua.commons.exceptions;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author user_Chubby
 * @date 2021/5/3
 * @Description
 */
@NoArgsConstructor
@Data
public class TanHuaException extends RuntimeException{
    private Object errData;

    public TanHuaException(String errMessage){
        super(errMessage);
    }

    public TanHuaException(Object data){
        super();
        this.errData = data;
    }


}
