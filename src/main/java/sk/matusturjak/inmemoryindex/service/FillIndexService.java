package sk.matusturjak.inmemoryindex.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.matusturjak.inmemoryindex.dto.TwoThreeObject;
import sk.matusturjak.tree.TwoThreeTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FillIndexService {

    @PersistenceContext
    private EntityManager entityManager;

    private AppIndexes appIndexes;

    public FillIndexService(AppIndexes appIndexes) {
        this.appIndexes = appIndexes;
    }

    @Transactional
    public Map<String, TwoThreeTree> fillIndexes(String idFieldName, List<String> fieldNames, String entityClazzName) {
        Map<String, TwoThreeTree> indexes = new HashMap<>();

        fieldNames.forEach(name -> {
            indexes.put(name, new TwoThreeTree<>());
        });

        fieldNames.add(0, idFieldName);

        String names = fieldNames
            .stream()
            .map(columnName -> "obj." + columnName)
            .collect(Collectors.joining(","));

        String query = "SELECT " + names + " from " + entityClazzName + " obj";

        Stream<Object[]> resultStream = (Stream<Object[]>) entityManager.createQuery(query).getResultStream();

        resultStream.forEach(obj -> {
            for (int i = 1; i < obj.length; i++) {
                TwoThreeObject dto = new TwoThreeObject((Comparable) obj[0], (Comparable) obj[i]);
                String fieldName = fieldNames.get(i);
                indexes.get(fieldName).insertData(dto);
            }
        });

        appIndexes.addEntityIndexes(idFieldName, entityClazzName, indexes);
        return indexes;
    }
}
