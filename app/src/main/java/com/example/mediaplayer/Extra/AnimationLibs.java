package com.example.mediaplayer.Extra;

import android.view.View;

public class AnimationLibs {
    public void slideInRight(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.post(() -> {
            view.setAlpha(1f);
            view.setTranslationX(view.getWidth());
            view.animate()
                    .withStartAction(onStart)
                    .translationX(0f)
                    .setDuration(duration)
                    .withEndAction(onEnd)
                    .start();
        });
    }

    public void slideOutRight(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.animate()
                .withStartAction(onStart)
                .translationX(view.getWidth())
                .setDuration(duration)
                .withEndAction(onEnd)
                .start();
    }

    public void slideInBottom(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.post(() -> {
            view.setAlpha(1f);
            view.setTranslationY(view.getHeight());
            view.animate()
                    .withStartAction(onStart)
                    .translationY(0f)
                    .setDuration(duration)
                    .withEndAction(onEnd)
                    .start();
        });
    }

    public void slideOutBottom(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.animate()
                .withStartAction(onStart)
                .translationY(view.getHeight())
                .setDuration(duration)
                .withEndAction(onEnd)
                .start();
    }

    public void slideInTop(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.post(() -> {
            view.setAlpha(1f);
            view.setTranslationY(-view.getHeight());
            view.animate()
                    .withStartAction(onStart)
                    .translationY(0f)
                    .setDuration(duration)
                    .withEndAction(onEnd)
                    .start();
        });
    }

    public void slideOutTop(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.animate()
                .withStartAction(onStart)
                .translationY(-view.getHeight())
                .setDuration(duration)
                .withEndAction(onEnd)
                .start();
    }

    public void fadeIn(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.post(() -> {
            view.animate()
                    .withStartAction(onStart)
                    .alpha(1f)
                    .setDuration(duration)
                    .withEndAction(onEnd)
                    .start();
        });
    }

    public void fadeOut(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.animate()
                .withStartAction(onStart)
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(onEnd)
                .start();
    }

    public void fadeSlideInRight(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.post(() -> {
            view.setTranslationX(view.getWidth());
            view.animate()
                    .withStartAction(onStart)
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(duration)
                    .withEndAction(onEnd)
                    .start();
        });
    }

    public void fadeSlideOutRight(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.animate()
                .withStartAction(onStart)
                .alpha(0f)
                .translationX(view.getWidth())
                .setDuration(duration)
                .withEndAction(onEnd)
                .start();
    }

    public void fadeSlideInBottom(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        view.post(() -> {
            view.setTranslationY(view.getHeight());
            view.animate()
                    .withStartAction(onStart)
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(duration)
                    .withEndAction(onEnd)
                    .start();
        });
    }

    public void fadeSlideOutBottom(View view, long duration, Runnable onStart, Runnable onEnd) {
        view.animate()
                .withStartAction(onStart)
                .alpha(0f)
                .translationY(view.getHeight())
                .setDuration(duration)
                .withEndAction(onEnd)
                .start();
    }

}
