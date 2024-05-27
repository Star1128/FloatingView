package com.ethan.floatingview;

public interface MagnetViewListener {

    void onRemove(FloatingMagnetView magnetView);

    void onClick(FloatingMagnetView magnetView);

    void onDragStart(FloatingMagnetView magnetView);

    void onDragEnd(FloatingMagnetView magnetView);

    void onMoveToEdge(FloatingMagnetView magnetView);
}
