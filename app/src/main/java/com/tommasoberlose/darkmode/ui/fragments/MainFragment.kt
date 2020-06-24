package com.tommasoberlose.darkmode.ui.fragments

import android.Manifest
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.transition.MaterialSharedAxis
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tommasoberlose.darkmode.ui.activities.MainActivity
import com.tommasoberlose.darkmode.R
import com.tommasoberlose.darkmode.components.BottomSheetMenu
import com.tommasoberlose.darkmode.components.events.MainUiEvent
import com.tommasoberlose.darkmode.databinding.MainFragmentBinding
import com.tommasoberlose.darkmode.global.Constants
import com.tommasoberlose.darkmode.global.Preferences
import com.tommasoberlose.darkmode.helpers.DarkThemeHelper
import com.tommasoberlose.darkmode.helpers.DarkThemeHelper.SECURE_PERMISSION_ERROR
import com.tommasoberlose.darkmode.helpers.TimeHelper
import com.tommasoberlose.darkmode.services.LocationService
import com.tommasoberlose.darkmode.services.SunsetSunriseService
import com.tommasoberlose.darkmode.services.UpdatesIntentService
import com.tommasoberlose.darkmode.ui.viewmodels.MainViewModel
import com.tommasoberlose.darkmode.utils.*
import com.zerobranch.layout.SwipeLayout
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

typealias Callback = (newTime: String) -> Unit

class MainFragment : Fragment() {

  companion object {
    fun newInstance() = MainFragment()
  }

  private lateinit var viewModel: MainViewModel
  private lateinit var binding: MainFragmentBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View {
    viewModel = ViewModelProvider(activity as MainActivity).get(MainViewModel::class.java)
    binding = DataBindingUtil.inflate<MainFragmentBinding>(inflater, R.layout.main_fragment, container, false)

    subscribeUi(viewModel)

    binding.lifecycleOwner = this
    binding.viewModel = viewModel

    return binding.root
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    addListener()

    // Link
    action_view_tutorial.movementMethod = LinkMovementMethod.getInstance()

    // Check OnePlus device
    warning_oneplus_container.isVisible = !Build.MANUFACTURER.equals("oneplus", ignoreCase = true) && !Preferences.hideOnePlusWarning
    warning_oneplus_container.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {
      override fun onOpen(direction: Int, isContinuous: Boolean) {
        if (isContinuous) {
          warning_oneplus.visibility = View.INVISIBLE
          warning_oneplus_container.collapseHeight()
          Preferences.hideOnePlusWarning = true
        }
      }
      override fun onClose() {
      }
    })

    // Missing permission
    checkPermission()

    // Settings
    action_settings.setOnClickListener {
      Navigation.findNavController(it).navigate(R.id.action_mainFragment_to_settingsFragment)
    }
  }

  private fun subscribeUi(viewModel: MainViewModel) {
    viewModel.isTileAdded.observe(viewLifecycleOwner, Observer {
      quick_settings_tile.isVisible = !it
    })

    viewModel.automaticMode.observe(viewLifecycleOwner, Observer {
      when (it) {
        Constants.AutomaticMode.DISABLED -> {
          automatic_mode_label.text = getText(R.string.turn_on_dark_mode_automatically_disabled)
          custom_time_range_container.collapseHeight()
          sunset_sunrise_time_container.collapse()
        }
        Constants.AutomaticMode.SUNRISE_SUNSET_BASED -> {
          automatic_mode_label.text = getText(R.string.turn_on_dark_mode_automatically_sunrise_sunset)
          custom_time_range_container.collapseHeight()
          sunset_sunrise_time_container.expand()
        }
        Constants.AutomaticMode.TIME_BASED -> {
          automatic_mode_label.text = getText(R.string.turn_on_dark_mode_automatically_custom_time)
          custom_time_range_container.expandHeight()
          sunset_sunrise_time_container.collapse()
        }
        else -> {}
      }

      checkPermission()
    })

    viewModel.startTime.observe(viewLifecycleOwner, Observer {
      time_range_start.text = DateUtils.formatDateTime(context, TimeHelper.getRangeCalendars().first.timeInMillis, DateUtils.FORMAT_SHOW_TIME)
    })

    viewModel.endTime.observe(viewLifecycleOwner, Observer {
      time_range_end.text = DateUtils.formatDateTime(context, TimeHelper.getRangeCalendars().second.timeInMillis, DateUtils.FORMAT_SHOW_TIME)
    })

    viewModel.sunsetTime.observe(viewLifecycleOwner, Observer {
      time_sunset.text = TimeHelper.getFormattedTime(requireContext(), TimeHelper.getSunsetSunriseCalendars().first)
    })

    viewModel.sunriseTime.observe(viewLifecycleOwner, Observer {
      time_sunrise.text = TimeHelper.getFormattedTime(requireContext(), TimeHelper.getSunsetSunriseCalendars().second)
    })

    viewModel.location.observe(viewLifecycleOwner, Observer {
      location.text = if (Preferences.latitude == "0" && Preferences.longitude == "" || it == "") getString(R.string.unknown_location) else it
    })
  }

  private fun addListener() {
    action_toggle_mode.setOnCheckedChangeListener { button, isChecked ->
      if (button.isPressed) {
        loader.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
          delay(300)
          withContext(Dispatchers.Main) {
            loader.visibility = View.INVISIBLE
            DarkThemeHelper.toggleDarkTheme(requireContext(), isChecked)
          }
        }
      }
    }

    action_change_automatic_mode.setOnClickListener {
      BottomSheetMenu<Constants.AutomaticMode>(requireContext(), header = getString(R.string.turn_on_dark_mode_automatically)).setSelectedValue(Preferences.automaticMode)
        .addItem(getString(R.string.turn_on_dark_mode_automatically_disabled), Constants.AutomaticMode.DISABLED)
        .addItem(getString(R.string.turn_on_dark_mode_automatically_sunrise_sunset), Constants.AutomaticMode.SUNRISE_SUNSET_BASED)
        .addItem(getString(R.string.turn_on_dark_mode_automatically_custom_time), Constants.AutomaticMode.TIME_BASED)
        .addOnSelectItemListener { value ->
          Preferences.automaticMode = value
          UpdatesIntentService.setUpdates(requireContext())

          if (value == Constants.AutomaticMode.SUNRISE_SUNSET_BASED) {
            requirePermission()
            SunsetSunriseService.requestSunsetSunriseTime(requireContext())
            LocationService.requestNewLocation(requireContext())
          }
        }.show()
    }

    action_change_start_time.setOnClickListener {
      showTimePicker(Preferences.startTime) {
        Preferences.startTime = it
        UpdatesIntentService.setUpdates(requireContext())
      }
    }

    action_change_end_time.setOnClickListener {
      showTimePicker(Preferences.endTime) {
        Preferences.endTime = it
        UpdatesIntentService.setUpdates(requireContext())
      }
    }

    action_update_location.setOnClickListener {
      LocationService.requestNewLocation(requireContext())
    }
  }

  private fun showTimePicker(time: String, callback: Callback) {
    val dialog = TimePickerDialog(
      requireContext(),
      OnTimeSetListener { _, hour, minute ->
        callback.invoke("${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}")
      },
      TimeHelper.getHour(time),
      TimeHelper.getMinute(time),
      is24HourFormat(requireContext())
    )
    dialog.show()
    dialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
    dialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
  }

  private fun checkPermission() {
    if (Preferences.automaticMode != Constants.AutomaticMode.SUNRISE_SUNSET_BASED || activity?.checkGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION) == true && activity?.checkGrantedPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == true) {
      action_grant_location_permission.collapseHeight()
      warning_missing_permission.collapseHeight()
    } else {
      action_grant_location_permission.expandHeight()
      warning_missing_permission.expandHeight()
      action_grant_location_permission?.setOnClickListener {
        requirePermission()
      }
    }
  }

  private fun requirePermission() {
    Dexter.withContext(requireContext())
      .withPermissions(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
      ).withListener(object: MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
          report?.let {
            checkPermission()
          }
        }
        override fun onPermissionRationaleShouldBeShown(
          permissions: MutableList<PermissionRequest>?,
          token: PermissionToken?
        ) {
          token?.continuePermissionRequest()
        }
      })
      .check()
  }

  override fun onResume() {
    super.onResume()
    EventBus.getDefault().register(this)
    checkPermission()

    // UI Setup
    action_toggle_mode.isChecked = requireContext().isDarkTheme()
    custom_time_range_container.isVisible = Preferences.automaticMode == Constants.AutomaticMode.TIME_BASED
  }

  override fun onPause() {
    EventBus.getDefault().unregister(this)
    super.onPause()
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  fun onMessageEvent(event: MainUiEvent) {
    loader.visibility = if (event.isLoading) View.VISIBLE else View.INVISIBLE
    event.error?.let {
      if (it == SECURE_PERMISSION_ERROR) {
        viewModel.isSecurePermissionGranted.value = false
      } else if (!it.isBlank()) {
        requireActivity().toast(it)
      }
    }
  }
}