package mg.itu.framework.model;

import java.util.Map;

public class ModelView {
    private String jspName;

    Map<String, Object> data = new java.util.HashMap<>();
    public String getJspName() {
        return jspName;
    }

    public void setJspName(String jspName) {
        this.jspName = jspName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    public void addItem(String key, Object value) {
        data.put(key, value);
    }
}
