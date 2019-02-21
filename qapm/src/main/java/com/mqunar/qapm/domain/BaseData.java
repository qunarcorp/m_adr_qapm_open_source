package com.mqunar.qapm.domain;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by pengchengpc.liu on 2018/11/22.
 */
public interface BaseData extends Serializable{
    JSONObject toJSONObject() ;
    String toString() ;
}
