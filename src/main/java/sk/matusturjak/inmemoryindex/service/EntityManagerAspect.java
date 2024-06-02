package sk.matusturjak.inmemoryindex.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import sk.matusturjak.inmemoryindex.dto.TwoThreeObject;

@Aspect
public class EntityManagerAspect {

    private AppIndexes appIndexes;

    public EntityManagerAspect(AppIndexes appIndexes) {
        this.appIndexes = appIndexes;
    }

    @Around("execution(* jakarta.persistence.EntityManager.persist(..)) && args(entity)")
    public Object persistEntityToIndexesAdvice(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {
        addEntityToIndex(entity);

        return joinPoint.proceed();
    }

    @Around("execution(* jakarta.persistence.EntityManager.merge(..)) && args(entity)")
    public Object mergeEntityToIndexesAdvice(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {
        addEntityToIndex(entity);

        return joinPoint.proceed();
    }

    @Around("execution(* jakarta.persistence.EntityManager.remove(..)) && args(entity)")
    public Object removeDataToIndexesAdvice(ProceedingJoinPoint joinPoint, Object entity) throws Throwable {
        appIndexes.getAppIndexes().forEach(entityIndexesData -> {
            try {
                Class cls = Class.forName(entityIndexesData.getEntityName());

                if (cls.isInstance(entity)) {
                    entityIndexesData.getIndexes().forEach((field, twoThreeTree) -> {
                        try {
                            Comparable idFieldData = (Comparable) entity.getClass().getDeclaredField(entityIndexesData.getIdFieldName()).get(entity);
                            Comparable fieldData = (Comparable) entity.getClass().getDeclaredField(field).get(entity);

                            twoThreeTree.deleteData(new TwoThreeObject(idFieldData, fieldData));
                        } catch (IllegalAccessException | NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return joinPoint.proceed();
    }

    private void addEntityToIndex(Object entity) {
        appIndexes.getAppIndexes().forEach(entityIndexesData -> {
            try {
                Class cls = Class.forName(entityIndexesData.getEntityName());

                if (cls.isInstance(entity)) {
                    entityIndexesData.getIndexes().forEach((field, twoThreeTree) -> {
                        try {
                            Comparable idFieldData = (Comparable) entity.getClass().getDeclaredField(entityIndexesData.getIdFieldName()).get(entity);
                            Comparable fieldData = (Comparable) entity.getClass().getDeclaredField(field).get(entity);

                            twoThreeTree.insertData(new TwoThreeObject(idFieldData, fieldData));
                        } catch (IllegalAccessException | NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
