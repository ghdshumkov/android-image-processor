package com.cft.android.test.feature.view

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.cft.android.test.R
import com.cft.android.test.core.dagger.AppComponent
import com.cft.android.test.databinding.ActivityMainBinding
import com.cft.android.test.feature.model.operation.OperationStatusObserver
import com.cft.android.test.feature.util.InputValueDialogFragment
import com.cft.android.test.feature.util.MediaUtils
import com.cft.android.test.feature.util.MenuDialogFragment

class OperationActivity : AppCompatActivity(), IOperationView,
        InputValueDialogFragment.InputValueDialogListener,
        MenuDialogFragment.MenuDialogListener {

    private lateinit var mPresenter: OperationPresenter
    private lateinit var mOperationListAdapter: OperationListAdapter
    private lateinit var mBinding: ActivityMainBinding

    private var mProgressDialog: ProgressDialog? = null

    private var mCameraTempSelectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mPresenter = OperationPresenter(AppComponent.instance.getOperationManager())
                .also {
                    mBinding.operationListener = it
                }
        mOperationListAdapter = OperationListAdapter({ position ->
            if (mPresenter.checkItemResult(position)) {
                MenuDialogFragment.getInstance(getString(R.string.dialog_operation_menu_title),
                        arrayOf(getString(R.string.label_use_uri), getString(R.string.label_remove)))
                        .show(supportFragmentManager, DIALOG_TAG_OPERATION_MENU)
            }
        })

        mBinding.also {
            it.callback = this
            it.list.adapter = mOperationListAdapter
        }

        savedInstanceState?.let {
            mPresenter.selectedImageUri = it.getParcelable(SIS_BUNDLE_URI_KEY)
        }
    }

    override fun onStart() {
        super.onStart()
        mPresenter.onAttachViewInterface(this)
    }

    override fun onStop() {
        super.onStop()
        mPresenter.onDetachViewInterface()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable(SIS_BUNDLE_URI_KEY, mPresenter.selectedImageUri)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            var cameraAndStoragePermissionGranted = true

            for (grantResult in grantResults) {
                cameraAndStoragePermissionGranted = cameraAndStoragePermissionGranted and
                        (grantResult == PackageManager.PERMISSION_GRANTED)
            }

            if (cameraAndStoragePermissionGranted) {
                localImageSelected()
            } else {
                Toast.makeText(this,
                        R.string.message_camera_permissions_needed,
                        Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("OperationActivity", "onActivityResult, requestCode: $requestCode, resultCode: $resultCode")

        when (requestCode) {
            IMAGE_PIC_INTENT_REQUEST_CODE ->
                if (resultCode == RESULT_OK) {
                    data?.let {
                        it.data?.let {
                            // It is no camera, so select and return
                            mPresenter.selectedImageUri = it
                            return
                        }
                    }
                    // Camera result is OK
                    mPresenter.selectedImageUri = mCameraTempSelectedImageUri
                }
        }
    }

    override fun renderList(list: List<OperationStatusObserver>) {
        mOperationListAdapter.statusList = list
    }

    override fun updateListForNewOperation(newPosition: Int) {
        mOperationListAdapter.notifyItemInserted(newPosition)
    }

    override fun updateListForRemoveOperation(removePos: Int) {
        mOperationListAdapter.notifyItemRemoved(removePos)
    }

    override fun showSelectedImage(imageUri: Uri) {
        mBinding.uri = imageUri
    }

    override fun showDownloadProgress(progress: Int) {
        if (mProgressDialog == null) {
            val dialog = ProgressDialog(this)
            dialog.setMessage(getString(R.string.message_download_image))
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            dialog.isIndeterminate = false
            dialog.setCancelable(false)
            dialog.show()
            mProgressDialog = dialog
        }
        mProgressDialog?.progress = progress
    }

    override fun showErrorMessage(errorText: String) {
        Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show()
    }

    override fun dismissDownloadProgress() {
        mProgressDialog?.dismiss()
        mProgressDialog = null
    }

    fun onImageSelected() {
        MenuDialogFragment.getInstance(getString(R.string.dialog_image_select_title),
                arrayOf(getString(R.string.label_image_select_local), getString(R.string.label_image_select_url)))
                .show(supportFragmentManager, DIALOG_TAG_SELECT_IMAGE_MENU)
    }

    override fun onItemSelected(fragmentTag: String?, pos: Int) {
        fragmentTag?.let {
            if (it == DIALOG_TAG_SELECT_IMAGE_MENU) {
                when(pos) {
                    0 -> localImageSelected()
                    1 -> showUrlSelectionDialog()
                }
            }
            else if (it == DIALOG_TAG_OPERATION_MENU) {
                when(pos) {
                    0 -> mPresenter.useUriOfSelectedItem()
                    1 -> mPresenter.removeSelectedItem()
                }
            }
        }
    }

    override fun onAcceptValueClick(value: String) {
        mPresenter.download(value)
    }

    private fun localImageSelected() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            var cameraIntent: Intent? = null

            val tempFile = MediaUtils.createTempImageFile(this)
            if (tempFile != null) {

                // This is temp data - useful only Camera result is OK
                mCameraTempSelectedImageUri = MediaUtils.getUriForFile(this, tempFile)

                cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                    it.putExtra(MediaStore.EXTRA_OUTPUT, mCameraTempSelectedImageUri)
                }
            }

            val contentIntent = Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                it.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
            }

            val chooserIntent = Intent.createChooser(contentIntent, getString(R.string.label_chooser_title)).also {
                if (cameraIntent != null) {
                    it.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
                }
            }

            startActivityForResult(chooserIntent, IMAGE_PIC_INTENT_REQUEST_CODE)
        }
        else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                    CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun showUrlSelectionDialog() {
        InputValueDialogFragment.getInstance(getString(R.string.dialog_input_url_title),
                getString(R.string.dialog_input_url_message), "")
                .show(supportFragmentManager, null)
    }

    companion object {
        private const val IMAGE_PIC_INTENT_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_REQUEST_CODE = 102

        private const val SIS_BUNDLE_URI_KEY = "selected_uri"

        private const val DIALOG_TAG_OPERATION_MENU = "fragment_tag_operation_menu"
        private const val DIALOG_TAG_SELECT_IMAGE_MENU = "fragment_tag_select_image_menu"
    }
}
