package com.toxicstoxm.YAJL.areas;




import static com.toxicstoxm.YAJL.YAJLSettingsBundle.*;

import java.awt.*;
import java.util.List;
import java.util.*;

public class YAJLLogAreaManger implements LogAreaManager {
    public static YAJLLogAreaMap registeredAreas;

    public YAJLLogAreaManger() {
        registeredAreas = new YAJLLogAreaMap();
    }

    @Override
    public void registerArea(LogArea logArea) {
        if (ShownAreas.getInstance().get().contains(logArea.getName().toUpperCase())) registeredAreas.registerArea(logArea);
    }

    @Override
    public Color getAreaColor(String area) {
        return registeredAreas.containsArea(area) ? registeredAreas.getColorOfArea(area) : new Color(0, 0, 0, 0);
    }

    @Override
    public boolean enableArea(String area) {
        return registeredAreas.enableArea(area);
    }

    @Override
    public boolean disableArea(String area) {
        return registeredAreas.disableArea(area);
    }

    @Override
    public boolean isAreaEnabled(String area) {
        return registeredAreas.isAreaEnabled(area) || registeredAreas.isAreaEnabled("ALL");
    }

    @Override
    public boolean isAreaEnabled(LogArea area) {
        return LogAreaManager.super.isAreaEnabled(area) || registeredAreas.isAreaEnabled(area);
    }

    @Override
    public LogAreaConfigurator configureArea(String area) {
        if (!registeredAreas.containsArea(area)) return null;
        return new LogAreaConfigurator() {
            @Override
            public LogArea getArea() {
                return registeredAreas.get(area);
            }

            @Override
            public void setColor(Color color) {
                getArea().setColor(color);
            }

            @Override
            public void setParents(Collection<String> parents) {
                getArea().setParents(parents.stream().toList());
            }

            @Override
            public void addParents(Collection<String> parents) {
                List<String> currentParents = getArea().getParents();
                currentParents.addAll(parents);
                getArea().setParents(currentParents);
            }

            @Override
            public void setAreaParents(Collection<LogArea> parents) {
                List<String> newParents = new ArrayList<>();
                parents.forEach(logArea -> newParents.add(logArea.getName()));
                setParents(newParents);
            }

            @Override
            public void addAreaParents(Collection<LogArea> parents) {
                List<String> newParents = new ArrayList<>();
                parents.forEach(logArea -> newParents.add(logArea.getName()));
                addParents(newParents);
            }
        };
    }

    @Override
    public boolean unregisterArea(LogArea logArea) {
        return registeredAreas.unregisterArea(logArea);
    }

    @Override
    public Set<Map.Entry<String, LogArea>> entrySet() {
        return registeredAreas.entrySet();
    }
}
