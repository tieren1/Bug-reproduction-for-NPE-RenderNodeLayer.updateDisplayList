package com.terence.composeTest

import android.graphics.drawable.Animatable
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat

/**
 * @param modifier Modifier object to configure the screen
 * @param drawableResId This must be a gif drawable
 * @param drawableSize The size of the loading animated drawable, should be a avd default 160.dp
 * @param loadingText Text below the image
 */
@ExperimentalAnimationGraphicsApi
@Composable
fun IndeterminateLoadingScreen(
    modifier: Modifier = Modifier,
    @DrawableRes drawableResId: Int? = null,
    @DrawableRes gifDrawableResId: Int? = null,
    manualLoopAnim: Boolean = false,
    drawableSize: Dp = Dp.Unspecified,
    loadingText: String? = null,
    loadingTextColor: Color = MaterialTheme.colors.primary,
    loadingSubText: String? = null,
    loadingSubTextColor: Color = MaterialTheme.colors.onBackground
) {

    check(gifDrawableResId != null || drawableResId != null) {
        "Both drawableResId and animatedVectorDrawableResId cannot be null!"
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .then(modifier)
    ) {

        ConstraintLayout(
            modifier = Modifier
                .padding(
                    start = dimensionResource(id = R.dimen.spacing07),
                    end = dimensionResource(id = R.dimen.spacing07),
                    top = dimensionResource(id = R.dimen.spacing07)
                )
        ) {

            val (drawableRef, spacerRef, textRef, subTextSpacerRef, subTextRef) = createRefs()

            createVerticalChain(
                drawableRef,
                spacerRef,
                textRef,
                subTextSpacerRef,
                subTextRef,
                chainStyle = ChainStyle.Packed
            )

            val imageModifier = Modifier
                .size(drawableSize)
                .constrainAs(drawableRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(spacerRef.top)
                    if (drawableSize == Dp.Unspecified) {
                        width = Dimension.fillToConstraints
                        height = Dimension.preferredWrapContent
                    }
                }

            if (gifDrawableResId != null) {
                // GifDrawableRepeating(
                //     modifier = imageModifier,
                //     id = gifDrawableResId
                // )
            } else {
                AnimatedVectorDrawableRepeating(
                    modifier = imageModifier,
                    manualLoopAnim = manualLoopAnim,
                    id = drawableResId ?: R.drawable.avd_placeholder_spinner
                )
            }

            Spacer(
                modifier = Modifier
                    .height(dimensionResource(id = R.dimen.spacing08))
                    .constrainAs(spacerRef) {
                        top.linkTo(drawableRef.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(textRef.top)
                        width = Dimension.fillToConstraints
                    }
            )

            if (!loadingText.isNullOrBlank()) {
                Text(
                    text = loadingText.uppercase(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.overline,
                    color = loadingTextColor,
                    modifier = Modifier.constrainAs(textRef) {
                        top.linkTo(spacerRef.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(subTextSpacerRef.top)
                        width = Dimension.fillToConstraints
                    }
                )
            }

            if (!loadingSubText.isNullOrBlank()) {
                Spacer(
                    modifier = Modifier
                        .height(dimensionResource(id = R.dimen.spacing05))
                        .constrainAs(subTextSpacerRef) {
                            top.linkTo(textRef.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(subTextRef.top)
                            width = Dimension.fillToConstraints
                        }
                )

                Text(
                    text = loadingSubText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.overline,
                    color = loadingSubTextColor,
                    modifier = Modifier.constrainAs(subTextRef) {
                        top.linkTo(subTextSpacerRef.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                        height = Dimension.preferredWrapContent
                    }
                )
            }
        }
    }
}

@ExperimentalAnimationGraphicsApi
@Composable
fun AnimatedVectorDrawableRepeating(
    modifier: Modifier = Modifier,
    manualLoopAnim: Boolean = false, // used for when avd does not internally do repeating="infinite"
    @DrawableRes id: Int
) {
    AndroidView(
        modifier = modifier,
        factory = {
            ImageView(it).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageResource(id)
                autoStartAnimation(autoStart = true, loopAnim = manualLoopAnim)
            }
        },
        update = {}
    )
}

fun ImageView.autoStartAnimation(autoStart: Boolean = false, loopAnim: Boolean = false) {
    // Fix for java.lang.NullPointerException at androidx.compose.ui.platform.RenderNodeLayer.updateDisplayList(RenderNodeLayer.android.kt:245) from
    // IndeterminateLoadingScreen with avd drawable happening on Huawei devices mostly
    // Commented out post block for crash reproduction
    // post {
    if (drawable is Animatable) {
        val animatable = drawable as Animatable

        if (autoStart) {
            if (!animatable.isRunning) {
                if (loopAnim) {
                    doOnAttach {
                        when (animatable) {
                            is AnimatedVectorDrawableCompat -> {
                                val animationListener =
                                    object : Animatable2Compat.AnimationCallback() {
                                        override fun onAnimationEnd(drawable: Drawable?) {
                                            post { animatable.start() }
                                        }
                                    }.apply {
                                        animatable.registerAnimationCallback(this)
                                    }

                                doOnDetach {
                                    animatable.unregisterAnimationCallback(animationListener)
                                }
                            }
                            is AnimatedVectorDrawable -> {
                                val animationListener =
                                    object : Animatable2.AnimationCallback() {
                                        override fun onAnimationEnd(drawable: Drawable?) {
                                            post { animatable.start() }
                                        }
                                    }.apply {
                                        animatable.registerAnimationCallback(this)
                                    }

                                doOnDetach {
                                    animatable.unregisterAnimationCallback(animationListener)
                                }
                            }
                        }
                    }
                }
                animatable.start()
            }
        } else {
            animatable.stop()
        }
    }
    // }
}
