package com.instagram

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.instagram.ui.theme.ShareIntentTestTheme
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        intent.action?.let { Log.d("ShareIntentTest", it) }
        var picUrl: Uri? = null
        var content_url: String? = ""
        var bitmapSource: Bitmap? = null
        var fileOutputStream: BufferedOutputStream? = null
        val destinationFilename =
            filesDir.path + File.separatorChar.toString() + "test.png"
        var saved = false
        var isLyrics = false
        intent.extras?.let { it ->
//            Log.d("ShareIntentTest", "Intent extras: ${it.keySet()}")
            Log.d("ShareIntentTest", "Intent extras: ${it.getString("content_url")}")
            content_url = it.getString("content_url")!!
            if (content_url!!.contains("lyrics")) {
                isLyrics = true
            }
            Log.d(
                "ShareIntentTest",
                "Intent extras: ${it.getParcelable<Uri>("interactive_asset_uri")}"
            )
            picUrl = it.getParcelable("interactive_asset_uri")!!
            Log.d("ShareIntentTest", "Intent extras: ${it.getString("source_application")}")
            if (picUrl != null) {
                val buf = ByteArray(1024)
                var len: Int
                val inputStream = contentResolver.openInputStream(picUrl!!)
                val source = ImageDecoder.createSource(contentResolver, picUrl!!)
                bitmapSource = ImageDecoder.decodeBitmap(source)
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        fileOutputStream =
                            BufferedOutputStream(FileOutputStream(destinationFilename, false))
                        if (inputStream != null) {
                            while (inputStream.read(buf).also { len = it } > 0) {
                                fileOutputStream!!.write(buf, 0, len)
                            }
                        }
                        inputStream?.close()
                        fileOutputStream!!.flush()
                        fileOutputStream!!.close()
                        saved = true
                    } catch (e: Exception) {
                        Log.d("ShareIntentTest", "Error: ${e.message}")
                    }

                }

            }
        }

        setContent {
            val bitmap = remember {
                mutableStateOf<Bitmap?>(bitmapSource)
            }
            val uriHandler = LocalUriHandler.current

            ShareIntentTestTheme {
                Scaffold(
                    topBar = {

                    },
                    floatingActionButton = {
                        if (picUrl != null && saved) ExtendedFloatingActionButton(onClick = {
                            sharePic(
                                destinationFilename,
                                this@MainActivity
                            )
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    "Share",
                                    modifier = Modifier
                                )
                                Text(
                                    text = "Share",
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.Center,
                    containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else Transparent,
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.background,
                            )
                        )
                    ),

                    ) {
                    Surface(
                        color = Transparent,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it)
                            .padding(top = 0.dp),
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(45.dp))
                            if (bitmap.value == null) {
                                Text(
                                    text = "😱 Oops",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "You should Share From  🍊 Apple Music",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(
                                        top = 8.dp,
                                        bottom = 8.dp,
                                        start = 5.dp
                                    )
                                )
                                InfoScreen(this@MainActivity.applicationContext)

                            } else {
                                Text(
                                    text = "👻 Hola",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "👇🏻 A Lovely Share Intent Picked Up",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(
                                        top = 8.dp,
                                        bottom = 10.dp,
                                        start = 5.dp
                                    )
                                )
                                ElevatedCard(
                                    shape = MaterialTheme.shapes.medium.copy(
                                        topStart = CornerSize(
                                            16.dp
                                        ), topEnd = CornerSize(16.dp)
                                    ), modifier = Modifier.padding(bottom = 50.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.Top) {
                                        if (!isLyrics) Spacer(modifier = Modifier.height(16.dp))
                                        Image(
                                            bitmap = bitmap.value!!.asImageBitmap(),
                                            contentDescription = "Share Intent Image",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        //TODO: Show Metadata of the song
//                                        Spacer(modifier = Modifier.height(30.dp))
                                        Row(
                                            Modifier.padding(
                                                top = 5.dp,
                                                start = (5).dp,
                                                end = 9.dp
                                            ), verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.pentagon_40px),
                                                "Icon",
                                                tint = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier
                                                    .padding(end = 16.dp)
                                                    .size(30.dp)

                                            )
                                            Spacer(modifier = Modifier.weight(1.0f))
                                            FilledTonalIconButton(onClick = {
                                                uriHandler.openUri(content_url!!)
                                            }, shape = MaterialTheme.shapes.small) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.link_40px),
                                                    "Icon",
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier
                                                )
                                            }
                                            FilledTonalIconButton(onClick = {
                                               savePicture(
                                                    bitmap.value!!,
                                                    this@MainActivity
                                                  )

                                            }, shape = MaterialTheme.shapes.small, modifier = Modifier.padding(start = 8.dp)) {
                                                Icon(
                                                      painter = painterResource(id = R.drawable.downloading_48px),
                                                    "Icon",
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier
                                                )
                                            }

                                        }
                                    }

                                }

                            }
                        }
                    }
                }
            }
        }
    }

}

fun savePicture(bitmap: Bitmap,context: Context){
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            CoroutineScope(Dispatchers.IO).launch {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "Share Poster")
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                imageUri?.let { uri ->
                    val imageOutStream = resolver.openOutputStream(uri)
                    imageOutStream?.use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
                withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved to Pictures/Share Poster", Toast.LENGTH_SHORT).show()}
        }}else{
            //TODO: Not Tested yet
            val savedImageURL: String = MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "${System.currentTimeMillis()}",
                "Image of Poster"
            )

            Toast.makeText(context, "Saved to Pictures", Toast.LENGTH_SHORT).show()

        }
    }catch (e: Exception){
        Log.e("ShareIntentTest", "Error: ${e.message}")
    }
}
fun sharePic(destinationFilename: String, context: Context) {

    val file = File(destinationFilename)
    val uri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".fake.provider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Music Lyrics Poster"))
}

@Composable
fun InfoScreen(context: Context) {
    CardOptions("包名", context.packageName)
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .padding(top = 16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                "可用功能",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(bottom = 5.dp)
            )
            PackageFunctions("分享歌曲URL")
            PackageFunctions("分享歌词")

        }

    }
}

@Composable
fun PackageFunctions(name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(0.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            "Check",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
//            .padding(16.dp)
                .padding(end = 12.dp)
        )
        Text(
            name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
fun CardOptions(title: String, info: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .padding(20.dp)
                .height(IntrinsicSize.Max)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.inventory_2_48px),
                contentDescription = "Apple Music",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    info,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ShareIntentTestTheme {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                "可用功能",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, end = 0.dp)
            )
            PackageFunctions("分享歌曲URL")
        }

    }
}