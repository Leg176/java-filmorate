package ru.yandex.practicum.filmorate.model;

public enum MotionPictureAssociation {
    G,
    PG,
    PG_13,
    R,
    NC_17;

    @Override
    public String toString() {
        return switch (this) {
            case PG_13 -> "PG-13";
            case NC_17 -> "NC-17";
            default -> name();
        };
    }
}
