package mg.itu.framework.model;

import java.util.HashMap;
import java.util.Map;

/**
 * SPRINT 3 : ModelView - Transport de données vers les JSP
 * 
 * Permet aux contrôleurs de retourner des données et le nom de la vue.
 * 
 * @version Sprint 3 - 1 novembre 2024
 */
public class ModelView {
    private String jspName;
    private Map<String, Object> data;
    
    public ModelView() {
        this.data = new HashMap<>();
    }
    
    public ModelView(String jspName) {
        this.jspName = jspName;
        this.data = new HashMap<>();
    }
    
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
    
    /**
     * Ajoute une donnée au ModelView
     */
    public void addItem(String key, Object value) {
        this.data.put(key, value);
    }
}