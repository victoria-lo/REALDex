
package com.gameguildstudios.pokedex

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.ml.common.  FirebaseMLException
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StillImageActivity : BaseActivity() {

  private var currentPhotoFile: File? = null
  private var imagePreview: ImageView? = null
  private var textView: TextView? = null
  private var titleView: TextView? = null

  private var classifier: ImageClassifier? = null

  private var pokemon = ""

  private var tts: TextToSpeech? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_still_image)
    imagePreview = findViewById(R.id.image_preview)
    textView = findViewById(R.id.result_text)
    titleView = findViewById(R.id.title_text)
    findViewById<ImageButton>(R.id.photo_camera_button)?.setOnClickListener { takePhoto() }
    findViewById<ImageButton>(R.id.photo_library_button)?.setOnClickListener { chooseFromLibrary() }

    //Init tts to a TextToSpeech object
    tts = TextToSpeech(this, this)

    // Setup image classifier.
    try {
      classifier = ImageClassifier(this)
    } catch (e: FirebaseMLException) {
      textView?.text = getString(R.string.fail_to_initialize_img_classifier)
    }
  }

  override fun onDestroy() {
    classifier?.close()

    if(tts != null){
      tts!!.stop()
      tts!!.shutdown()
    }
    super.onDestroy()
  }

  override fun onInit(status: Int) {
    if(status == TextToSpeech.SUCCESS){
      //set language to english US
      val result = tts!!.setLanguage(Locale.US)
      if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
        Log.e("TTS", "Language not supported")
      }
    }else{
      Log.e("TTS", "Initialization fail")
    }
  }

  //speaks text aloud
  private fun speakOut(text:String){
    tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
  }

  /** Create a file to pass to camera app */
  @Throws(IOException::class)
  private fun createImageFile(): File {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val storageDir = cacheDir
    return createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
    ).apply {
      // Save a file: path for use with ACTION_VIEW intents.
      currentPhotoFile = this
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode != Activity.RESULT_OK) return

    when (requestCode) {
      // Make use of FirebaseVisionImage.fromFilePath to take into account
      // Exif Orientation of the image files.
      REQUEST_IMAGE_CAPTURE -> {
        FirebaseVisionImage.fromFilePath(this, Uri.fromFile(currentPhotoFile)).also {
          classifyImage(it.bitmap)
        }
      }
      REQUEST_PHOTO_LIBRARY -> {
        val selectedImageUri = data?.data ?: return
        FirebaseVisionImage.fromFilePath(this, selectedImageUri).also {
          classifyImage(it.bitmap)
        }
      }
    }
  }

  /** Run image classification on the given [Bitmap] */
  private fun classifyImage(bitmap: Bitmap) {
    if (classifier == null) {
      textView?.text = getString(R.string.uninitialized_img_classifier_or_invalid_context)
      return
    }

    // Show image on screen.
    imagePreview?.setImageBitmap(bitmap)

    // Classify image.
    classifier?.classifyFrame(bitmap)?.
      addOnCompleteListener { task ->
        if (task.isSuccessful) {
          Toast.makeText(this.baseContext, task.result, Toast.LENGTH_SHORT).show()
          textView?.text = ""

          pokemon = task.result?.split("\\s".toRegex())?.get(0).toString().toLowerCase()
          pokemon = pokemon.replace("_".toRegex(),"-")
          if(pokemon != "no"){
            val title = task.result?.split("\\s".toRegex())?.get(0).toString()
            titleView?.text = title
            runPokedexAnalysis()
          }else{
            titleView?.text = "No Pokemon Detected"
          }
        } else {
          val e = task.exception
          Log.e(TAG, "Error classifying frame", e)
          textView?.text = e?.message
        }
      }
  }

  private fun chooseFromLibrary() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"

    // Only accept JPEG or PNG format.
    val mimeTypes = arrayOf("image/jpeg", "image/png")
    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

    startActivityForResult(intent, REQUEST_PHOTO_LIBRARY)
  }

  private fun takePhoto() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
      // Ensure that there's a camera activity to handle the intent.
      takePictureIntent.resolveActivity(packageManager)?.also {
        // Create the File where the photo should go.
        val photoFile: File? = try {
          createImageFile()
        } catch (e: IOException) {
          // Error occurred while creating the File.
          Log.e(TAG, "Unable to save image to run classification.", e)
          null
        }
        // Continue only if the File was successfully created.
        photoFile?.also {
          val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "com.gameguildstudios.pokedex.fileprovider",
            it
          )
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
      }
    }
  }

  private fun runPokedexAnalysis() {
    val url = "https://pokeapi.co/api/v2/pokemon/$pokemon"
    val request = Request.Builder().url(url).build()

    val client = OkHttpClient();
    client.newCall(request).enqueue(object: Callback {
      override fun onResponse(call: Call, response: Response) {
        val body = response?.body()?.string()
        val obj = JSONObject(body)
        val typesArray = obj.getJSONArray("types")
        val typeSlot = typesArray.getJSONObject(0)
        val type = typeSlot.getJSONObject("type")
        val typeName = type.getString("name")
        if(typesArray.length() == 1) {
          speakOut("$pokemon, the $typeName type poekaymaan.")
        }
        else{
          val typeSlot2 = typesArray.getJSONObject(1)
          val type2 = typeSlot2.getJSONObject("type")
          val typeName2 = type2.getString("name")
          speakOut("$pokemon, the $typeName and $typeName2 type poekaymaan.")
        }
      }

      override fun onFailure(call: Call, e: IOException) {
        Toast.makeText(baseContext, "Fail to fetch data.", Toast.LENGTH_SHORT).show()
      }
    })

  }
  companion object {

    /** Tag for the [Log].  */
    private const val TAG = "StillImageActivity"

    /** Request code for starting photo capture activity  */
    private const val REQUEST_IMAGE_CAPTURE = 1

    /** Request code for starting photo library activity  */
    private const val REQUEST_PHOTO_LIBRARY = 2

  }
}
