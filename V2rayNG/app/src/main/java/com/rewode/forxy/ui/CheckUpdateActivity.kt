package com.rewode.forxy.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.rewode.forxy.AppConfig
import com.rewode.forxy.BuildConfig
import com.rewode.forxy.R
import com.rewode.forxy.databinding.ActivityCheckUpdateBinding
import com.rewode.forxy.dto.CheckUpdateResult
import com.rewode.forxy.extension.toast
import com.rewode.forxy.extension.toastSuccess
import com.rewode.forxy.handler.MmkvManager
import com.rewode.forxy.handler.SpeedtestManager
import com.rewode.forxy.handler.UpdateCheckerManager
import com.rewode.forxy.util.Utils
import kotlinx.coroutines.launch

class CheckUpdateActivity : BaseActivity() {

    private val binding by lazy { ActivityCheckUpdateBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        title = getString(R.string.update_check_for_update)

        binding.layoutCheckUpdate.setOnClickListener {
            checkForUpdates(binding.checkPreRelease.isChecked)
        }

        binding.checkPreRelease.setOnCheckedChangeListener { _, isChecked ->
            MmkvManager.encodeSettings(AppConfig.PREF_CHECK_UPDATE_PRE_RELEASE, isChecked)
        }
        binding.checkPreRelease.isChecked = MmkvManager.decodeSettingsBool(AppConfig.PREF_CHECK_UPDATE_PRE_RELEASE, false)

        "v${BuildConfig.VERSION_NAME} (${SpeedtestManager.getLibVersion()})".also {
            binding.tvVersion.text = it
        }

        checkForUpdates(binding.checkPreRelease.isChecked)
    }

    private fun checkForUpdates(includePreRelease: Boolean) {
        toast(R.string.update_checking_for_update)

        lifecycleScope.launch {
            val result = UpdateCheckerManager.checkForUpdate(includePreRelease)
            if (result.hasUpdate) {
                showUpdateDialog(result)
            } else {
                toastSuccess(R.string.update_already_latest_version)
            }
        }
    }

    private fun showUpdateDialog(result: CheckUpdateResult) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_new_version_found, result.latestVersion))
            .setMessage(result.releaseNotes)
            .setPositiveButton(R.string.update_now) { _, _ ->
                result.downloadUrl?.let {
                    Utils.openUri(this, it)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}