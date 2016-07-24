package cz.cuni.lf1.lge.ThunderSTORM.util;

enum class PluginCommands(val value: String) {
    CAMERA_SETUP("Camera setup"),
    RENDERING("Visualization"),
    IMPORT_RESULTS("Import results"),
    EXPORT_RESULTS("Export results"),
    IMPORT_GT("Import ground-truth"),
    EXPORT_GT("Export ground-truth"),
    PERFORMANCE_EVALUATION("Performance evaluation")
}
