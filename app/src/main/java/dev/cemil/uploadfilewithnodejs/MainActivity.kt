package dev.cemil.uploadfilewithnodejs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.exceptions.UploadError
import net.gotev.uploadservice.exceptions.UserCancelledUploadException
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val pickFileRequestCode = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pick.setOnClickListener {
            pickFile()
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, pickFileRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == pickFileRequestCode && resultCode == Activity.RESULT_OK) {
            data?.let {
                upload(it.data.toString())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun upload(filePath: String) {
        MultipartUploadRequest(this, "http://10.0.2.2:4707/upload")
                .addFileToUpload(filePath = filePath, parameterName = "file")
                .subscribe(context = this, lifecycleOwner = this, delegate = object : RequestObserverDelegate {
                    override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                        //2 * 2
//                        println("onProgress")
                    }

                    override fun onSuccess(
                            context: Context,
                            uploadInfo: UploadInfo,
                            serverResponse: ServerResponse
                    ) {
                        //3
//                        println("serverResponse")
                        val resultMap = Gson().fromJson(serverResponse.bodyString, mutableMapOf<String, Any>().javaClass)
                        if (resultMap["status"] == true) {
                            tv_result.setTextColor(resources.getColor(R.color.colorSuccess))
                        } else {
                            tv_result.setTextColor(resources.getColor(R.color.colorError))
                        }
                        tv_result.text = resultMap["message"].toString()
                    }

                    override fun onError(
                            context: Context,
                            uploadInfo: UploadInfo,
                            exception: Throwable
                    ) {
                        when (exception) {
                            is UserCancelledUploadException -> {
                                Log.e("RECEIVER", "Error, user cancelled upload: $uploadInfo")
                            }

                            is UploadError -> {
                                Log.e("RECEIVER", "Error, upload error: ${exception.serverResponse.code}")
                            }

                            else -> {
                                Log.e("RECEIVER", "Error: $uploadInfo", exception)
                            }
                        }

                        Toast.makeText(applicationContext, "Check Node Serve port => 4707", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                        //4
//                        println("onCompleted")
                        println(uploadInfo.toString())
                    }

                    override fun onCompletedWhileNotObserving() {
                        //1
//                        println("onCompletedWhileNotObserving")
                    }
                })
    }
}