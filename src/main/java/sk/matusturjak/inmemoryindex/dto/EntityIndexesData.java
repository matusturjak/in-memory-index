package sk.matusturjak.inmemoryindex.dto;

import sk.matusturjak.tree.TwoThreeTree;

import java.util.Map;

public class EntityIndexesData {
    private String idFieldName;
    private String entityName;
    private Map<String, TwoThreeTree> indexes;

    public EntityIndexesData(String idFieldName, String entityName, Map<String, TwoThreeTree> indexes) {
        this.idFieldName = idFieldName;
        this.entityName = entityName;
        this.indexes = indexes;
    }

    public String getIdFieldName() {
        return idFieldName;
    }

    public String getEntityName() {
        return entityName;
    }

    public Map<String, TwoThreeTree> getIndexes() {
        return indexes;
    }
}
