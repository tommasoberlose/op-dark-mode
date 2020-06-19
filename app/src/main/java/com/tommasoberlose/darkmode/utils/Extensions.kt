package com.tommasoberlose.darkmode.utils

import android.content.pm.PackageManager
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.WallpaperManager
import android.content.*
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import android.content.res.Configuration
import android.provider.Settings
import android.util.Patterns
import java.security.NoSuchAlgorithmException
import kotlin.math.max
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import com.tommasoberlose.darkmode.R
import java.util.*


fun PackageManager.missingSystemFeature(name: String): Boolean = !hasSystemFeature(name)

fun Context.toast(message: String, long: Boolean = false) {
    val toast = Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
//    toast.setGravity(Gravity.CENTER, 0, 0)
    toast.show()
}

fun Int.toPixel(context: Context): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()
fun Float.toPixel(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)

fun View.reveal(initialX: Int? = null, initialY: Int? = null, animDuration: Long = 200L) {

    when (visibility) {
        View.VISIBLE -> {
            val anim = ViewAnimationUtils.createCircularReveal(this, initialX ?: this.measuredWidth / 2, initialY ?: this.measuredHeight / 2, max(width.toFloat(), height.toFloat()), 0f)
                .apply {
                    duration = animDuration
                }
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    super.onAnimationEnd(animation)
                }
            })
            anim.start()
        } else -> {
            val anim = ViewAnimationUtils.createCircularReveal(this, initialX ?: this.measuredWidth / 2, initialY ?: this.measuredHeight / 2, 0f, max(width.toFloat(), height.toFloat()))
                .apply {
                    duration = animDuration
                }
            visibility = View.VISIBLE
            anim.start()
        }
    }
}


fun View.expand() {
    measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    if (!isVisible) {
        ValueAnimator.ofFloat(
            0f,
            1f
        ).apply {
            duration = 500L
            addUpdateListener {
                val animatedValue = animatedValue as Float
                translationY = - measuredHeight + animatedValue * measuredHeight
                alpha = animatedValue
            }
            doOnStart {
                isVisible = true
            }
        }.start()
    }
}

fun View.collapse() {
    if (isVisible) {
        ValueAnimator.ofFloat(
            1f,
            0f
        ).apply {
            duration = 500L
            addUpdateListener {
                val animatedValue = animatedValue as Float
                translationY = - measuredHeight + animatedValue * measuredHeight
                alpha = animatedValue
            }
            doOnEnd {
                isVisible = false
            }
        }.start()
    }
}


fun View.expandHeight() {
    measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    if (!isVisible) {
        ValueAnimator.ofInt(
            0,
            measuredHeight
        ).apply {
            duration = 500L
            addUpdateListener {
                val animatedValue = animatedValue as Int
                val newLayoutParams = layoutParams
                newLayoutParams.height = animatedValue
                layoutParams = newLayoutParams
                alpha = animatedFraction
            }
            doOnStart {
                isVisible = true
            }
        }.start()
    }
}

fun View.collapseHeight() {
    val initialHeight = height
    if (isVisible) {
        ValueAnimator.ofInt(
            initialHeight,
            0
        ).apply {
            duration = 500L
            addUpdateListener {
                val animatedValue = animatedValue as Int
                val newLayoutParams = layoutParams
                newLayoutParams.height = animatedValue
                layoutParams = newLayoutParams
                alpha = 1 - animatedFraction
            }
            doOnEnd {
                isVisible = false
            }
        }.start()
    }
}

fun Context.openURI(url: String) {
    try {
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    } catch (e: Exception) {
        try {
            val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(openIntent)
        } catch (ignored: Exception) {
            val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.app_name), url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, R.string.error_opening_uri, Toast.LENGTH_LONG).show()
        }
    }
}

fun Context.isTablet(): Boolean {
    return (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

fun String.md5(): String {
    val MD5 = "MD5"
    try {
        // Create MD5 Hash
        val digest = java.security.MessageDigest
            .getInstance(MD5)
        digest.update(toByteArray())
        val messageDigest = digest.digest()

        // Create Hex String
        val hexString = StringBuilder()
        for (aMessageDigest in messageDigest) {
            var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
            while (h.length < 2)
                h = "0$h"
            hexString.append(h)
        }
        return hexString.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return ""
}

fun String.isValidEmail(): Boolean
        = this.isNotEmpty() &&
        Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun Context.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Activity.isNotificationAccessGranted(): Boolean = Settings.Secure.getString(this.contentResolver,"enabled_notification_listeners").contains(this.packageName)

fun Float.convertDpToPixel(context: Context): Float {
    val resources: Resources = context.resources
    val metrics: DisplayMetrics = resources.displayMetrics
    val px: Float = this * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
    return px
}

fun Float.convertSpToPixels(context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics)
}

fun Context.checkGrantedPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.getCurrentWallpaper(): Drawable? = try {
    WallpaperManager.getInstance(this).drawable
} catch (e: Exception) {
    null
}

fun String.getCapWordString(): String {
    return try {
        val ar = this.split(" ")
        var newText = ""
        for (t: String in ar) {
            newText += " "
            newText += t.substring(0, 1).toUpperCase(Locale.getDefault())
            newText += t.substring(1)
        }
        newText.substring(1)
    } catch (e: Exception) {
        this
    }
}

fun Context.checkIfFitInstalled(): Boolean {
    return try {
        packageManager.getPackageInfo("com.google.android.apps.fitness", PackageManager.GET_ACTIVITIES)
        true
    } catch (e: Exception) {
        false
    }
}

fun Intent.isDefaultSet(context: Context): Boolean {
    val pm = context.packageManager
    return try {
        resolveActivity(pm) != null && resolveActivity(pm).packageName.isNotBlank()
    } catch (ex: java.lang.Exception) {
        false
    }
}