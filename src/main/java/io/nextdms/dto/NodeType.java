package io.nextdms.dto;

public enum NodeType {
    FOLDER("Folder"),
    APPLICATION("Application"), // special type of folder
    DOCUMENT("Document"),
    PHOTO_IMAGE("Photo/Image"),
    VIDEO("Video"),
    AUDIO("Audio"),
    PDF("PDF"),
    SPREADSHEET("Spreadsheet"),
    PRESENTATION("Presentation"),
    TEXT("Text"),
    ARCHIVE("Archive"),
    OTHER("Other");

    private final String displayName;

    NodeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
