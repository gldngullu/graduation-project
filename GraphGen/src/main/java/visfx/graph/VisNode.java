package visfx.graph;

import com.google.gson.Gson;

public class VisNode {
    private long id;
    private String label;
    private String group;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public VisNode(long id, String label, String group) {
        this.id = id;
        this.label = label;
        this.group = group;
    }

    public VisNode(long id, String label) {
        this.id = id;
        this.label = label;
        this.group = group;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
