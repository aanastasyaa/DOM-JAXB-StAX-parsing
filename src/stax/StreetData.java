package stax;

import java.util.HashSet;

/**
 * Представляет собой информацию о улице.
 * Хранит количество ее кусков, ID,
 * множество использованных классов дороги (primary, secondary, tertiary и т. д. – значения тега highway)
 */
public class StreetData {
    private long ID;
    private int parts;
    private HashSet<String> typeRoad;

    public StreetData(long ID, String typeRoad) {
        this.ID=ID;
        this.typeRoad=new HashSet<String>();
        this.typeRoad.add(typeRoad);
        parts=1;
    }

    public StreetData(long ID) {
        this.ID=ID;
        typeRoad=new HashSet<String>();
        parts=1;
    }

    public void addPartStreet(String typeRoad) {
        parts++;
        this.typeRoad.add(typeRoad);
    }

    @Override
    public String toString() {
        return Long.toString(ID)+"\t"+parts+"\t"+typeRoad.toString();
    }
}
