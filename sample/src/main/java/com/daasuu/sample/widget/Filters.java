package com.daasuu.sample.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.daasuu.camerarecorder.egl.filter.GlBilateralFilter;
import com.daasuu.camerarecorder.egl.filter.GlBoxBlurFilter;
import com.daasuu.camerarecorder.egl.filter.GlBulgeDistortionFilter;
import com.daasuu.camerarecorder.egl.filter.GlCGAColorspaceFilter;
import com.daasuu.camerarecorder.egl.filter.GlFilter;
import com.daasuu.camerarecorder.egl.filter.GlFilterGroup;
import com.daasuu.camerarecorder.egl.filter.GlGaussianBlurFilter;
import com.daasuu.camerarecorder.egl.filter.GlGrayScaleFilter;
import com.daasuu.camerarecorder.egl.filter.GlInvertFilter;
import com.daasuu.camerarecorder.egl.filter.GlLookUpTableFilter;
import com.daasuu.camerarecorder.egl.filter.GlMonochromeFilter;
import com.daasuu.camerarecorder.egl.filter.GlSepiaFilter;
import com.daasuu.camerarecorder.egl.filter.GlSharpenFilter;
import com.daasuu.camerarecorder.egl.filter.GlSphereRefractionFilter;
import com.daasuu.camerarecorder.egl.filter.GlToneCurveFilter;
import com.daasuu.camerarecorder.egl.filter.GlToneFilter;
import com.daasuu.camerarecorder.egl.filter.GlVignetteFilter;
import com.daasuu.camerarecorder.egl.filter.GlWeakPixelInclusionFilter;
import com.daasuu.sample.R;

import java.io.IOException;
import java.io.InputStream;

import pl.droidsonroids.gif.GifOptions;
import pl.droidsonroids.gif.GifTexImage2D;
import pl.droidsonroids.gif.InputSource;

public enum Filters {
    NORMAL,
    OVERLAY_1,
    OVERLAY_2,
    OVERLAY_SACHIN,
    OVERLAY_DYNAMIC,
    BILATERAL,
    BOX_BLUR,
    BULGE_DISTORTION,
    CGA_COLOR_SPACE,
    GAUSSIAN_BLUR,
    GLAY_SCALE,
    INVERT,
    LOOKUP_TABLE,
    MONOCHROME,
    OVERLAY,
    SEPIA,
    SHARPEN,
    SPHERE_REFRACTION,
    TONE_CURVE,
    TONE,
    VIGNETTE,
    WEAKPIXELINCLUSION,
    FILTER_GROUP;

    public static GlFilter getFilterInstance(Filters filter, Context context) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap;
        options.inScaled = false;
        switch (filter) {
            case BILATERAL:
                return new GlBilateralFilter();
            case BOX_BLUR:
                return new GlBoxBlurFilter();
            case BULGE_DISTORTION:
                return new GlBulgeDistortionFilter();
            case CGA_COLOR_SPACE:
                return new GlCGAColorspaceFilter();
            case GAUSSIAN_BLUR:
                return new GlGaussianBlurFilter();
            case GLAY_SCALE:
                return new GlGrayScaleFilter();
            case INVERT:
                return new GlInvertFilter();
            case LOOKUP_TABLE:
                return new GlLookUpTableFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.lookup_sample));
            case MONOCHROME:
                return new GlMonochromeFilter();
            case OVERLAY:
                return new GlBitmapOverlaySample(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));
            case OVERLAY_1:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sticker, options);
                return new GlRajatOverlayFilter(bitmap);
            case OVERLAY_2:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.horror, options);
                return new GlRajatOverlayFilter(bitmap);
            case OVERLAY_SACHIN:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sachin, options);
                return new GlRajatOverlayFilter(bitmap);
            case OVERLAY_DYNAMIC:
//                GifTexImage2D gifTexImage2D;
//                try {
//                    GifOptions gifOptions = new GifOptions();
//                    gifOptions.setInIsOpaque(false);
//                    gifTexImage2D = new GifTexImage2D(new InputSource.ResourcesSource(
//                            context.getResources(), R.drawable.bear), gifOptions);
//                } catch (IOException e) {
//                    throw new IllegalStateException(e);
//                }
//                gifTexImage2D.startDecoderThread();
//                return new GlGifOverlayFilter(gifTexImage2D);

                int[] gif = {R.drawable.gif_1, R.drawable.gif_2, R.drawable.gif_3};
                return new GlGifOverlayFilter(context, gif);
            case SEPIA:
                return new GlSepiaFilter();
            case SHARPEN:
                return new GlSharpenFilter();
            case SPHERE_REFRACTION:
                return new GlSphereRefractionFilter();
            case TONE_CURVE:
                try {
                    InputStream inputStream = context.getAssets().open("acv/tone_cuver_sample.acv");
                    return new GlToneCurveFilter(inputStream);
                } catch (Exception e) {
                    return new GlFilter();
                }
            case TONE:
                return new GlToneFilter();
            case VIGNETTE:
                return new GlVignetteFilter();
            case WEAKPIXELINCLUSION:
                return new GlWeakPixelInclusionFilter();
            case FILTER_GROUP:
                return new GlFilterGroup(new GlMonochromeFilter(), new GlVignetteFilter());
            default:
                return new GlFilter();
        }

    }

}
