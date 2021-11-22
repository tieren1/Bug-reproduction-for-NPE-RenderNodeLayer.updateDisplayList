package com.terence.composeTest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.terence.composeTest.databinding.FragmentTestPinBinding
import com.terence.composeTest.ui.BasicsCodelabTheme

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TestChangePinFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TestChangePinFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentTestPinBinding

    private val viewModel: ChangePinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @ExperimentalAnimationGraphicsApi
    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    @ExperimentalComposeUiApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestPinBinding.inflate(inflater, container, false)

        binding.cmpvRoot.run {
            // Dispose the Composition when viewLifecycleOwner is destroyed
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                ProvideWindowInsets(consumeWindowInsets = false) {
                    BasicsCodelabTheme {
                        val insets = LocalWindowInsets.current
                        val statusBarHeight = with(LocalDensity.current) { insets.statusBars.top.toDp() }
                        val navBarHeight = with(LocalDensity.current) { insets.navigationBars.bottom.toDp() }

                        val status =
                            viewModel.checkPinStatus.observeAsState(ChangePinViewModel.IDLE_STATE).value

                        var currentViewIndex by remember { mutableStateOf(ChangePinUiStates.CURRENT_PIN_VIEW) }
                        var showPinKeyPad by remember { mutableStateOf(false) }

                        var errorText by remember { mutableStateOf<String?>(null) }

                        var headerText = stringResource(R.string.txt_current_passcode)

                        when (status) {
                            ChangePinViewModel.IDLE_STATE -> {
                                handler.postDelayed(
                                    {
                                        errorText = null
                                    },
                                    200
                                )
                            }
                            ChangePinViewModel.PIN_VERIFYING -> {
                                currentViewIndex = ChangePinUiStates.VERIFY_LOADING_VIEW
                            }
                            ChangePinViewModel.PIN_MATCH -> {
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
                            toolbarHeight = statusBarHeight,
                            navBarHeight = navBarHeight,
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

                                            val isPinPatternValid =
                                                Pair<String?, Boolean>(null, true)/*InitRegistrationUtil.isPinPatternValid(
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
                                                currentViewIndex =
                                                    ChangePinUiStates.CONFIRM_NEW_PIN_VIEW
                                            } else {
                                                showPinKeyPad = false
                                                errorText =
                                                    if (isPinPatternValid.first.isNullOrBlank())
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
                                                errorText =
                                                    "The re-entered passcode is not the same. Try again"/*getString(R.string.txt_pin_entry_error)*/
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
            }
        }
        // Inflate the layout for this fragment
        return binding.root
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
                    IndeterminateLoadingScreen(
                        drawableResId = R.drawable.avd_placeholder_spinner,
                        loadingText = stringResource(R.string.txt_spinner_verifying),
                        drawableSize = dimensionResource(id = R.dimen.act_pro_spinner_size)
                    )
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

    private enum class ChangePinUiStates {
        CURRENT_PIN_VIEW,
        NEW_PIN_VIEW,
        CONFIRM_NEW_PIN_VIEW,
        VERIFY_LOADING_VIEW,
        NOTIFICATION_DRAWER_VIEW,
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TestPinFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TestChangePinFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
