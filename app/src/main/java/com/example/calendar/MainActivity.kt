package com.example.calendar

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.calendar.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

@SuppressLint("NewApi")
class MainActivity : AppCompatActivity() {

    lateinit var bd: ActivityMainBinding

    var timeZone_ = 9.0
    val genesis = LocalDateTime.of(1997,1,22,0,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bd.root)

        bd.dtrSwitch.setOnClickListener { changeTimeZone(getTimeZoneFromInput()) }
        bd.dtrSwitch9.setOnClickListener { changeTimeZone(9.0, true) }
        bd.openCal.setOnClickListener { handleCal() }
        bd.openTime.setOnClickListener { handleTime() }
        bd.clearInput.setOnClickListener { clearInp() }
        bd.submitInput.setOnClickListener { convertDtC() }
        bd.tenToTimeButton.setOnClickListener { convertCtD() }

        bd.yearInput.doAfterTextChanged { jumpYear() }
        bd.monthInput.doAfterTextChanged { jumpMonth() }
        bd.dateInput.doAfterTextChanged { jumpDate() }
        bd.nowOpen.setOnClickListener { nowLoad() }
        bd.hourInput.doAfterTextChanged { jumpHour() }

        bd.tenView.setOnClickListener{copyTenNow()}
        bd.xtView.setOnClickListener{copyTenNow()}

        bd.tenDisp.setOnClickListener{copyDispNow()}
        bd.xtDisp.setOnClickListener{copyDispNow()}

        bd.tzne.setText(setTimeZone(timeZone_))

        val timer = object: CountDownTimer(Long.MAX_VALUE, 765) {
            override fun onTick(millisUntilFinished: Long) {
                val this_moment = LocalDateTime.now()
                val target_date = ymdToDate(this_moment.year, this_moment.monthValue, this_moment.dayOfMonth, this_moment.hour, this_moment.minute, this_moment.second, 9.0)
                val tv_cont = gregToClem(target_date)
                val xt_cont = hourToClem(target_date.hour, target_date.minute, target_date.second.toDouble())
                bd.tenView.setText(tv_cont.toString())
                bd.xtView.setText(".%02d %04d".format((xt_cont * 100).toInt(), ((xt_cont * 1000000) - ((xt_cont * 100).toInt()) * 10000).toInt()))

                bd.notes.setText(this_moment.toEpochSecond(ZoneOffset.UTC).toString() + " " + genesis.toEpochSecond(ZoneOffset.UTC).toString() + " " + target_date.dayOfYear.toString())
            }
            override fun onFinish() {
                bd.tenView.setText("d")
            }
        }
        timer.start()
    }

    fun copyTenNow(){
        val text = bd.tenView.text.toString() + bd.xtView.text.toString().filter { !it.isWhitespace() }
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        showToast("Timnapš ténvíks")
    }

    fun copyDispNow(){
        val text = bd.tenDisp.text.toString() + bd.xtDisp.text.toString().filter { !it.isWhitespace() }
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
        showToast("Timnapš ténvíks")

    }

    fun nowLoad(){
        val datenow = LocalDateTime.now()
        bd.yearInput.setText(datenow.year.toString())
        bd.monthInput.setText(datenow.monthValue.toString())
        bd.dateInput.setText(datenow.dayOfMonth.toString())
        bd.hourInput.setText(datenow.hour.toString())
        bd.minuteInput.setText(datenow.minute.toString())

        bd.tenInput.setText(bd.tenDisp.text.toString())
        bd.xtnInput.setText((hourToClem(datenow.hour, datenow.minute, datenow.second.toDouble()) * 1000000).toInt().toString())

        convertCtD()
        convertDtC()
    }

    fun convertCtD(){
        var ten_ : Long
        var xt_ : Double
        try {
            xt_ = bd.xtnInput.getText().toString().toDouble() / 1000000
            bd.xtnInput.setText("%06d".format((xt_ * 1000000).toInt()))
        } catch (e : Exception){
            xt_ = 0.0
            bd.xtnInput.setText("000000")
        }
        try {
            ten_ = bd.tenInput.getText().toString().toLong()
            val target_date = genesis.plusDays(ten_ - 1).minusHours(9.0.toLong() - timeZone_.toLong()).plusSeconds((xt_ * 86400).toLong())
            val result_string = "%d.%02d.%02d\n%02d:%02d:%02d %s".format(target_date.year, target_date.monthValue, target_date.dayOfMonth, target_date.hour, target_date.minute, target_date.second, setTimeZone(timeZone_).substring(3))
            bd.dateResult.setText(result_string)
            bd.submitInput.hideKeyboard()
        } catch (e : Exception){
            showToast("Akkurat byr téns yetjat.")
            bd.tenInput.setText("")
            bd.xtnInput.setText("")
            bd.dateResult.setText("")
        }
    }

    fun convertDtC(){
        var hour_ : Int
        var minute_ : Int
        val year_ : Int
        val month_ : Int
        val day_ : Int
        val sec = LocalDateTime.now().second
        try {
            hour_ = bd.hourInput.text.toString().toInt()
            minute_ = bd.minuteInput.text.toString().toInt()
            bd.hourInput.setText("%02d".format(bd.hourInput.text.toString().toInt()))
            bd.minuteInput.setText("%02d".format(bd.minuteInput.text.toString().toInt()))
        } catch (e : Exception) {
            hour_ = 0
            minute_ = 0
            bd.hourInput.setText("00")
            bd.minuteInput.setText("00")
        }
        try {
            year_ = bd.yearInput.text.toString().toInt()
            month_ = bd.monthInput.text.toString().toInt()
            day_ = bd.dateInput.text.toString().toInt()
            bd.monthInput.setText("%02d".format(bd.monthInput.text.toString().toInt()))
            bd.dateInput.setText("%02d".format(bd.dateInput.text.toString().toInt()))
            val target_date = ymdToDate(year_, month_, day_, hour_, minute_, sec, timeZone_)
            val date = gregToClem(target_date)
            val time = hourToClem(target_date.hour, target_date.minute, target_date.second.toDouble())
            bd.tenDisp.setText(date.toString())
            bd.xtDisp.setText(".%02d %04d".format((time * 100).toInt(), ((time * 1000000) - ((time * 100).toInt()) * 10000).toInt()))
            bd.submitInput.hideKeyboard()
        } catch (e : Exception) {
            showToast("Akkurat byr ils, sars, téns, dónms, au i nónc yetjat.")
            bd.tenDisp.setText("")
            bd.xtDisp.setText("")
        }
    }

    fun ymdToDate (year : Int, month : Int, date : Int, hour : Int = 0, minute : Int = 0, second : Int = 0, tz : Double = 9.0) : LocalDateTime{
        val stringDate = "%04d-%02d-%02dT%02d:%02d:%02d".format(year, month, date, hour, minute, second)
        var dt = LocalDateTime.parse(stringDate)
        dt = dt.plusHours(9.0.toLong() - tz.toLong())
        return dt
    }

    fun gregToClem(target : LocalDateTime): Int{
        val diff = ChronoUnit.DAYS.between(genesis, target) + 1
        return diff.toInt()
    }

    fun hourToClem(hour: Int, minute: Int, second: Double = 0.0) : Double{
        return ((second + 60 * (minute + 60 * hour)) / 86400)
    }

    fun jumpYear(){
        if (bd.yearInput.getText().toString().length == 4){
            bd.monthInput.requestFocus()
        }
    }
    fun jumpMonth(){
        if (bd.monthInput.getText().toString().length == 2){
            bd.dateInput.requestFocus()
        }
    }
    fun jumpDate(){
        if (bd.dateInput.getText().toString().length == 2){
            bd.hourInput.requestFocus()
        }
    }
    fun jumpHour(){
        if (bd.hourInput.getText().toString().length == 2){
            bd.minuteInput.requestFocus()
        }
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun clearInp(){
        bd.yearInput.setText("")
        bd.monthInput.setText("")
        bd.dateInput.setText("")
        bd.hourInput.setText("")
        bd.minuteInput.setText("")
        bd.tenInput.setText("")
        bd.xtnInput.setText("")
        bd.submitInput.hideKeyboard()
        bd.xtDisp.setText("")
        bd.tenDisp.setText("")
        bd.dateResult.setText("")
    }

    fun debug(msg : String){
        bd.debugline.setText(msg)
    }
    fun handleCal(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this@MainActivity, DatePickerDialog.OnDateSetListener { _, year_, month_, day_ ->

            // Display Selected date in textbox
            bd.yearInput.setText("" + year_)
            bd.monthInput.setText("" + (month_ + 1))
            bd.dateInput.setText("" + day_)

        }, year, month, day)

        dpd.show()
    }

    fun handleTime(){
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val ts_listener = TimePickerDialog.OnTimeSetListener  { _, hour_, minute_ ->
            bd.hourInput.setText("%02d".format(hour_))
            bd.minuteInput.setText("%02d".format(minute_))
        }

        val dpd = TimePickerDialog(this@MainActivity, ts_listener, hour, minute, true)
        dpd.show()
    }

    fun showToast(message : String){
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }
    fun getTimeZoneFromInput() : Double{
        try {
            return bd.dtrInput.text.toString().toDouble()
        } catch (e : Exception) {
            showToast("Č šampvík bo ľuelitia: 9-t pékir.")
            return 9.0
        }
    }

    fun changeTimeZone(tz : Double, force : Boolean = false){
        if (tz > -15 && tz < 15){
            timeZone_ = tz
            bd.datenow.setTimeZone(setTimeZone(timeZone_))
            bd.submitInput.hideKeyboard()
        } else {
            showToast("Č šampvík bo ľuelitia: 9-t pékir.")
            bd.datenow.setTimeZone(setTimeZone(9.0))
        }
        if (force){
            bd.dtrInput.setText("9")
        }
        debug(timeZone_.toString())
        if (!bd.tenDisp.getText().toString().equals("")){
            convertDtC()
        }
        if (!bd.dateResult.getText().toString().equals("")){
            convertCtD()
        }
    }

    fun setTimeZone(tz : Double) : String {
        val format_ : String
        val tz_min = ((tz - tz.toInt()) * 60).toInt()
        if (tz >= 0) {
            format_ = "GMT+%02d:%02d"
        } else {
            format_ = "GMT-%02d:%02d"
        }
        return (format_.format(tz.toInt(), tz_min))
    }
}