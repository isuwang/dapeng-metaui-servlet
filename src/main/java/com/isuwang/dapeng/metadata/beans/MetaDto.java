package com.isuwang.dapeng.metadata.beans;

import com.isuwang.dapeng.core.metadata.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by wahsonleung on 2017/8/5.
 */
public class MetaDto {
    private Service serviceMeta;
    private Map<String, List<FieldMeta>> fieldsInStruct;
    public Service getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(Service serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public Map<String, List<FieldMeta>> getFieldsInStruct() {
        return fieldsInStruct;
    }

    public void setFieldsInStruct(Map<String, List<FieldMeta>> fieldsInStruct) {
        this.fieldsInStruct = fieldsInStruct;
    }
}
