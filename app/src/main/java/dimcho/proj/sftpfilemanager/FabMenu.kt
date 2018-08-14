package dimcho.proj.sftpfilemanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.Resources
import android.support.design.widget.FloatingActionButton
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.view.*

/**
 * Created by dimcho on 02.03.18.
 */

// Improvement Callbacks
class FabMenu(private val context: Context, view: View) {
    private enum class FabStates {
        INITIAL, PRESSED
    }

    private var fabState: FabStates = FabStates.INITIAL

    private val resources: Resources = context.resources
    private val fab: FloatingActionButton = view.fab
    private val ltBlur: View = view.ltBlur
    private val fabItem1: FloatingActionButton = view.fabItem1
    private val fabItem2: FloatingActionButton = view.fabItem2

    private fun toggleLayoutBlur(opacity: Float, doOnAnimationEnd: () -> Unit) {
        val shortAnimationDuration =
                resources.getInteger(android.R.integer.config_shortAnimTime)

        val endAnimationListener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                doOnAnimationEnd()
            }
        }

        ltBlur.animate()
                .alpha(opacity)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(endAnimationListener)
                .start()
    }

    private fun openFabMenu() {
        val forwardSpinAnimation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.rotate_forward)

        fab.startAnimation(forwardSpinAnimation)

        // Animate the appearing frame layout
        ltBlur.visibility = View.VISIBLE
        toggleLayoutBlur(1f) {
            fabState = FabStates.PRESSED
            ltBlur.setOnClickListener { toggleFabMenu() } // Closes fab menu
        }

        // Animate fab menu items
        fabItem1.visibility = View.VISIBLE
        fabItem2.visibility = View.VISIBLE

        toggleFabItem(fabItem1, -resources.getDimension(R.dimen.fab_translation_100dp),
                0f)

        toggleFabItem(fabItem2, -resources.getDimension(R.dimen.fab_translation_55dp),
                0f)
    }

    private fun closeFabMenu() {
        val backwardSpinAnimation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.rotate_backward)

        ltBlur.setOnClickListener(null)

        fab.startAnimation(backwardSpinAnimation)
        // Animate the disappearing frame layout
        toggleLayoutBlur(0f) {
            ltBlur.visibility = View.GONE
            fabItem1.visibility = View.GONE
            fabItem2.visibility = View.GONE
            fabState = FabStates.INITIAL
        }

        // Animate fab menu items
        toggleFabItem(fabItem1, 0f, 90f)
        toggleFabItem(fabItem2, 0f, 90f)
    }

    private fun toggleFabItem(menuItem: FloatingActionButton, translation: Float, rotation: Float) {
        menuItem.animate()
                .translationY(translation)
                .rotation(rotation)
                .start()
    }

    fun toggleFabMenu() {
        when (fabState) {
            FabStates.INITIAL -> {
                openFabMenu()
            }

            FabStates.PRESSED -> {
                closeFabMenu()
            }
        }
    }
}