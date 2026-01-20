module YAJL {
    requires YAJSI;
    requires static lombok;
    requires org.jetbrains.annotations;
    requires java.desktop;

    exports com.toxicstoxm.YAJL.old;
    exports com.toxicstoxm.YAJL.old.tools;
    exports com.toxicstoxm.YAJL.old.level;
    exports com.toxicstoxm.YAJL.old.placeholders;
    exports com.toxicstoxm.YAJL.old.config;
}