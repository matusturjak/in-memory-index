package sk.matusturjak.inmemoryindex.processor;

import com.google.auto.service.AutoService;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.matusturjak.inmemoryindex.annotations.FieldIndex;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("sk.matusturjak.inmemoryindex.annotations.FieldIndex")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class FieldIndexProcessor extends AbstractProcessor {

    private Logger log = LoggerFactory.getLogger(FieldIndexProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> entityClasses = roundEnv.getElementsAnnotatedWith(Entity.class);

        Map<Element, List<Element>> map = new HashMap<>();

        entityClasses.forEach(entityClassElement -> {
            entityClassElement.getEnclosedElements().forEach(enclosedElement -> {
                if (enclosedElement.getAnnotation(Id.class) != null) {
                    log.info("There is ID annotation");
                }
                if (enclosedElement.getKind().isField() && enclosedElement.getAnnotation(FieldIndex.class) != null) {
                    List<Element> fields = null;
                    if (!map.containsKey(entityClassElement)) {
                        fields = new LinkedList<>();
                    } else {
                        fields = map.get(entityClassElement);
                    }

                    fields.add(enclosedElement);
                    map.put(entityClassElement, fields);
                }
            });
        });

        buildClasses(map);

        return false;
    }

    private void buildClasses(Map<Element, List<Element>> map) {
        map.forEach((classElement, fieldElements) -> {
            StringBuilder model = new StringBuilder();

            String packageName = "sk.matusturjak.inmemoryindex"; // TODO
            String entityName = processingEnv.getElementUtils().getPackageOf(classElement).toString() + "." + classElement.getSimpleName();
            String generatedClassName = classElement.getSimpleName() + "IndexImpl";

            log.info(fieldElements.toString());

            String idFieldName = "\"" + classElement
                .getEnclosedElements()
                .stream()
                .filter(field -> field.getKind().isField() && field.getAnnotation(Id.class) != null)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Id field not found."))
                .getSimpleName().toString() + "\"";

            String fieldNames = "new LinkedList<String>(Arrays.asList(" + fieldElements.stream()
                .filter(field -> field.getAnnotation(Id.class) == null)
                .map(field -> "\"" + field.getSimpleName().toString() + "\"")
                .collect(Collectors.joining(",")) + "))";

            model.append("package ").append(packageName).append(";\n\n");
            model.append("import jakarta.persistence.EntityManager").append(";\n");
            model.append("import jakarta.persistence.PersistenceContext").append(";\n");
            model.append("import org.springframework.stereotype.Service").append(";\n");
            model.append("import structure.TwoThreeTree").append(";\n"); // TODO sk.matusturjak
            model.append("import sk.matusturjak.inmemoryindex.service.FillIndexService").append(";\n");
            model.append("import java.util.Arrays").append(";\n");
            model.append("import java.util.List").append(";\n");
            model.append("import java.util.LinkedList").append(";\n");
            model.append("import java.util.Map").append(";\n\n");

            model.append("@Service\n");
            model.append("public class ").append(generatedClassName).append(" {\n\n");

            model.append("\t@PersistenceContext\n");
            model.append("\tprivate EntityManager entityManager;\n\n");

            model.append("\tprivate FillIndexService fillIndexService;\n\n");

            model.append("\tprivate Map<String, TwoThreeTree> indexes;\n\n");

            // constructor
            model.append(
                String.format("\tpublic %s(FillIndexService fillIndexService) {\n", generatedClassName)
            );
            model.append("\t\tthis.fillIndexService = fillIndexService;\n");
            model.append(
                String.format("\t\tthis.indexes = this.fillIndexService.fillIndexes(%s, %s, %s);\n", idFieldName, fieldNames, "\"" + entityName + "\"")
            );
            model.append("\t}\n\n");

            fieldElements.forEach(field -> {
                String fieldName = field.getSimpleName().toString();
                String fieldType = field.asType().toString();

                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

//                String searchInTree = ""
//                String selectToDB = String.format(
//                    "\t\tString select = \"select obj from %s where %s in(%s)\";\n",
//                    entityName, "obj." + fieldName,
//                );
                model.append(
                    String.format("\tpublic List<%s> %s(%s objParam) { \n\t\treturn null;\n\t}\n\n", entityName, getterName, fieldType)
                );
            });
            model.append("}\n");


            try {
                Writer writer = processingEnv.getFiler()
                    .createSourceFile(packageName + "." + generatedClassName)
                    .openWriter();
                writer.write(model.toString());
                writer.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }
}
