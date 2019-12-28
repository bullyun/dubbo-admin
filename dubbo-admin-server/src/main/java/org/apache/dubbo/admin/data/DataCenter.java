package org.apache.dubbo.admin.data;

import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.admin.model.dto.ConfigDTO;
import org.apache.dubbo.admin.registry.config.GovernanceConfiguration;
import org.apache.dubbo.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
* 冗余的数据存储，会存在不同步的问题。宁愿列表有、数据不存在；也不能数据存在，而列表看不到。
* 所以：添加的时候，要先添加冗余数据；删除的时候，后删除冗余数据
*
* */

@Service
public class DataCenter {

    protected final String ADMIN_DATA_KEY = "config/dubbo-admin/data.json";

    @Autowired
    protected GovernanceConfiguration dynamicConfiguration;

    protected boolean loadDB = false;
    protected JSONObject db = new JSONObject();

    private void load(){
        if (loadDB == false){
            String json = dynamicConfiguration.getConfig(ADMIN_DATA_KEY);
            if (StringUtils.isNotEmpty(json)){
                db = JSONObject.parseObject(json);
            }
            loadDB = true;
        }
    }

    private void save(){
        dynamicConfiguration.setConfig(ADMIN_DATA_KEY, db.toJSONString());
    }

    private List<String> getChildObject(String key){
        load();
        String child = db.getString(key);
        if (StringUtils.isNotEmpty(child)){
            return JSONObject.parseArray(child, String.class);
        }
        return new ArrayList<>();
    }

    private void setChildObject(String key, List<String> objs){
        objs.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        String json = JSONObject.toJSONString(objs);
        db.put(key, JSONObject.parseArray(json));
        save();
    }

    public void setConfig(String group, String key){
        List<String> keys = getChildObject(group);
        if (keys.indexOf(key) < 0){
            keys.add(key);
        }
        setChildObject(group, keys);
    }

    public void deleteConfig(String group, String key){
        List<String> keys = getChildObject(group);
        keys.remove(key);
        setChildObject(group, keys);
    }

    public List<String> getConfigs(String group){
        return getChildObject(group);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    public void setConfigDTO(String key){
        setConfig("ConfigDTO", key);
    }

    public void deleteConfigDTO(String key){
        deleteConfig("ConfigDTO", key);
    }

    public List<String> getConfigDTOs(){
        return getConfigs("ConfigDTO");
    }


    public void setConditionRouteDTO(String key){
        setConfig("ConditionRouteDTO", key);
    }

    public void deleteConditionRouteDTO(String key){
        deleteConfig("ConditionRouteDTO", key);
    }

    public List<String> getConditionRouteDTOs(){
        return getConfigs("ConditionRouteDTO");
    }
}
