package dimcho.proj.sftpfilemanager

import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.os.Bundle
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import java.io.Serializable


/**
 * Created by dimcho on 12.03.18.
 */

const val RESULT_DIALOG_OK = 1
const val RESULT_DIALOG_CANCEL = 2
const val RESULT_PASSWORD_SET = 3
const val RESULT_PASSWORD_CANCEL = 4

class PassInputDialogFragment: DialogFragment() {
//    private var dialogResultListener: OnDialogResultListener? = null
//
//    var message = "Oh gosh, message construction failed!"
//    var customViewResId: Int =  R.layout.layout_password_dialog

    // TODO remodel the PassInputDialog
    companion object Factory {
        private const val KEY_MESSAGE: String = "message"

        fun create(message: Int): PassInputDialogFragment {
            val passDialogFragment = PassInputDialogFragment()

            val args: Bundle = Bundle()
            args.putInt(KEY_MESSAGE, message)

            passDialogFragment.arguments = args

            return passDialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false

        val builder = AlertDialog.Builder(context)
        val inflater: LayoutInflater = activity.layoutInflater
        var dialogCustomView: View? = null
        var resultArray: Array<Array<Int>> = arrayOf(
                arrayOf(RESULT_DIALOG_OK, RESULT_DIALOG_CANCEL),
                arrayOf(RESULT_PASSWORD_SET, RESULT_PASSWORD_CANCEL)
        )

        var resultType: Int = 0

//        if(customViewResId > -1) {
//            dialogCustomView = inflater.inflate(customViewResId, null)
//            builder.setView(dialogCustomView)
//            resultType = 1
//        }
//
//        builder.setMessage(message)
//                .setPositiveButton(android.R.string.ok) { dialog, id ->
//                    val tvPassword: TextView? = dialogCustomView?.findViewById(R.id.tvPassword)
//
//                    dialogResultListener?.onReceiveResult(resultArray[resultType][0],
//                            tvPassword?.text.toString())
//                }
//
//                .setNegativeButton(android.R.string.cancel) { dialog, id ->
//                    dialogResultListener?.onReceiveResult(resultArray[resultType][1])
//                }

        // Create the AlertDialog object and return it
        return builder.create()
    }

//    override fun onPause() {
//        super.onPause()
//        this.dismiss()
//    }

    interface OnDialogResultListener: Serializable {
        fun onReceiveResult(resultCode: Int, result: String = "")
    }

//    fun setOnDialogResultListener(listener: OnDialogResultListener) {
//        dialogResultListener = listener
//    }
}