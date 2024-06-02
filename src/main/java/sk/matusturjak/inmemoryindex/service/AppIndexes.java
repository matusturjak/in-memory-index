package sk.matusturjak.inmemoryindex.service;

import org.springframework.stereotype.Component;
import sk.matusturjak.inmemoryindex.dto.EntityIndexesData;
import sk.matusturjak.tree.TwoThreeTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AppIndexes {

    private List<EntityIndexesData> appIndexes;

    public AppIndexes() {
        this.appIndexes = new ArrayList<>();
    }

    public void addEntityIndexes(String idFieldName, String entityName, Map<String, TwoThreeTree> indexes) {
        appIndexes.add(
            new EntityIndexesData(idFieldName, entityName, indexes)
        );
    }

    public List<EntityIndexesData> getAppIndexes() {
        return appIndexes;
    }
}
