package com.example.happybirthday

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.media.MediaParser.SeekMap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.temporal.TemporalAmount
import kotlin.properties.Delegates

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENTAGE = 1
class MainActivity : AppCompatActivity() {
    private lateinit var etBaseAmount: EditText
    private lateinit var seekBarTip: SeekBar
    private lateinit var tvTipPercentLabel: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTipLevel : TextView
    private lateinit var etNumberOfPeople : TextView
    private lateinit var tvTotalByPerson : TextView
    private var billTotal : Double = 0.0
    private var currentProgress : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etBaseAmount = findViewById(R.id.etBaseAmount)
        seekBarTip = findViewById(R.id.seekBarTip)
        tvTipPercentLabel = findViewById(R.id.tvTipPercentLabel)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipLevel = findViewById(R.id.tvTipLevel)
        etNumberOfPeople = findViewById(R.id.etNumberOfPeople)
        tvTotalByPerson = findViewById(R.id.tvTotalByPerson)

        seekBarTip.progress = INITIAL_TIP_PERCENTAGE
        tvTipPercentLabel.text = "$INITIAL_TIP_PERCENTAGE %"
        showTipLevel(INITIAL_TIP_PERCENTAGE)


        seekBarTip.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "OnProgressChanged para $progress")
                tvTipPercentLabel.text = "$progress %"
                currentProgress = progress
                computeChanges(currentProgress)
                //showTipLevel(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        etBaseAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged: bill amount was updated to $s")
                billTotal = computeTipAndTotal()
                computeChanges(currentProgress)



            }
        })

        etNumberOfPeople.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                computeChanges(currentProgress)
                Log.i(TAG, "afterTextChanged: number of friends to slip the bill with was updated to $s")

            }
        })
    }

    private fun computeChanges(progress: Int){
        billTotal = computeTipAndTotal()
        try{
            computeSplitBill()
        }catch (_: java.lang.NumberFormatException){}
        showTipLevel(progress)
    }

    private fun computeSplitBill() {
        var denom = etNumberOfPeople.text.toString().toDouble()
        var result = billTotal / denom
        Log.i(TAG, "$billTotal / $denom = $result ")
        tvTotalByPerson.text = result.toString()

    }

    private fun showTipLevel(tipPercent: Int) {
        val result : String? = when (tipPercent) {
            in 0..10 -> "Poor"
            in 10 .. 40 -> "Resoanable"
            in 40 .. 80 -> "Nice"
            else -> "Amazing"
        }
        tvTipLevel.text = result

        // Update color
        val color = ArgbEvaluator().evaluate(
            (tipPercent.toFloat() / seekBarTip.max),
            ContextCompat.getColor(this, R.color.red_800),
            ContextCompat.getColor(this, R.color.green_200)
        ) as Int

        tvTipLevel.setTextColor(color)

    }

    @SuppressLint("SetTextI18n")
    private fun computeTipAndTotal() : Double {
        // Get the values

        var baseAmount : Double
        baseAmount = try {
            etBaseAmount.text.toString().toDouble()
        }catch (_: java.lang.NumberFormatException){
            0.0
        }

        val percentage = seekBarTip.progress.toDouble()
        // Compute tip and total
        val tip = baseAmount * percentage / 100
        val total = baseAmount + tip
        // Update UI
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.HALF_EVEN
        val tipAmount = df.format(tip)
        val totalAmount = "%.2f".format(total)

        tvTipAmount.text = " R$ $tipAmount"
        tvTotalAmount.text = "R$ $totalAmount"

        return total
    }
}
