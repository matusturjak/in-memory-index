package sk.matusturjak.inmemoryindex.dto;

import sk.matusturjak.inmemoryindex.service.SomeService;

public class TwoThreeObject implements Comparable<TwoThreeObject> {
    private Comparable id;
    private Comparable atr;

    public TwoThreeObject(Comparable id, Comparable atr) {
        this.id = id;
        this.atr = atr;
    }

    public Comparable getId() {
        return id;
    }

    public void setId(Comparable id) {
        this.id = id;
    }

    public Comparable getAtr() {
        return atr;
    }

    public void setAtr(Comparable atr) {
        this.atr = atr;
    }

    @Override
    public int compareTo(TwoThreeObject o) {
        int compareByAtr = this.atr.compareTo(o.atr);

        if (compareByAtr == 0) {
            return this.id.compareTo(o.id);
        }
        return compareByAtr;
    }
}
