package dimcho.proj.sftpfilemanager

import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

/**
 * Created by dimcho on 12.03.18.
 */

const val REQUESTED_PASSWORD = "ReqPass"
const val RESULT_PASSWORD_SET = 1
const val RESULT_PASSWORD_CANCEL = 2

class PassInputDialogFragment: DialogFragment() {

    companion object Factory {
        private const val KEY_MESSAGE: String = "message"

        fun create(message: Int): PassInputDialogFragment {
            val passDialogFragment = PassInputDialogFragment()

            val args = Bundle()
            args.putInt(KEY_MESSAGE, message)
            passDialogFragment.arguments = args

            return passDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        val builder = AlertDialog.Builder(context)
        val inflater: LayoutInflater = activity.layoutInflater
        // Set the custom dialog view
        val dialogCustomView: View? = inflater.inflate(R.layout.layout_password_dialog, null)
        builder.setView(dialogCustomView)

        // Creates the AlertDialog object and returns it
        builder.setMessage(resources.getString(arguments.getInt(KEY_MESSAGE)))
                .setPositiveButton(android.R.string.ok) { _, _ ->

                    val tvPassword: TextView? = dialogCustomView?.findViewById(R.id.tvPassword)
                    // Pass the password to the targetFragment via intent
                    val result: Intent = Intent().putExtra(REQUESTED_PASSWORD,
                            tvPassword?.text.toString())

                    targetFragment.onActivityResult(targetRequestCode,
                            RESULT_PASSWORD_SET, result)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->

                    targetFragment.onActivityResult(targetRequestCode,
                            RESULT_PASSWORD_CANCEL, null)
                }

        return builder.create()
    }
}