package com.sap.ims.isa.jcomigrationhelper.i18n;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String   BUNDLE_NAME              = "com.sap.ims.isa.jcomigrationhelper.i18n.messages";
    private static ResourceBundle bundleForConstructedKeys = ResourceBundle.getBundle(BUNDLE_NAME);
    public static String          marker_counter_btn_ok;
    public static String          marker_counter_msg_result;
    public static String          marker_counter_msg_title;
    public static String          marker_gen_error_in_task;
    public static String          marker_gen_info_not_generated_title;
    public static String          marker_gen_info_not_generated_type_not_supported;
    public static String          marker_title;
    public static String          marker_done_title;
    public static String          marker_warn_docline_not_retrieved;
    public static String          marker_warn_update_failed;
    public static String          marker_warn_update_failed_log;
    public static String          marker_warn_update_title;
    public static String          markers_error_createmarkers_msg_title;
    public static String          markers_error_createmarkers_msg_content;
    public static String          markers_error_getting_cus;
    public static String          markers_error_getting_cus_log;
    public static String          markers_error_getting_packages;
    public static String          markers_error_getting_packages_log;
    public static String          markers_error_getting_src_folders;
    public static String          markers_error_getting_src_folders_log;
    public static String          markers_error_finding_markers;
    public static String          switch_operation_error_apply_changes;
    public static String          switch_operation_error_apply_changes_log;
    public static String          switch_operation_error_init_switch_failed;
    public static String          switch_operation_msg_error_title;
    public static String          switch_operation_nothing_changed_content;
    public static String          switch_operation_nothing_changed_title;
    public static String          switch_operation_parameter_init_failed_log;
    public static String          warn_no_javaelement_found;
    public static String          warn_type_not_determineable;
    public static String          markers_warning_marker_not_removed;
    public static String          markers_error_removingmarkers_msg_title;
    public static String          markers_error_title;
    public static String          task_output_cancel_requested;
    public static String          task_output_starting_generation;

    public static String          task_output_starting_generation_for_compilation_unit;
    public static String          task_output_starting_generation_for_package;
    public static String          task_output_starting_generation_for_project;

    public static String          imports_gen_error_in_task;
    public static String          imports_gen_info_not_generated_title;
    public static String          imports_gen_info_not_generated_type_not_supported;

    public static String          imports_error_title;

    public static String          marker_title_import;
    public static String          task_output_imports_starting_orga;
    public static String          task_output_imports_starting_orga_for_compilation_unit;
    public static String          task_output_imports_starting_orga_for_package;
    public static String          task_output_imports_starting_orga_for_project;
    public static String          task_output_imports_error_apply_changes_title;
    public static String          task_output_imports_error_apply_changes_msg;
    public static String          task_output_imports_error_create_doc_instance;
    public static String          task_output_imports_error_create_workingcopy;

    public static String          switching_params_process_error_in_task;
    public static String          switching_params_process_error_in_task_title;
    public static String          task_output_switching_params_for_compilation_unit;

    public static String          asserts_error_not_null_doc;
    public static String          asserts_error_not_null_resource;

    static {
        reloadMessages();
    }

    public static void reloadMessages() {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // No instances required!
    }

    public static ResourceBundle getBundleForConstructedKeys() {
        return bundleForConstructedKeys;
    }
}
