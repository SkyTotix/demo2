package com.example.demo2.utils;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
import javafx.scene.paint.Color;

/**
 * Clase helper para iconos del sistema usando Ikonli con FontAwesome5
 * Proporciona iconos profesionales de FontAwesome 5
 */
public class IconHelper {
    
    // Tamaños estándar de iconos
    public static final int SMALL_SIZE = 16;
    public static final int MEDIUM_SIZE = 20;
    public static final int LARGE_SIZE = 24;
    public static final int XLARGE_SIZE = 28;
    
    // Colores del proyecto - públicos para uso en toda la aplicación
    public static final String PRIMARY_COLOR = "#3B82F6";
    public static final String SECONDARY_COLOR = "#64748B";
    public static final String SUCCESS_COLOR = "#10B981";
    public static final String ERROR_COLOR = "#EF4444";
    public static final String WARNING_COLOR = "#F59E0B";
    
    // === ICONOS PARA ESTADÍSTICAS ===
    public static FontIcon getAdminIcon() {
        return createIcon(FontAwesomeSolid.USER_TIE, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getLibrarianIcon() {
        return createIcon(FontAwesomeSolid.USER_COG, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getUsersIcon() {
        return createIcon(FontAwesomeSolid.USERS, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getDatabaseIcon() {
        return createIcon(FontAwesomeSolid.DATABASE, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    // === ICONOS PARA MENÚ ===
    public static FontIcon getHomeIcon() {
        return createIcon(FontAwesomeSolid.HOME, MEDIUM_SIZE, SECONDARY_COLOR);
    }
    
    public static FontIcon getSettingsIcon() {
        return createIcon(FontAwesomeSolid.COG, MEDIUM_SIZE, SECONDARY_COLOR);
    }
    
    public static FontIcon getBookIcon() {
        return createIcon(FontAwesomeSolid.BOOK, MEDIUM_SIZE, SECONDARY_COLOR);
    }
    
    public static FontIcon getLoanIcon() {
        return createIcon(FontAwesomeSolid.BOOK_READER, MEDIUM_SIZE, SECONDARY_COLOR);
    }
    
    public static FontIcon getSearchIcon() {
        return createIcon(FontAwesomeSolid.SEARCH, MEDIUM_SIZE, SECONDARY_COLOR);
    }
    
    public static FontIcon getReportIcon() {
        return createIcon(FontAwesomeSolid.CHART_BAR, MEDIUM_SIZE, SECONDARY_COLOR);
    }
    
    // === ICONOS PARA NOTIFICACIONES Y ESTADOS ===
    public static FontIcon getNotificationIcon() {
        return createIcon(FontAwesomeSolid.BELL, MEDIUM_SIZE, SECONDARY_COLOR);
    }
    
    public static FontIcon getSuccessIcon() {
        return createIcon(FontAwesomeSolid.CHECK_CIRCLE, MEDIUM_SIZE, SUCCESS_COLOR);
    }
    
    public static FontIcon getErrorIcon() {
        return createIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE, MEDIUM_SIZE, ERROR_COLOR);
    }
    
    public static FontIcon getWarningIcon() {
        return createIcon(FontAwesomeSolid.EXCLAMATION_CIRCLE, MEDIUM_SIZE, WARNING_COLOR);
    }
    
    public static FontIcon getInfoIcon() {
        return createIcon(FontAwesomeSolid.INFO_CIRCLE, MEDIUM_SIZE, PRIMARY_COLOR);
    }
    
    // === ICONOS PARA ACTIVIDADES ===
    public static FontIcon getReturnIcon() {
        return createIcon(FontAwesomeSolid.UNDO, MEDIUM_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getAddIcon() {
        return createIcon(FontAwesomeSolid.PLUS_CIRCLE, MEDIUM_SIZE, SUCCESS_COLOR);
    }
    
    public static FontIcon getEditIcon() {
        return createIcon(FontAwesomeSolid.EDIT, MEDIUM_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getDeleteIcon() {
        return createIcon(FontAwesomeSolid.TRASH, MEDIUM_SIZE, ERROR_COLOR);
    }
    
    // === ICONOS PARA ACCIONES RÁPIDAS ===
    public static FontIcon getNewAdminIcon() {
        return createIcon(FontAwesomeSolid.USER_PLUS, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getNewLibrarianIcon() {
        return createIcon(FontAwesomeSolid.USER_PLUS, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getNewUserIcon() {
        return createIcon(FontAwesomeSolid.USER_PLUS, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getNewBookIcon() {
        return createIcon(FontAwesomeSolid.BOOK_MEDICAL, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getNewLoanIcon() {
        return createIcon(FontAwesomeSolid.BOOK_OPEN, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getDevolutionIcon() {
        return createIcon(FontAwesomeSolid.HAND_HOLDING, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    public static FontIcon getSearchBookIcon() {
        return createIcon(FontAwesomeSolid.SEARCH, LARGE_SIZE, PRIMARY_COLOR);
    }
    
    // === ICONOS ADICIONALES ===
    public static FontIcon getLogoutIcon() {
        return createIcon(FontAwesomeSolid.SIGN_OUT_ALT, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getLoginIcon() {
        return createIcon(FontAwesomeSolid.SIGN_IN_ALT, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getKeyIcon() {
        return createIcon(FontAwesomeSolid.KEY, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getEmailIcon() {
        return createIcon(FontAwesomeSolid.ENVELOPE, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getPasswordIcon() {
        return createIcon(FontAwesomeSolid.LOCK, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getExportIcon() {
        return createIcon(FontAwesomeSolid.DOWNLOAD, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getImportIcon() {
        return createIcon(FontAwesomeSolid.UPLOAD, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getRefreshIcon() {
        return createIcon(FontAwesomeSolid.SYNC, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getFilterIcon() {
        return createIcon(FontAwesomeSolid.FILTER, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getCalendarIcon() {
        return createIcon(FontAwesomeSolid.CALENDAR_ALT, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getClockIcon() {
        return createIcon(FontAwesomeSolid.CLOCK, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getSecurityIcon() {
        return createIcon(FontAwesomeSolid.SHIELD_ALT, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getTestIcon() {
        return createIcon(FontAwesomeSolid.FLASK, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getRocketIcon() {
        return createIcon(FontAwesomeSolid.ROCKET, MEDIUM_SIZE, null);
    }
    
    public static FontIcon getPlugIcon() {
        return createIcon(FontAwesomeSolid.PLUG, MEDIUM_SIZE, null);
    }
    
    // === MÉTODOS HELPER ===
    
    /**
     * Crea un icono con los parámetros especificados
     */
    public static FontIcon createIcon(org.kordamp.ikonli.Ikon iconCode, int size, String color) {
        FontIcon icon = FontIcon.of(iconCode, size);
        if (color != null) {
            icon.setIconColor(Color.web(color));
        }
        return icon;
    }
    
    /**
     * Crea un icono con tamaño y color personalizado
     */
    public static FontIcon createIcon(org.kordamp.ikonli.Ikon iconCode, int size) {
        return createIcon(iconCode, size, null);
    }
    
    /**
     * Crea un icono con tamaño predeterminado
     */
    public static FontIcon createIcon(org.kordamp.ikonli.Ikon iconCode) {
        return createIcon(iconCode, MEDIUM_SIZE, null);
    }
    
    /**
     * Actualiza el color de un icono existente
     */
    public static void setIconColor(FontIcon icon, String color) {
        icon.setIconColor(Color.web(color));
    }
    
    /**
     * Actualiza el tamaño de un icono existente
     */
    public static void setIconSize(FontIcon icon, int size) {
        icon.setIconSize(size);
    }
} 