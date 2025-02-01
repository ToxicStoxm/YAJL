module YAJL {
    requires YAJSI;
    requires static lombok;
    requires org.jetbrains.annotations;
    requires java.desktop;

    exports com.toxicstoxm.YAJL;
    exports com.toxicstoxm.YAJL.tools;
    exports com.toxicstoxm.YAJL.level;
    exports com.toxicstoxm.YAJL.area;
    exports com.toxicstoxm.YAJL.placeholders;
    exports com.toxicstoxm.YAJL.config;
}