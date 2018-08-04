package com.example.tarun.apna_cafe_server.Common;

import com.example.tarun.apna_cafe_server.Model.Request;
import com.example.tarun.apna_cafe_server.Model.User;

public class Common {
    public static User currentUser;

    public static Request currentRequest;

    public static final String UPDATE = "Update";

    public static final String DELETE = "Delete";

    public static final int Image_pick_request = 72;

    public static String convertCodeStatus(String code)
    {
        if(code.equals ( "0" ))
            return "Placed";
        else if(code.equals ( "1" ))
            return "On my way";
        else
            return "Shipped";
    }
}
