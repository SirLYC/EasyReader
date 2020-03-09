package com.lyc.easyreader.bookshelf.secret

import android.os.SystemClock
import androidx.annotation.IntDef
import com.lyc.easyreader.api.book.BookFile
import com.lyc.easyreader.base.preference.PreferenceManager
import com.lyc.easyreader.base.preference.value.EnumPrefValue
import com.lyc.easyreader.base.preference.value.PrefValue
import com.lyc.easyreader.base.preference.value.StringPrefValue
import com.lyc.easyreader.base.ui.ReaderToast
import com.lyc.easyreader.base.utils.LogUtils
import com.lyc.easyreader.base.utils.getMd5
import com.lyc.easyreader.bookshelf.BookManager
import com.lyc.easyreader.bookshelf.secret.password.PasswordActivity
import com.lyc.easyreader.bookshelf.secret.settings.PasswordSession
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * Created by Liu Yuchuan on 2020/3/8.
 */
object SecretManager {
    private val preference = PreferenceManager.getPrefernce("SecretManager")
    private val passwordAccessLock = ReentrantLock()
    private val passwordHash = StringPrefValue("secret_password_hash", "", preference)
    private const val TAG = "SECRET_MANAGER"

    fun hasPassword(): Boolean {
        return passwordAccessLock.withLock {
            val value = passwordHash.value
            value.length == 32
        }
    }

    fun addBooksToSecret(bookFiles: Iterable<BookFile>, async: Boolean = true) {
        if (hasPassword()) {
            BookManager.instance.addBooksToSecret(bookFiles)
        } else {
            PasswordActivity.openPasswordActivity(ActivityAction.SetOrImportBookFile, bookFiles)
        }
    }

    fun validatePassword(password: String): Boolean {
        if (password.any { !it.isDigit() }) {
            return false
        }

        if (password.length != 6) {
            return false
        }

        val md5 = password.getMd5()
        return passwordAccessLock.withLock {
            val value = passwordHash.value
            if (value.length != 32) {
                return@withLock false
            }

            return md5 == value
        }
    }

    fun updateSecretAccessTime() {
        passwordAccessLock.withLock {
            val time = SystemClock.elapsedRealtime()
            lastEnterPasswordTime = time
            LogUtils.d(TAG, "更新正确输入密码进入私密空间的时间: $time")
        }
    }

    fun setPassword(newPassword: String): Boolean {
        LogUtils.i(TAG, "Set password=$newPassword")
        if (newPassword.any { !it.isDigit() }) {
            return false
        }

        if (newPassword.length != 6) {
            return false
        }
        val md5 = newPassword.getMd5()
        passwordAccessLock.withLock {
            val old = passwordHash.value
            passwordHash.value = md5
            lastEnterPasswordTime = 0
            if (old.length == 32) {
                ReaderToast.showToast("修改密码成功")
            }
        }
        return true
    }

    sealed class PasswordAction(
        val action: String,
        val nextAction: PasswordAction?,
        val needValidPassword: Boolean = false,
        val needValidLast: Boolean = false,
        val needModifyPassword: Boolean = false
    ) {
        object Invalid : PasswordAction("", null)

        // 初始状态
        // 修改密码的第一个状态
        object Modify : PasswordAction("输入新密码", ModifyRepeat)

        // （有密码）进入私密空间的第一个状态
        object Input : PasswordAction("输入密码", ActionImportOrOpen, true)

        // （无密码）进入私密空间的第一个状态
        // 或导入时没有密码的状态
        object Set : PasswordAction("设置私密空间密码", SetAndAddRepeat, false)

        // 中间状态
        object ModifyRepeat :
            PasswordAction("重复密码", ActionBack, needValidLast = true, needModifyPassword = true)

        object SetAndAddRepeat : PasswordAction(
            "重复密码",
            ActionImportOrOpen,
            needValidLast = true,
            needModifyPassword = true
        )

        // 结束动作
        object ActionImportOrOpen : PasswordAction("", null)

        object ActionBack : PasswordAction("", null)
    }

    enum class ActivityAction {
        ModifyPassword,
        OpenSecretPage,
        SetOrImportBookFile
    }

    private const val ACTION_MODIFY_INPUT = 0
    private const val ACTION_INPUT = 3
    private const val ACTION_SET = 6
    private const val ACTION_MODIFY = 9
    private const val ACTION_MODIFY_REPEAT = 12
    private const val ACTION_SET_AND_ADD_REPEAT = 15
    private const val ACTION_OPEN = 18
    private const val ACTION_IMPORT_OR_OPEN = 21
    private const val ACTION_BACK = 24

    @IntDef(
        value = [ACTION_MODIFY_INPUT, ACTION_INPUT, ACTION_SET, ACTION_MODIFY, ACTION_MODIFY_REPEAT, ACTION_SET_AND_ADD_REPEAT, ACTION_OPEN, ACTION_IMPORT_OR_OPEN, ACTION_BACK]
    )
    private annotation class ActionInt

    fun actionToInt(action: PasswordAction): Int {
        return when (action) {
            PasswordAction.Input -> ACTION_INPUT
            PasswordAction.Set -> ACTION_SET
            PasswordAction.Modify -> ACTION_MODIFY
            PasswordAction.ModifyRepeat -> ACTION_MODIFY_REPEAT
            PasswordAction.SetAndAddRepeat -> ACTION_SET_AND_ADD_REPEAT
            PasswordAction.ActionImportOrOpen -> ACTION_IMPORT_OR_OPEN
            PasswordAction.ActionBack -> ACTION_BACK
            else -> throw IllegalArgumentException("Invalid action!")
        }
    }

    fun intToAction(@ActionInt actionInt: Int): PasswordAction {
        return when (actionInt) {
            ACTION_INPUT -> PasswordAction.Input
            ACTION_SET -> PasswordAction.Set
            ACTION_MODIFY -> PasswordAction.Modify
            ACTION_MODIFY_REPEAT -> PasswordAction.ModifyRepeat
            ACTION_SET_AND_ADD_REPEAT -> PasswordAction.SetAndAddRepeat
            ACTION_IMPORT_OR_OPEN -> PasswordAction.ActionImportOrOpen
            ACTION_BACK -> PasswordAction.ActionBack
            else -> {
                throw IllegalArgumentException("Cannot recognize action! ActionInt=${actionInt}")
            }
        }
    }

    // ---------------------------------------- settings ---------------------------------------- //
    val secretSettings = hashSetOf<PrefValue<*>>()

    val passwordSession = EnumPrefValue(
        "password_session",
        PasswordSession.Never,
        preference,
        { PasswordSession.valueOf(it) }).also { secretSettings.add(it) }

    @Volatile
    private var lastEnterPasswordTime = 0L

    fun passwordSessionValid(): Boolean {
        return passwordAccessLock.withLock {
            val time = SystemClock.elapsedRealtime()
            val session = passwordSession.value
            val expireTime = lastEnterPasswordTime + session.duration * 1000L
            val result = expireTime > time
            LogUtils.i(
                TAG,
                "CurrentTime=$time sessionValidDuration=${session.displayName} expireTime=$expireTime isValidNow=$result"
            )
            result
        }
    }
}
