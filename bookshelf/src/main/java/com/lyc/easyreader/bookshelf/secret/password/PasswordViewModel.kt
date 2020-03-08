package com.lyc.easyreader.bookshelf.secret.password

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.arch.LiveState
import com.lyc.easyreader.base.arch.NonNullLiveData
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.secret.SecretManager
import java.util.*

/**
 * Created by Liu Yuchuan on 2020/3/8.
 */
class PasswordViewModel : ViewModel() {
    private var bookFiles: Array<BookFile>? = null

    private val actionBackStack = LinkedList<SecretManager.PasswordAction>()
    private val inputBackStack = Stack<String>()

    val currentAction =
        LiveState<SecretManager.PasswordAction>(SecretManager.PasswordAction.Invalid)
    val errorString = MutableLiveData<String>()
    val currentInput = NonNullLiveData("")

    private companion object {
        const val TAG = "PasswordViewModel"

        const val KEY_BOOK_FILE = "${TAG}_BOOK_FILE"
        const val KEY_CURRENT_ACTION = "${TAG}_CURRENT_ACTION"
        const val KEY_ACTION_STACK = "${TAG}_ACTION_STACK"
        const val KEY_INPUT_STACK = "${TAG}_INPUT_STACK"
    }

    fun init(action: SecretManager.PasswordAction, bookFile: Array<BookFile>?) {
        currentAction.state = action
        this.bookFiles = bookFile
    }

    fun saveState(outState: Bundle) {
        if (bookFiles != null) {
            outState.putParcelableArray(KEY_BOOK_FILE, bookFiles)
        }
        currentAction.state.let {
            if (it != SecretManager.PasswordAction.Invalid) {
                outState.putInt(KEY_CURRENT_ACTION, SecretManager.actionToInt(it))
            }
        }
        outState.putIntArray(
            KEY_ACTION_STACK,
            actionBackStack.map { SecretManager.actionToInt(it) }.toIntArray()
        )
        outState.putStringArray(
            KEY_INPUT_STACK,
            inputBackStack.toTypedArray()
        )
    }

    fun restoreState(bundle: Bundle) {
        bookFiles =
            bundle.getParcelableArray(KEY_BOOK_FILE)?.mapNotNull { it as? BookFile }?.toTypedArray()
        currentAction.state = SecretManager.intToAction(bundle.getInt(KEY_CURRENT_ACTION))
        actionBackStack.clear()
        actionBackStack.addAll(bundle.getIntArray(KEY_ACTION_STACK)!!.map {
            SecretManager.intToAction(
                it
            )
        })
        inputBackStack.clear()
        inputBackStack.addAll(bundle.getStringArray(KEY_INPUT_STACK)!!)
    }

    /**
     * Check if the state of this viewModel is normal.
     * If this method returns false, should not use this viewModel any more.
     */
    fun isValid(): Boolean {
        return currentAction.state != SecretManager.PasswordAction.Invalid && inputBackStack.size == actionBackStack.size
    }

    fun handleLastStep(): Boolean {
        if (actionBackStack.isEmpty()) {
            return false
        }

        currentAction.state = actionBackStack.pop()
        inputBackStack.pop()
        currentInput.value = ""
        errorString.value = null
        return true
    }

    fun backspace() {
        currentInput.value =
            currentInput.value.let { if (it.isEmpty()) "" else it.substring(0, it.length - 1) }
    }

    fun input(value: Int) {
        val newVal = currentInput.value + value
        if (currentInput.value.length < 6) {
            errorString.value = null
            currentInput.value = newVal

            if (newVal.length == 6) {
                currentAction.state.run {
                    if (needValidPassword) {
                        if (SecretManager.validatePassword(currentInput.value)) {
                            nextAction()
                        } else {
                            errorString.value = "密码输入错误"
                            currentInput.value = ""
                        }
                    } else if (needValidLast) {
                        if (inputBackStack.peek() != newVal) {
                            errorString.value = "两次输入不一致"
                            currentInput.value = ""
                        } else {
                            nextAction()
                        }
                    } else {
                        nextAction()
                    }
                }
            }
        }
    }

    fun importBookToSecret(): Boolean {
        val bookFiles = this.bookFiles
        if (bookFiles != null) {
            BookManager.instance.addBooksToSecret(bookFiles.toList())
            return true
        }

        return false
    }

    private fun nextAction() {
        val state = currentAction.state
        val input = currentInput.value
        if (state.needModifyPassword) {
            SecretManager.setPassword(input)
        }
        errorString.value = null
        inputBackStack.push(input)
        actionBackStack.push(state)
        currentInput.value = ""
        currentAction.state = state.nextAction!!
    }
}
