package com.example.phypi.pushpushbox

import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.example.phypi.pushpushbox.recyclerview.RecyclerViewAdapter
import com.example.phypi.pushpushbox.recyclerview.Rowdata
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.popup_layout.view.*
import kotlinx.android.synthetic.main.row.view.*
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    //サーバから受けるプッシュ通知メッセージを入れるリスト
    private lateinit var dataset: MutableList<Rowdata>

    //プッシュ通知リストのRecyclerView UI表示の為のアダプタ
    private lateinit var adapter: RecyclerViewAdapter

    //プッシュ通知のリストクリック際に出すポップアップビュー
    private lateinit var mPopupWindow: PopupWindow

    //ログ用タグ
    private val TAG = MainActivity::class.java.simpleName

    /**
     * MyFcmListenerService.ktでプッシュ通知メッセージを受ける際に
     * アプリがフォアグラウンドにあると
     * 現在のアクティビティを更新（startActivity）させる為
     * フォアグラウンド有無判断フラグ
     */
    companion object {
        @JvmStatic
        private var activityVisible: Boolean = false

        fun isActivityVisible(): Boolean {
            return activityVisible
        }

        fun activityResumed() {
            activityVisible = true
        }

        fun activityPaused() {
            activityVisible = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "Saved token: " + getTokenFromPrefs())

        //Push通知の購読開始
        FirebaseMessaging.getInstance().subscribeToTopic("mytopic")

        //購読解除
        //FirebaseMessaging.getInstance().unsubscribeFromTopic("mytopic")
    }

    override fun onResume() {
        super.onResume()
        MainActivity.activityResumed()
        this.createDataset()
        //this.updateDataset()
        //this.setRecyclerView()
        this.mPopupWindow = PopupWindow(this)
    }

    override fun onPause() {
        super.onPause()
        MainActivity.activityPaused()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPopupWindow.isShowing) mPopupWindow.dismiss()
    }

    /**
     * プッシュ通知のリスト更新
     */
    private fun createDataset() {
        //TODO サーバーにお知らせリストを要請、更新
        //テスト用。仮データ。
        BackGroundTask().execute()

        //テスト用。仮データ。
        /*dataset = ArrayList()
        for (i in 0..9) {
            var data: Rowdata = Rowdata("title " + i, "content " + i)
            dataset.add(data)
        }
        Log.d("firstList: ", dataset.size.toString())*/
        //return dataset
    }

    /**
     * テスト用。本来であらばcreateDataset()でサーバから毎回最新リストを更新してもらう。
     */
    private fun updateDataset() {
        Log.d("updateDataset: ", "IN :::")
        var data: Rowdata = Rowdata(getTitleFromPrefs().toString(), getContenteFromPrefs().toString())
        if (!data.title.equals("null")) dataset.add(data)
        Log.d("newList: ", dataset.size.toString())
        //adapter.notifyDataSetChanged()
    }

    /**
     * RecyclerView UIセッティング
     */
    private fun setRecyclerView() {
        adapter = RecyclerViewAdapter(dataset, this.onRowClicked)
        val llm: LinearLayoutManager = LinearLayoutManager(this)
        val rv: RecyclerView = findViewById(R.id.recyclerView)
        llm.reverseLayout = true
        llm.stackFromEnd = true
        rv.setHasFixedSize(true)
        rv.layoutManager = llm
        rv.addItemDecoration(DividerItemDecoration(rv.context, llm.orientation))
        rv.adapter = adapter
    }

    /**
     * RecyclerViewAdapterに渡すリストクリックリスナ
     */
    private var onRowClicked = View.OnClickListener { row ->
        var popupView = LayoutInflater.from(this).inflate(R.layout.popup_layout,null)
        popupView.popup_title.text = row.row_title.text
        popupView.popup_content.text = row.row_content.text
        popupView.popup_close_button.setOnClickListener {
            if (mPopupWindow.isShowing) mPopupWindow.dismiss()
        }

        mPopupWindow.contentView = popupView
        //mPopupWindow.setBackgroundDrawable()
        mPopupWindow.isOutsideTouchable = true
        mPopupWindow.isFocusable = true
        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0,0)
        mPopupWindow.update(0, 0, 1000, 1500)
    }

    /**
     * サーバーからリスト受信
     * テスト用。仮想マシンのPHPサーバー。
     */
    internal inner class BackGroundTask : AsyncTask<Void, Void, String>() {
        var ip = "10.0.2.2"
        lateinit var target: String

        override fun onPreExecute() {
            super.onPreExecute()
            target = "http://$ip/memorize_english/getlist.php"
        }

        override fun doInBackground(vararg voids: Void): String? {
            try {
                val url = URL(target)
                val httpURLConnection = url.openConnection() as HttpURLConnection
                val inputStream = httpURLConnection.inputStream
                //val bufferedReader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

                val stringBuilder = StringBuilder()

                var temp: String = ""

                temp = inputStream.bufferedReader().use { it.readText() }

                /*while (temp != null) {
                    stringBuilder.append(temp + "\n")
                    temp = bufferedReader.readLine()
                }*/

                //bufferedReader.close()
                //inputStream.close()
                httpURLConnection.disconnect()

                //return stringBuilder.toString().trim { it <= ' ' }
                return temp

            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        override fun onProgressUpdate(vararg values: Void) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            getList(result)
            updateDataset()
            setRecyclerView()
        }
    }

    private fun getList(result: String) {
        dataset = ArrayList()

        try {
            val jsonObject = JSONObject(result)
            val jsonArray = jsonObject.getJSONArray("response")
            var count = 0
            //var words: String
            var answer: String
            var sentence: String
            //var sentence_answer: String

            while (count < jsonArray.length()) {
                val `object` = jsonArray.getJSONObject(count)
                //words = `object`.getString("words")
                answer = `object`.getString("answer")
                sentence = `object`.getString("sentence")
                //sentence_answer = `object`.getString("sentence_answer")

                val list = Rowdata(answer, sentence)

                dataset.add(list)

                count++
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTokenFromPrefs(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getString("registration_id", null)
    }

    private fun getTitleFromPrefs(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getString("title_new", null)
    }

    private fun getContenteFromPrefs(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getString("content_new", null)
    }
}
