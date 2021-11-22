/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.terence.composeTest

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.terence.composeTest.ChangePinViewModel.Companion.IDLE_STATE
import com.terence.composeTest.ChangePinViewModel.Companion.PIN_MATCH
import com.terence.composeTest.ChangePinViewModel.Companion.PIN_VERIFYING
import com.terence.composeTest.ui.BasicsCodelabTheme

class MainActivity : AppCompatActivity() {

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val viewModel: ChangePinViewModel by viewModels()

    @ExperimentalAnimationGraphicsApi
    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent {
        //     BasicsCodelabTheme {
        //         MyApp(handler, viewModel)
        //     }
        // }

        // val binding: ActivityMainBinding =
        //     DataBindingUtil.setContentView(this, R.layout.activity_main)

        // binding.cmpvRoot.run {
        //     setContent {
        //         BasicsCodelabTheme {
        //             MyApp(handler, viewModel)
        //         }
        //     }
        // }

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<TestChangePinFragment>(R.id.fcvContainer)
            }
        }
    }
}

private enum class ChangePinUiStates {
    CURRENT_PIN_VIEW,
    NEW_PIN_VIEW,
    CONFIRM_NEW_PIN_VIEW,
    VERIFY_LOADING_VIEW,
    NOTIFICATION_DRAWER_VIEW,
}

@ExperimentalAnimationGraphicsApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
private fun MyApp(handler: Handler, viewModel: ChangePinViewModel) {
    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }

    if (shouldShowOnboarding) {
        OnboardingScreen(onContinueClicked = { shouldShowOnboarding = false })
    } else {
        // Greetings()
        val status = viewModel.checkPinStatus.observeAsState(IDLE_STATE).value

        var currentViewIndex by remember { mutableStateOf(ChangePinUiStates.CURRENT_PIN_VIEW) }
        var showPinKeyPad by remember { mutableStateOf(false) }

        var errorText by remember { mutableStateOf<String?>(null) }

        var headerText = stringResource(R.string.txt_current_passcode)

        when (status) {
            IDLE_STATE -> {
                handler.postDelayed(
                    {
                        errorText = null
                    },
                    200
                )
            }
            PIN_VERIFYING -> {
                currentViewIndex = ChangePinUiStates.VERIFY_LOADING_VIEW
            }
            PIN_MATCH -> {
                if (currentViewIndex == ChangePinUiStates.VERIFY_LOADING_VIEW) {
                    showPinKeyPad = true
                    currentViewIndex = ChangePinUiStates.NEW_PIN_VIEW
                }
            }
        }

        // start of PinEntryScreen
        ChangePinScreen(
            headerText = headerText,
            errorText = errorText,
            currentViewIndex = currentViewIndex,
            // toolbarHeight = statusBarHeight + toolbarHeightPadding,
            // navBarHeight = navBarHeight,
            showPinKeyPad = showPinKeyPad,
            viewModel = viewModel,
            notificationDrawerButtonOnClick = {
                // showAnimation?.targetState = false
                //
                // handler.postDelayed(
                //     {
                //         findNavController().popBackStack()
                //     },
                //     350
                // )
            },
            onPinEntryEditTextClick = {
                showPinKeyPad = true
                errorText = null
            },
            onNonDigitPinClick = {
                showPinKeyPad = false
            },
            onResetAppClick = {
                // findNavController().linkToNavGraph(
                //     NavGraphLinks.RESET_APP(
                //         navOptions = NavigationUtil.buildUponRightInLeftOutNavOptions {
                //             anim {
                //                 enter = R.anim.slide_in_right
                //                 exit = android.R.anim.fade_out
                //                 popExit = R.anim.slide_out_right
                //                 popEnter = android.R.anim.fade_in
                //             }
                //         },
                //         resetAppFragmentArgs = ResetAppFragmentArgs(ResetAppFragment.TYPE_FROM_FORGOT_PASSCODE)
                //     )
                // )
            },

        ) { digits ->
            showPinKeyPad = false

            when (currentViewIndex) {
                ChangePinUiStates.CURRENT_PIN_VIEW -> {
                    handler.postDelayed(
                        {
                            // Check pin after last digit
                            viewModel.checkPin(digits)
                        },
                        300
                    )
                }
                ChangePinUiStates.NEW_PIN_VIEW -> {
                    handler.postDelayed(
                        {
                            if (digits.isBlank() /*|| viewModel.currentPin.isBlank()*/) {
                                // errorText = getString(R.string.general_error_message)
                                return@postDelayed
                            }

                            val isPinPatternValid = Pair<String?, Boolean>(null, true)/*InitRegistrationUtil.isPinPatternValid(
                            digits,
                            requireContext(),
                            securePref,
                            null,
                            null,
                            viewModel.currentPin,
                            analytics
                        )*/

                            if (isPinPatternValid.second) {
                                // viewModel.newPin = digits
                                showPinKeyPad = true
//                                                        headerText = getString(R.string.txt_pin_entry_2)
                                currentViewIndex = ChangePinUiStates.CONFIRM_NEW_PIN_VIEW
                            } else {
                                showPinKeyPad = false
                                errorText = if (isPinPatternValid.first.isNullOrBlank())
                                // getString(R.string.txt_pin_not_secure_enough)
                                    "Passcode cannot be a common pattern. Use something else"
                                else
                                    isPinPatternValid.first
                            }
                        },
                        300
                    )
                }
                ChangePinUiStates.CONFIRM_NEW_PIN_VIEW -> {
                    handler.postDelayed(
                        {
                            // Check pin after last digit
                            val newPin = "12456"/*viewModel.newPin*/

                            val isValid = newPin.isNotBlank() &&
                                newPin == digits

                            if (isValid) {
                                // viewModel.changePin()
                            } else {
                                errorText = "The re-entered passcode is not the same. Try again"/*getString(R.string.txt_pin_entry_error)*/
                            }
                        },
                        300
                    )
                }
                ChangePinUiStates.NOTIFICATION_DRAWER_VIEW,
                ChangePinUiStates.VERIFY_LOADING_VIEW -> Unit
            }
        }
        // end of PinEntryScreen

        DisposableEffect(Unit) {
            // startAnimation = !startAnimation
            showPinKeyPad = !showPinKeyPad
            // showNotification = !showNotification

            onDispose { }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalAnimationGraphicsApi
@Composable
private fun ChangePinScreen(
    headerText: String,
    errorText: String?,
    currentViewIndex: ChangePinUiStates,
    navBarHeight: Dp = 0.dp,
    toolbarHeight: Dp = 0.dp,
    showPinKeyPad: Boolean,
    viewModel: ChangePinViewModel,
    notificationDrawerButtonOnClick: () -> Unit = {},
    onPinEntryEditTextClick: () -> Unit = {},
    onNonDigitPinClick: (String) -> Unit = {},
    onResetAppClick: () -> Unit = {},
    onAllPinFilledUp: (String) -> Unit = {}
) {

    var mutableHeaderText = headerText

    AnimatedContent(
        targetState = currentViewIndex,
        transitionSpec = {
            when (initialState) {
                ChangePinUiStates.CURRENT_PIN_VIEW -> {
                    when (targetState) {
                        ChangePinUiStates.CURRENT_PIN_VIEW,
                        ChangePinUiStates.NEW_PIN_VIEW -> slideInHorizontally { -it } with slideOutHorizontally { it }
                        else -> fadeIn() with fadeOut()
                    }
                }
                ChangePinUiStates.VERIFY_LOADING_VIEW -> {
                    when (targetState) {
                        ChangePinUiStates.NEW_PIN_VIEW,
                        ChangePinUiStates.NOTIFICATION_DRAWER_VIEW -> slideInHorizontally { it } with slideOutHorizontally { -it }
                        else -> fadeIn() with fadeOut()
                    }
                }
                ChangePinUiStates.NEW_PIN_VIEW -> {
                    when (targetState) {
                        ChangePinUiStates.CONFIRM_NEW_PIN_VIEW -> slideInHorizontally { it } with slideOutHorizontally { -it }
                        else -> fadeIn() with fadeOut()
                    }
                }
                ChangePinUiStates.CONFIRM_NEW_PIN_VIEW -> {
                    when (targetState) {
                        ChangePinUiStates.NEW_PIN_VIEW -> slideInHorizontally { -it } with slideOutHorizontally { it }
                        ChangePinUiStates.NOTIFICATION_DRAWER_VIEW -> slideInHorizontally { it } with slideOutHorizontally { -it }
                        else -> fadeIn() with fadeOut()
                    }
                }
                ChangePinUiStates.NOTIFICATION_DRAWER_VIEW -> fadeIn() with fadeOut()
            }
        }
    ) { targetScreen ->
        when (targetScreen) {
            ChangePinUiStates.VERIFY_LOADING_VIEW -> {
                // IndeterminateLoadingScreen(
                //     drawableResId = R.drawable.avd_spinner,
                //     loadingText = stringResource(R.string.txt_spinner_verifying),
                //     drawableSize = dimensionResource(id = R.dimen.act_pro_spinner_size)
                // )
            }
            ChangePinUiStates.NOTIFICATION_DRAWER_VIEW -> {
                // showAnimation = changePinEndStateScreen(
                //     headerText = stringResource(R.string.txt_change_passcode_success),
                //     buttonText = stringResource(R.string.button_done),
                //     buttonClick = notificationDrawerButtonOnClick,
                // )
            }
            else -> {
                var descText = ""
                var resetText: String? = null

                when (targetScreen) {
                    ChangePinUiStates.CURRENT_PIN_VIEW -> {
                        mutableHeaderText = stringResource(R.string.txt_current_passcode)
                        descText = stringResource(R.string.txt_cannot_remember_reset_passcode)
                        resetText = stringResource(R.string.txt_reset_this_app)
                    }
                    ChangePinUiStates.NEW_PIN_VIEW -> {
                        mutableHeaderText = stringResource(R.string.txt_pin_entry_1)
                        descText = stringResource(R.string.txt_avoid_pin_pattern)
                    }
                    ChangePinUiStates.CONFIRM_NEW_PIN_VIEW -> {
                        mutableHeaderText = stringResource(R.string.txt_pin_entry_2)
                    }
                    else -> Unit
                }

                PinEntryScreen(
                    headerText = mutableHeaderText,
                    errorText = errorText,
                    descText = descText,
                    resetText = resetText,
                    showPinKeyPad = showPinKeyPad,
                    topInsetPadding = toolbarHeight,
                    bottomInsetPadding = navBarHeight,
                    backgroundColor = MaterialTheme.colors.surface,
                    onNonDigitPinClick = { key ->
                        when (key) {
                            PinPadKey.KEY_BACKSPACE.name -> {
                                when (currentViewIndex) {
                                    ChangePinUiStates.CURRENT_PIN_VIEW -> {
                                        viewModel.clearStatus()
                                    }
                                    else -> Unit
                                }
                            }
                            PinPadKey.KEY_HIDE_KEYBOARD.name -> {
//                                        showPinKeyPad = false
                                onNonDigitPinClick.invoke(key)
                            }
                        }
                    },
                    onPinEntryEditTextClick =
                    {
//                                showPinKeyPad = true
                        onPinEntryEditTextClick.invoke()
//                                errorText = null

                        when (currentViewIndex) {
                            ChangePinUiStates.CURRENT_PIN_VIEW -> {
                                viewModel.clearStatus()
                            }
                            else -> Unit
                        }
                    },
                    onResetAppClick = onResetAppClick,
                    onAllPinFilledUp = onAllPinFilledUp,
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreen(onContinueClicked: () -> Unit) {
    Surface {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Basics Codelab!")
            Button(
                modifier = Modifier.padding(vertical = 24.dp),
                onClick = onContinueClicked
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun Greetings(names: List<String> = List(1000) { "$it" }) {
    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
            Greeting(name = name)
        }
    }
}

@Composable
private fun Greeting(name: String) {
    Card(
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        CardContent(name)
    }
}

@Composable
private fun CardContent(name: String) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(12.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            Text(text = "Hello, ")
            Text(
                text = name,
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
            if (expanded) {
                Text(
                    text = (
                        "Composem ipsum color sit lazy, " +
                            "padding theme elit, sed do bouncy. "
                        ).repeat(4),
                )
            }
        }
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = if (expanded) Filled.ExpandLess else Filled.ExpandMore,
                contentDescription = if (expanded) {
                    stringResource(R.string.show_less)
                } else {
                    stringResource(R.string.show_more)
                }

            )
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    uiMode = UI_MODE_NIGHT_YES,
    name = "DefaultPreviewDark"
)
@Preview(showBackground = true, widthDp = 320)
@Composable
fun DefaultPreview() {
    BasicsCodelabTheme {
        Greetings()
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    BasicsCodelabTheme {
        OnboardingScreen(onContinueClicked = {})
    }
}
