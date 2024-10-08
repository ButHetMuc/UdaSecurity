module com.udacity.capoint.security {
    requires com.google.common;
    requires com.google.gson;
    requires com.udacity.catpoint.images;
    requires com.miglayout.swing;
    requires java.desktop;
    requires java.prefs;
    opens com.udacity.catpoint.security.data to com.google.gson;
}