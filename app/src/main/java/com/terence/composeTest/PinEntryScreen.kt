package com.terence.composeTest

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusOrder
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.text.isDigitsOnly
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.terence.composeTest.ui.Grey10
import com.terence.composeTest.ui.Grey20
import com.terence.composeTest.ui.Grey40
import com.terence.composeTest.ui.Red30
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Terence Peh on 18/11/21.
 */

sealed class SizeConstraint(open val min: TextUnit = TextUnit.Unspecified) {
    data class Width(override val min: TextUnit = TextUnit.Unspecified) : SizeConstraint(min)
    data class Height(override val min: TextUnit = TextUnit.Unspecified) : SizeConstraint(min)
}

@ExperimentalAnimationApi
@OptIn(ExperimentalUnitApi::class)
@ExperimentalComposeUiApi
@Composable
fun PinEntryScreen(
    modifier: Modifier = Modifier,
    headerText: String,
    errorText: String? = null,
    descText: String? = null,
    resetText: String? = null,
    requestExpiresText: String? = null,
    @DrawableRes bannerIconResId: Int = R.drawable.ic_c_warning,
    showPinKeyPad: Boolean = false,
    cdiMode: Boolean = false,
    topInsetPadding: Dp = dimensionResource(R.dimen.spacing00),
    bottomInsetPadding: Dp = dimensionResource(R.dimen.spacing00),
    backgroundColor: Color = Color.Transparent,
    headerStyle: TextStyle = MaterialTheme.typography.h2,
    constraint: SizeConstraint = SizeConstraint.Height(),
    onPinEntryEditTextClick: () -> Unit = {},
    onNonDigitPinClick: (String) -> Unit = {},
    onResetAppClick: () -> Unit = {},
    secondaryButton: @Composable () -> Unit = @Composable { },
    onAllPinFilledUp: (String) -> Unit = {}
) {
    val passcodeLength = 6

    val digits = remember {
        mutableStateListOf(
            *((0 until passcodeLength).map { "" }.toTypedArray())
        )
    }

    val visualTransformations = remember {
//        mutableStateListOf(
//            *((0 until passcodeLength).map { VisualTransformation.None }.toTypedArray())
//        )
        mutableStateOf(VisualTransformation.None)
    }

    var keyIndex by remember { mutableStateOf(0) }

    // Creates a CoroutineScope bound to the PinEntryScreen's lifecycle
    val scope = rememberCoroutineScope()

//    val interactionSource = remember { MutableInteractionSource() }

    val isLightMode = MaterialTheme.colors.isLight

    // val extraThemeColors = ExtraThemeColors.getExtraThemeColors(!isLightMode, cdiMode)

    val systemUiController = rememberSystemUiController()

    var textStyle by remember { mutableStateOf(headerStyle) }
    var readyToDraw by remember { mutableStateOf(false) }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = isLightMode
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        color = backgroundColor
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(top = topInsetPadding))
        ) {

            val (textColumnRef, pinPadRef, secondaryButtonRef) = createRefs()

            val pinEntryGuideline = createGuidelineFromTop(0.55f)

//            if (errorText != null) {
//                // clear the inout whenever error is shown
//                keyIndex = 0
//                digits.fill("")
//            }

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .padding(
                        start = dimensionResource(R.dimen.spacing07),
                        end = dimensionResource(R.dimen.spacing07),
                        bottom = dimensionResource(R.dimen.spacing03)
                    )
                    .constrainAs(textColumnRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(secondaryButtonRef.top)
                        height = Dimension.fillToConstraints
                    }
            ) {

                Spacer(
                    modifier = Modifier
                        .height(dimensionResource(R.dimen.spacing08))
                )

                Text(
                    text = headerText,
//            color = textColor,
                    style = textStyle,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                        .drawWithContent {
                            if (readyToDraw)
                                drawContent()
                        },
                    onTextLayout = { result ->
                        fun constrain() {
                            val reSized = textStyle.fontSize * 0.9f
                            textStyle = if (constraint.min != TextUnit.Unspecified && reSized <= constraint.min) {
                                textStyle.copy(fontSize = constraint.min)
                            } else {
                                textStyle.copy(fontSize = reSized)
                            }
                        }

                        when (constraint) {
                            is SizeConstraint.Height -> {
                                if (result.didOverflowHeight) {
                                    constrain()
                                } else {
                                    readyToDraw = true
                                }
                            }
                            is SizeConstraint.Width -> {
                                if (result.didOverflowWidth) {
                                    constrain()
                                } else {
                                    readyToDraw = true
                                }
                            }
                        }
                    }
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimensionResource(R.dimen.spacing08))
                )

//            val contentDescription = stringResource(R.string.content_desc_6_digit_tf)
                Box(
                    modifier = Modifier
                ) {
                    PinEntryTextField(
                        modifier = Modifier,
                        digits = digits,
                        isError = errorText != null,
                        isPassCode = true,
                        visualTransformations = visualTransformations,
                        onPinEntryEditTextClick = onPinEntryEditTextClick
                    )

//                Box(
//                    modifier = Modifier
//                        .matchParentSize()
//                        .alpha(0f)
//                        .semantics {
//                            // Set any explicit semantic properties
//                            customActions = listOf(
//                                CustomAccessibilityAction(contentDescription) { true },
//                            )
//                        }
//                        .clickable
//                        {
//                            onPinEntryEditTextClick.invoke()
//                        },
//                )
                }

//        Spacer(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(8.dp)
//                .constrainAs(bottomPeTextFieldRef) {
//                    top.linkTo(pinEntryTextFieldRef.bottom)
//                }
//        )

                if (resetText != null) {
                    AnimatedVisibility(
                        visible = errorText != null || requestExpiresText != null,
                        modifier = Modifier
                            .wrapContentSize()
                    ) {
//                        Column {
//                            val combinedText = if (showLastTryText) {
//                                if (requestExpiresText != null)
//                                    requestExpiresText + "\n" + lastTryBannerText
//                                else
//                                    lastTryBannerText
//                            } else requestExpiresText
                        Column {
                            errorText?.let {
                                Text(
                                    text = it,
                                    // style = getExtraTypography().labelLarge,
                                    color = MaterialTheme.colors.error,
                                    maxLines = 2,
                                    modifier = Modifier
                                        .padding(top = dimensionResource(R.dimen.spacing03))
                                        .fillMaxWidth()
                                )
                            }

                            requestExpiresText?.let {
                                CountDownBanner(
                                    modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing05)),
                                    bannerText = it,
                                    iconResId = bannerIconResId
                                )
                            }
                        }
//                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .height(dimensionResource(R.dimen.spacing09))
                    )

                    descText?.let {
                        Text(
                            text = descText,
                            style = MaterialTheme.typography.subtitle1,
                            color = Grey40,
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                    // val resetButtonContentDescription = stringResource(R.string.content_desc_cannot_remember_reset_passcode)

                    // TertiaryButton(
                    //     buttonText = resetText,
                    //     modifier = Modifier
                    //         .wrapContentSize()
                    //         .semantics {
                    //             this.contentDescription = resetButtonContentDescription
                    //         },
                    //     enabled = true,
                    //     onClick = onResetAppClick,
                    // )

//                    Text(
//                        text = resetText,
//                        style = MaterialTheme.typography.button.copy(
//                            fontWeight = FontWeight.SemiBold,
//                        ),
//                        maxLines = 1,
//                        color = MaterialTheme.colors.error,
//                        modifier = Modifier
//                            .wrapContentSize()
//                            .padding(top = dimensionResource(R.dimen.spacing03))
//                            .clickable(
//                                interactionSource = interactionSource,
//                                indication = rememberRipple(bounded = true, color = )
//                            ) {
//                                onResetAppClick.invoke()
//                            }
//                            .semantics {
//                                this.contentDescription = resetButtonContentDescription
//                            }
//                    )
                } else {

                    Spacer(
                        modifier = Modifier
                            .height(dimensionResource(R.dimen.spacing03))
                    )

                    AnimatedVisibility(
                        visible = errorText != null,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column {
                            if (errorText != null) {
                                Text(
                                    text = errorText,
                                    // style = getExtraTypography().labelLarge,
                                    maxLines = 3,
                                    color = MaterialTheme.colors.error,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )

                                Spacer(
                                    modifier = Modifier
                                        .height(dimensionResource(R.dimen.spacing05))
                                )
                            }
                        }
                    }

                    descText?.let {
                        Text(
                            text = descText,
                            color = Grey40,
                            // style = getExtraTypography().labelLarge,
                            maxLines = 3,
//                    color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = bottomInsetPadding)
                    .constrainAs(secondaryButtonRef) {
                        bottom.linkTo(parent.bottom)
                    }
            ) {
                secondaryButton()
            }

            AnimatedVisibility(
                visible = showPinKeyPad,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight ->
                        fullHeight
                    },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = 100,
                        easing = LinearOutSlowInEasing
                    )
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight ->
                        fullHeight
                    },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = 100,
                        easing = LinearOutSlowInEasing
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomInsetPadding)
                    .constrainAs(pinPadRef) {
                        top.linkTo(pinEntryGuideline)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                    }
            ) {
                PinPad(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxSize()
                ) { key ->

                    if (key.isDigitsOnly()) {
                        if (keyIndex in 0 until passcodeLength) {
                            scope.launch {
                                visualTransformations.value = VisualTransformation.None
                                delay(1000)
                                visualTransformations.value = PasswordVisualTransformation()
                            }

                            digits[keyIndex++] = key

                            if (keyIndex == digits.size) {
                                onAllPinFilledUp.invoke(digits.joinToString(separator = ""))
                                scope.launch {
                                    delay(300)
                                    digits.fill("")
                                    keyIndex = 0
                                }
                            }
                        }
                    } else {
                        when (key) {
                            PinPadKey.KEY_BACKSPACE.name -> {

                                if (keyIndex in 1..passcodeLength) {
                                    digits[--keyIndex] = ""
                                } else {
                                    digits[keyIndex] = ""
                                }
                            }
                        }
                        onNonDigitPinClick.invoke(key)
                    }
                }
            }
        }
    }
}

@ExperimentalUnitApi
@Composable
fun CountDownBanner(
    modifier: Modifier = Modifier,
    bannerText: String,
    @DrawableRes iconResId: Int,
    cdiMode: Boolean = false,
    backgroundColor: Color = Color.Gray,
) {

    Card(
        shape = RoundedCornerShape(dimensionResource(R.dimen.spacing03)),
        backgroundColor = backgroundColor,
        elevation = dimensionResource(R.dimen.spacing00),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(dimensionResource(R.dimen.spacing04))
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier
                    .layoutId("bannerIcon"),
            )

            Column(
                modifier = Modifier
                    .padding(start = dimensionResource(R.dimen.spacing03))
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = bannerText,
                    // style = getExtraTypography().labelBase,
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun PinEntryTextField(
    modifier: Modifier,
    cdiMode: Boolean = false,
//    textColor: Color = MaterialTheme.colors.onBackground,
    isError: Boolean = false,
    isPassCode: Boolean = false,
    digits: SnapshotStateList<String>,
    textFieldPadding: Float = 8F,
    visualTransformations: MutableState<VisualTransformation>,
    onPinEntryEditTextClick: () -> Unit = {},
) {

    val focusRequest = remember {
        (0 until digits.size).map { FocusRequester() }
    }

    val focusManager = LocalFocusManager.current

    var textFieldWidth by remember { mutableStateOf(0f) }

    BoxWithConstraints {

        textFieldWidth = maxWidth.value

        val boxWidth = ((textFieldWidth - textFieldPadding * (digits.size - 1)) / digits.size).dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
//                .onGloballyPositioned {
//                    textFieldWidth = it.size.toSize().width
//                }
        ) {
            val shape = RoundedCornerShape(dimensionResource(R.dimen.spacing03))

            for (i in 0 until digits.size) {

                CompositionLocalProvider(
                    // You can also provides null to completely disable the default input service.
                    LocalTextInputService provides null
                ) {
                    BasicTextField(
                        value = digits[i],
                        onValueChange = {
//                        if (it.isDigitsOnly()) {
//                            digits[i] = it.lastOrNull()?.toString() ?: ""
//                            if (digits[i].isBlank() && i > 0) {
//                                focusRequest[i + 1].requestFocus()
//                            } else if (i < digits.size - 1) {
//                                focusRequest[i + 1].requestFocus()
//                            }
//                        }
                        },
//                        readOnly = true,
                        modifier = Modifier
                            .padding(start = if (i == 0) dimensionResource(R.dimen.spacing00) else dimensionResource(R.dimen.spacing03))
                            .width(width = boxWidth)
                            .clip(shape = shape)
                            .background(color = if (isError) Red30 else Grey20)
                            .border(
                                width = 1.dp,
                                color = if (isError) MaterialTheme.colors.error else Grey20.copy(alpha = 0f),
                                shape = shape
                            )
                            .focusRequester(focusRequest[i])
                            .onFocusChanged {
                                if (it.hasFocus) {
                                    onPinEntryEditTextClick.invoke()
//                                    focusRequest[i].freeFocus()
                                    focusManager.clearFocus()
                                }
                            }
                            .focusOrder(focusRequest[i]),
//                        .onKeyEvent { keyEvent ->
//                            if (keyEvent.type == KeyEventType.KeyUp &&
//                                keyEvent.key == Key.Backspace &&
//                                digits[i].isEmpty() &&
//                                i > 0
//                            ) {
//                                focusRequest[i - 1].requestFocus()
//                                digits[i - 1] = ""
//                                true
//                            } else {
//                                false
//                            }
//                        },
//                    keyboardOptions = KeyboardOptions(
//                        autoCorrect = false,
//                        imeAction = ImeAction.Done,
//                        keyboardType = KeyboardType.Number
//                    ),
                        textStyle = MaterialTheme.typography.h1.copy(
                            color = MaterialTheme.colors.onSurface,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.sp,
                            lineHeight = 0.sp,
                            textIndent = TextIndent.None,
                        ),
                        singleLine = true,
//                    fontSize = 24.sp,
                        visualTransformation = if (isPassCode && i < digits.size - 1 && digits[i + 1] != "") {
                            PasswordVisualTransformation()
                        } else {
                            visualTransformations.value
                        },
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = dimensionResource(R.dimen.spacing11))
                                    .padding(top = dimensionResource(R.dimen.spacing04))
                                    .fillMaxWidth(),
                                propagateMinConstraints = true,
                                contentAlignment = Alignment.Center,
                            ) {
                                innerTextField()
                            }
                        }
//                    colors = TextFieldDefaults.textFieldColors(
//                        textColor = textColor,
//                        disabledTextColor = Color.Transparent,
//                        backgroundColor = if (isError) extraThemeColors.errorTextFieldBackground else extraThemeColors.secondaryButton,
//                        focusedIndicatorColor = Color.Transparent,
//                        unfocusedIndicatorColor = Color.Transparent,
//                        disabledIndicatorColor = Color.Transparent,
//                    ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun PinPad(
    modifier: Modifier,
    cdiMode: Boolean = false,
    keyBackgroundColor: Color = Grey10,
//    keyTextColor: Color = MaterialTheme.colors.onBackground,
    onKeyPress: (String) -> Unit = { }
) {

    // val extraThemeColors = ExtraThemeColors.getExtraThemeColors(!MaterialTheme.colors.isLight, cdiMode)

    val keysMatrix = arrayOf(
        arrayOf(R.string.txt_pin_cell_1, R.string.txt_pin_cell_2, R.string.txt_pin_cell_3,),
        arrayOf(R.string.txt_pin_cell_4, R.string.txt_pin_cell_5, R.string.txt_pin_cell_6),
        arrayOf(R.string.txt_pin_cell_7, R.string.txt_pin_cell_8, R.string.txt_pin_cell_9),
    )

    var keyHeight by remember { mutableStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {

        keyHeight = maxHeight.value / 4

        Column(
            modifier = Modifier
                .shadow(1.dp)
                .fillMaxSize()
//                .onGloballyPositioned {
//                    keyHeight = it.size.toSize().height / 9
//                }
        ) {

            keysMatrix.forEach { row ->
//            FixedHeightBox(modifier = Modifier.wrapContentWidth(), height = 24.dp ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    row.forEach { key ->
                        KeyboardKey(
                            key = key,
                            modifier = Modifier.weight(1f),
                            cdiMode = cdiMode,
                            backgroundColor = keyBackgroundColor,
//                            keyTextColor = keyTextColor,
                            keyHeight = keyHeight,
                            onKeyPress = onKeyPress,
                        )
                    }
                }
//            }
            }

//        FixedHeightBox(modifier = Modifier.wrapContentWidth(), height = 64.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KeyboardKey(
                    iconResId = PinPadKey.KEY_HIDE_KEYBOARD.iconResId,
                    modifier = Modifier.weight(1f),
                    cdiMode = cdiMode,
                    backgroundColor = keyBackgroundColor,
//                    keyTextColor = keyTextColor,
                    keyHeight = keyHeight,
                    onKeyPress = onKeyPress,
                )

                KeyboardKey(
                    key = R.string.txt_pin_cell_0,
                    modifier = Modifier.weight(1f),
                    cdiMode = cdiMode,
                    backgroundColor = keyBackgroundColor,
//                    keyTextColor = keyTextColor,
                    keyHeight = keyHeight,
                    onKeyPress = onKeyPress,
                )

                KeyboardKey(
                    iconResId = PinPadKey.KEY_BACKSPACE.iconResId,
                    modifier = Modifier.weight(1f),
                    cdiMode = cdiMode,
                    backgroundColor = keyBackgroundColor,
//                    keyTextColor = keyTextColor,
                    keyHeight = keyHeight,
                    onKeyPress = onKeyPress,
                )
            }
        }
    }
}

@ExperimentalUnitApi
@Composable
fun KeyboardKey(
    @StringRes key: Int? = null,
    @DrawableRes iconResId: Int? = null,
    modifier: Modifier,
    cdiMode: Boolean = false,
    backgroundColor: Color,
//    keyTextColor: Color,
    keyHeight: Float,
    onKeyPress: (String) -> Unit,
) {

    check(key != null || iconResId != null) { "Both keyboardKey and resId cannot be null" }

    // val extraThemeColors = ExtraThemeColors.getExtraThemeColors(!MaterialTheme.colors.isLight, cdiMode)

    val interactionSource = remember { MutableInteractionSource() }
//    val pressed = interactionSource.collectIsPressedAsState()
//    val textLineHeight = getTextFontHeight()

    Box(
        modifier = modifier
            .background(backgroundColor)
            .height(keyHeight.dp)
            .border(1.dp, Grey20),
        contentAlignment = Alignment.Center,
    ) {
//        val shape = RoundedCornerShape(5.dp)

        if (key != null) {
            val text = stringResource(id = key)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(bounded = true)
                    ) {
                        onKeyPress(text)
                    },
//                    .padding(
//                        start = 12.dp,
//                        end = 12.dp,
//                        top = 16.dp,
//                        bottom = 16.dp
//                    ),
                contentAlignment = Alignment.Center,
            ) {
                val pxMedium = 75.dp.value
                val pxSmall = 58.dp.value
                val pxExtraSmall = 48.dp.value

                Text(
                    text = text,
//                color = keyTextColor,
                    modifier = Modifier
                        .layoutId(key)
                        .fillMaxWidth()
//                    .background(sg.ndi.core.ui.theme.Color.Grey40)
//                    .padding(2.dp)
                        .padding(
                            start = dimensionResource(R.dimen.spacing04),
                            end = dimensionResource(R.dimen.spacing04),
                            top = dimensionResource(R.dimen.spacing05),
                            bottom = dimensionResource(R.dimen.spacing05),
                        ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body1.copy(
                        fontSize = if (keyHeight < pxMedium && keyHeight > pxSmall)
                            20.sp
                        else if (keyHeight <= pxSmall && keyHeight > pxExtraSmall)
                            16.sp
                        else if (keyHeight <= pxExtraSmall)
                            14.sp
                        else
                            24.sp
                    ),
                )
            }

//            if (pressed.value) {
//                Text(
//                    text,
//                    Modifier
//                        .fillMaxWidth()
//                        .height(keyHeight.dp)
//                        .border(1.dp, extraThemeColors.divider)
//                        .padding(
//                            start = 16.dp,
//                            end = 16.dp,
//                            top = 16.dp,
//                            bottom = 48.dp
//                        ),
//                    textAlign = TextAlign.Center,
//                    fontSize = 25.sp,
//                )
//            }
        }

        if (iconResId != null) {
            val contentDescription = when (iconResId) {
                PinPadKey.KEY_BACKSPACE.iconResId -> stringResource(R.string.content_desc_backspace)
                PinPadKey.KEY_HIDE_KEYBOARD.iconResId -> stringResource(R.string.content_desc_keyboard_arrow_down)
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyHeight.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(bounded = true)
                    ) {
                        val nonDigitKey = PinPadKey.fromValue(iconResId)
                        onKeyPress(nonDigitKey)
                    },
//                    .padding(
//                        start = 12.dp,
//                        end = 12.dp,
//                        top = 16.dp,
//                        bottom = 16.dp
//                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = contentDescription,
//                    tint = keyTextColor,
                    modifier = Modifier
                        .layoutId(iconResId)
                        .fillMaxWidth()
//                    .background(sg.ndi.core.ui.theme.Color.Grey40)
//                    .padding(2.dp)
//                        .height(keyHeight.dp)
//                    .background(MaterialTheme.colors.background)
//                        .border(1.dp, extraThemeColors.divider)
//                        .size(72.dp)
//                        .clickable(interactionSource = interactionSource, indication = rememberRipple(bounded = true)) {
//                            onKeyPress(null)
//                        }
                        .padding(
                            start = dimensionResource(R.dimen.spacing04),
                            end = dimensionResource(R.dimen.spacing04),
                            top = dimensionResource(R.dimen.spacing05),
                            bottom = dimensionResource(R.dimen.spacing05),
                        ),
                )
            }

//            if (pressed.value) {
//                Icon(
//                    painter = painterResource(id = iconResId),
//                    contentDescription = null,
//                    tint = MaterialTheme.colors.onPrimary,
//                    modifier = Modifier
//                        .layoutId("icon")
//                        .fillMaxWidth()
//                        .height(keyHeight.dp)
//                        .border(1.dp, extraThemeColors.divider)
//                        .padding(
//                            start = 16.dp,
//                            end = 16.dp,
//                            top = 16.dp,
//                            bottom = 48.dp
//                        ),
//                )
//            }
        }
    }
}

@Composable
fun FixedHeightBox(modifier: Modifier, height: Dp, content: @Composable () -> Unit) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }
        layout(constraints.maxWidth, height.roundToPx()) {
            placeables.forEach { placeable ->
                placeable.place(x = 0, y = 0)
            }
        }
    }
}

@Composable
private fun getTextFontHeight(): Dp {
    return with(LocalDensity.current) {

        val lineHeight = LocalTextStyle.current.lineHeight

        with(lineHeight) {
            if (isSp) {
                toDp()
            } else {
                20.dp
            }
        }
    }
}

@Composable
@Preview
fun KeyboardPreview() {
    Column(modifier = Modifier.background(Color.White)) {
        PinPad(Modifier) { }
    }
}

enum class PinPadKey(@DrawableRes val iconResId: Int) {
    KEY_BACKSPACE(R.drawable.ic_backspace),
    KEY_HIDE_KEYBOARD(R.drawable.ic_keyboard_arrow_down);

    companion object {
        private val mapping = values().associateBy(PinPadKey::iconResId)
        fun fromValue(value: Int) = mapping[value]?.name ?: error("Look up failed for ${this::class.java.declaringClass}")
    }
}
