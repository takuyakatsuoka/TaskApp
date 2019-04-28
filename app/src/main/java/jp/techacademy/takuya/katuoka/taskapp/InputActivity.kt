package jp.techacademy.takuya.katuoka.taskapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import io.realm.Realm
import kotlinx.android.synthetic.main.content_input.*
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.R
import android.support.v4.view.MenuItemCompat
import android.view.MenuInflater
import android.view.ViewGroup
import android.view.LayoutInflater


class SearchFragment : Fragment() {
    private val self = this

    private var searchView: SearchView? = null
    private var searchWord: String? = null

    private val onQueryTextListener = object : SearchView.OnQueryTextListener() {
        fun onQueryTextSubmit(searchWord: String): Boolean {
            // SubmitボタンorEnterKeyを押されたら呼び出されるメソッド
            return self.setSearchWord(searchWord)
        }

        fun onQueryTextChange(newText: String): Boolean {
            // 入力される度に呼び出される
            return false
        }
    }

    fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        // FragmentでMenuを表示する為に必要
        this.setHasOptionsMenu(true)
    }

    fun onCreateView(inflater: LayoutInflater, @Nullable container: ViewGroup, @Nullable savedInstanceState: Bundle): View {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        // Menuの設定
        inflater.inflate(R.menu.search, menu)

        // app:actionViewClass="android.support.v7.widget.SearchView"のItemの取得
        val menuItem = menu.findItem(R.id.search_menu_search_view)

        /**
         * API level 11以上の場合はこっちを使う
         *
         * this.searchView = (SearchView)menuItem.getActionView();
         */

        // ActionViewの取得
        this.searchView = MenuItemCompat.getActionView(menuItem) as SearchView

        // 虫眼鏡アイコンを最初表示するかの設定
        this.searchView!!.setIconifiedByDefault(true)

        // Submitボタンを表示するかどうか
        this.searchView!!.setSubmitButtonEnabled(false)


        if (this.searchWord != "") {
            // TextView.setTextみたいなもの
            this.searchView!!.setQuery(this.searchWord, false)
        } else {
            val queryHint = self.getResources().getString(R.string.search_menu_query_hint_text)
            // placeholderみたいなもの
            this.searchView!!.setQueryHint(queryHint)
        }
        this.searchView!!.setOnQueryTextListener(self.onQueryTextListener)
    }

    private fun setSearchWord(searchWord: String?): Boolean {
        val actionBar = (this.getActivity() as ActionBarActivity).getSupportActionBar()
        actionBar.setTitle(searchWord)
        actionBar.setDisplayShowTitleEnabled(true)
        if (searchWord != null && searchWord != "") {
            // searchWordがあることを確認
            this.searchWord = searchWord
        }
        // 虫眼鏡アイコンを隠す
        this.searchView!!.setIconified(false)
        // SearchViewを隠す
        this.searchView!!.onActionViewCollapsed()
        // Focusを外す
        this.searchView!!.clearFocus()
        return false
    }

    companion object {

        private val TAG = SearchFragment::class.java.simpleName
    }
}
class InputActivity : AppCompatActivity() {

    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0
    private var mTask: Task? = null

    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString =
                    mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay
        )
        datePickerDialog.show()
    }

    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }

    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        // UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        // EXTRA_TASKからTaskのidを取得して、idからTaskのインスタンスを取得する
        val intent = intent
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {
            // 新規作成の場合
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            // 更新の場合
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)
            category_edit_text.setText(mTask!!.category)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString =
                mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }
    }

    private fun addTask() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        if (mTask == null) {
            // 新規作成の場合
            mTask = Task()

            val taskRealmResult = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResult.max("id") != null) {
                    taskRealmResult.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }
        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()
        val category = category_edit_text.text.toString()

        mTask!!.title = title
        mTask!!.contents = content
        mTask!!.category = category
        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date

        realm.copyToRealmOrUpdate(mTask!!)
        realm.commitTransaction()

        realm.close()

        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id,
            resultIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)
    }
}
